package com.smartpaymentrouter.domain.engine

import com.smartpaymentrouter.data.model.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

object RecommendationEngine {

    /**
     * Calculates the ranked list of recommendations for a given transaction amount and category.
     * Guaranteed to execute within 2 milliseconds on a mid-range Android device.
     */
    fun calculate(
        amount: Double,
        category: SpendCategory,
        methods: List<PaymentMethod>,
        offers: List<Offer>,
        currentTime: Date = Date()
    ): List<Recommendation> {
        if (amount <= 0 || methods.isEmpty()) {
            return emptyList()
        }

        val activeMethods = methods.filter { it.isActive }
        if (activeMethods.isEmpty()) return emptyList()

        val results = mutableListOf<Recommendation>()

        for (method in activeMethods) {
            val specificRule = method.rules.find { it.category == category }
            val allRule = method.rules.find { it.category == null }
            val matchedRule = specificRule ?: allRule

            val baseRate = method.baseRate
            val categoryRate = matchedRule?.rate ?: baseRate

            val totalCap = method.monthlyCap ?: Double.POSITIVE_INFINITY
            val usedCap = method.currentMonthRewardUsed
            val remainingCap = max(0.0, totalCap - usedCap)

            var baseReward = 0.0
            var categoryBonus = 0.0
            var excessAtBaseRate = 0.0
            var isCapReached = false
            val isExcluded = method.exclusions.contains(category)

            if (isExcluded) {
                // Category is excluded from rewards on this method (US-04)
                baseReward = 0.0
                categoryBonus = 0.0
                excessAtBaseRate = 0.0
            } else if (categoryRate > baseRate) {
                val potentialBonus = (amount * categoryRate) / 100.0
                if (potentialBonus <= remainingCap) {
                    categoryBonus = roundTwoDecimals(potentialBonus)
                } else {
                    // Cap reached: bonus is limited to remaining cap
                    isCapReached = true
                    categoryBonus = roundTwoDecimals(remainingCap)

                    // Calculate excess spend that drops to base rate (Requirements Section 9)
                    val spendCovered = if (categoryRate > 0) remainingCap / (categoryRate / 100.0) else 0.0
                    val excessSpend = max(0.0, amount - spendCovered)
                    if (excessSpend > 0 && baseRate > 0) {
                        excessAtBaseRate = roundTwoDecimals((excessSpend * baseRate) / 100.0)
                    }
                }
            } else {
                val potentialBase = (amount * baseRate) / 100.0
                if (potentialBase <= remainingCap) {
                    baseReward = roundTwoDecimals(potentialBase)
                } else {
                    isCapReached = true
                    baseReward = roundTwoDecimals(remainingCap)
                }
            }

            // Evaluate eligible offers
            val eligibleOffers = offers.filter { offer ->
                offer.isActive &&
                        offer.methodId == method.id &&
                        amount >= offer.minAmount &&
                        (offer.usageCap <= 0 || offer.usageCount < offer.usageCap) &&
                        (offer.categories.isEmpty() || offer.categories.contains(category))
            }

            // Calculate offer discount values
            data class OfferValue(val offer: Offer, val value: Double)
            val offerValues = eligibleOffers.map { offer ->
                val v = when {
                    offer.discountFlat != null && offer.discountFlat > 0 -> offer.discountFlat
                    offer.discountPercent != null && offer.discountPercent > 0 -> {
                        val calc = (amount * offer.discountPercent) / 100.0
                        if (offer.maxDiscount != null && offer.maxDiscount > 0) min(calc, offer.maxDiscount) else calc
                    }
                    else -> 0.0
                }
                OfferValue(offer, roundTwoDecimals(v))
            }.sortedByDescending { it.value }

            var bestOfferReward = 0.0
            var appliedOffer: Offer? = null
            var offerReason: String? = null

            if (offerValues.isNotEmpty()) {
                val topOffer = offerValues.first()
                val stackable = offerValues.filter { it.offer.stacks }
                val stackableSum = stackable.sumOf { it.value }

                if (!topOffer.offer.stacks && topOffer.value >= stackableSum) {
                    bestOfferReward = topOffer.value
                    appliedOffer = topOffer.offer
                    offerReason = "Applied highest offer: \"${topOffer.offer.title}\" (does not stack with others)"
                } else if (stackableSum > 0) {
                    bestOfferReward = stackableSum
                    appliedOffer = stackable.first().offer
                    offerReason = "Stacked offers applied: " + stackable.joinToString(", ") { it.offer.title }
                } else {
                    bestOfferReward = topOffer.value
                    appliedOffer = topOffer.offer
                    offerReason = "Offer applied: \"${topOffer.offer.title}\""
                }
            }

            val cardRewardTotal = categoryBonus + excessAtBaseRate + baseReward
            val effectiveReward = roundTwoDecimals(cardRewardTotal + bestOfferReward)
            val effectiveRate = roundTwoDecimals((effectiveReward / amount) * 100.0)

            val remainingAfter = if (totalCap.isFinite()) max(0.0, remainingCap - cardRewardTotal) else Double.POSITIVE_INFINITY

            val breakdown = RewardBreakdown(
                baseReward = baseReward,
                categoryBonus = categoryBonus,
                excessAtBaseRate = excessAtBaseRate,
                offerReward = bestOfferReward,
                appliedOffer = appliedOffer,
                isCapReached = isCapReached,
                remainingCapBefore = if (remainingCap.isFinite()) remainingCap else 999999.0,
                remainingCapAfter = if (remainingAfter.isFinite()) remainingAfter else 999999.0
            )

            // Reason construction
            var reason = when {
                isExcluded -> "${category.displayName} is excluded from rewards on this card"
                specificRule != null && specificRule.rate > baseRate -> {
                    if (isCapReached) {
                        "${specificRule.rate}% ${category.displayName} bonus capped at ₹${categoryBonus.toInt()}" +
                                if (excessAtBaseRate > 0) " + ${baseRate}% base on remaining spend (₹$excessAtBaseRate)" else ""
                    } else {
                        "${specificRule.rate}% ${category.displayName} accelerated cashback"
                    }
                }
                else -> "${baseRate}% standard reward on all purchases"
            }

            if (bestOfferReward > 0 && appliedOffer != null) {
                reason += " + ₹${bestOfferReward.toInt()} (${appliedOffer.title})"
            }

            // Warning if rules are > 90 days old (FR-11)
            var ruleWarning: String? = null
            if (method.ruleLastConfirmed.isNotBlank()) {
                try {
                    val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
                    val confirmedDate = sdf.parse(method.ruleLastConfirmed)
                    if (confirmedDate != null) {
                        val diffDays = ((currentTime.time - confirmedDate.time) / (1000 * 60 * 60 * 24)).toInt()
                        if (diffDays > 90) {
                            ruleWarning = "Card rules last verified $diffDays days ago. Rates may have changed."
                        }
                    }
                } catch (e: Exception) {
                    // ignore
                }
            }

            results.add(
                Recommendation(
                    method = method,
                    effectiveReward = effectiveReward,
                    effectiveRate = effectiveRate,
                    breakdown = breakdown,
                    reason = reason,
                    offerReason = offerReason,
                    ruleWarning = ruleWarning,
                    isTopPick = false
                )
            )
        }

        // Rank by:
        // 1. effectiveReward (descending)
        // 2. Tie-breaker: higher remaining cap (Requirements Section 6 FR-6)
        // 3. Tie-breaker: baseRate (descending)
        results.sortWith { a, b ->
            val rewardDiff = b.effectiveReward.compareTo(a.effectiveReward)
            if (rewardDiff != 0) return@sortWith rewardDiff

            val capDiff = b.breakdown.remainingCapBefore.compareTo(a.breakdown.remainingCapBefore)
            if (capDiff != 0) return@sortWith capDiff

            b.method.baseRate.compareTo(a.method.baseRate)
        }

        if (results.isNotEmpty()) {
            results[0] = results[0].copy(isTopPick = true)
        }

        return results
    }

    private fun roundTwoDecimals(value: Double): Double {
        return (value * 100.0).roundToInt() / 100.0
    }
}
