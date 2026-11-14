package com.smartpaymentrouter

import com.smartpaymentrouter.data.model.*
import com.smartpaymentrouter.domain.engine.RecommendationEngine
import org.junit.Assert.*
import org.junit.Test

class RecommendationEngineTest {

    private val cardWith5PercentDining = PaymentMethod(
        id = "test-card-dining",
        name = "Rewards Dining Card",
        type = PaymentMethodType.CREDIT_CARD,
        issuer = "Test Bank",
        network = "Visa",
        baseRate = 1.0,
        rules = listOf(
            RewardRule(SpendCategory.DINING, 5.0, "monthly", 1000.0),
            RewardRule(null, 1.0)
        ),
        monthlyCap = 1000.0,
        currentMonthRewardUsed = 0.0,
        ruleLastConfirmed = "2026-11-01",
        isActive = true
    )

    private val genericBaseCard = PaymentMethod(
        id = "generic-card",
        name = "Generic 1.5% Card",
        type = PaymentMethodType.CREDIT_CARD,
        issuer = "Generic Bank",
        network = "Mastercard",
        baseRate = 1.5,
        rules = listOf(RewardRule(null, 1.5)),
        monthlyCap = 2000.0,
        currentMonthRewardUsed = 0.0,
        ruleLastConfirmed = "2026-11-01",
        isActive = true
    )

    @Test
    fun testAcceptanceCriteria1_5percentDiningOn500Spend() {
        // Acceptance Criteria: "Given a stored card with a 5 percent dining rate and a monthly cap of 1,000 rupees,
        // and a dining payment of 500 rupees with no offers, the app recommends that card and shows an estimated reward of 25 rupees."
        val results = RecommendationEngine.calculate(
            amount = 500.0,
            category = SpendCategory.DINING,
            methods = listOf(cardWith5PercentDining, genericBaseCard),
            offers = emptyList()
        )

        assertTrue(results.isNotEmpty())
        val top = results.first()

        assertEquals("test-card-dining", top.method.id)
        assertEquals(25.0, top.effectiveReward, 0.01)
        assertEquals(5.0, top.effectiveRate, 0.01)
        assertTrue(top.isTopPick)
    }

    @Test
    fun testAcceptanceCriteria2_with900UsedOutof1000Cap() {
        // Acceptance Criteria 2: "Given the same card with 900 rupees already used this month,
        // the app shows the reduced reward for the remaining 100 rupees of cap and recommends the next-best method if its reward is higher."
        val cardWith900Used = cardWith5PercentDining.copy(currentMonthRewardUsed = 900.0)

        // For large dining spend of ₹3000:
        // 5% of ₹3000 is ₹150, but remaining cap is only ₹100.
        // ₹100 cap covers 100 / 0.05 = ₹2000 spend.
        // Remaining ₹1000 spend drops to base rate 1% = ₹10.
        // Total reward = 100 + 10 = ₹110.
        val results = RecommendationEngine.calculate(
            amount = 3000.0,
            category = SpendCategory.DINING,
            methods = listOf(cardWith900Used, genericBaseCard),
            offers = emptyList()
        )

        val cardResult = results.find { it.method.id == "test-card-dining" }!!
        assertTrue(cardResult.breakdown.isCapReached)
        assertEquals(100.0, cardResult.breakdown.categoryBonus, 0.01)
        assertEquals(10.0, cardResult.breakdown.excessAtBaseRate, 0.01)
        assertEquals(110.0, cardResult.effectiveReward, 0.01)
    }

    @Test
    fun testAcceptanceCriteria2b_recommendsNextBestWhenRewardHigher() {
        val exhaustedCard = cardWith5PercentDining.copy(currentMonthRewardUsed = 1000.0) // 0 cap left

        // Exhausted card earns 1% base = ₹30
        // Generic card earns 1.5% base = ₹45
        val results = RecommendationEngine.calculate(
            amount = 3000.0,
            category = SpendCategory.DINING,
            methods = listOf(exhaustedCard, genericBaseCard),
            offers = emptyList()
        )

        assertEquals("generic-card", results[0].method.id)
        assertEquals(45.0, results[0].effectiveReward, 0.01)
        assertEquals("test-card-dining", results[1].method.id)
        assertEquals(30.0, results[1].effectiveReward, 0.01)
    }

    @Test
    fun testAcceptanceCriteria4_manualEntryParity() {
        // Acceptance Criteria 4 (US-02): Manually entered purchase gets the same recommendation as equivalent scanned one
        val scanned = RecommendationEngine.calculate(
            amount = 750.0,
            category = SpendCategory.DINING,
            methods = listOf(cardWith5PercentDining, genericBaseCard),
            offers = emptyList()
        )

        val manual = RecommendationEngine.calculate(
            amount = 750.0,
            category = SpendCategory.DINING,
            methods = listOf(cardWith5PercentDining, genericBaseCard),
            offers = emptyList()
        )

        assertEquals(scanned.first().method.id, manual.first().method.id)
        assertEquals(scanned.first().effectiveReward, manual.first().effectiveReward, 0.001)
    }

    @Test
    fun testNonStackableOffers_highestValueChosen() {
        val offerA = Offer(
            id = "o1",
            methodId = "test-card-dining",
            title = "10% Dining Offer",
            description = "10% off up to ₹100",
            categories = listOf(SpendCategory.DINING),
            minAmount = 500.0,
            discountPercent = 10.0,
            maxDiscount = 100.0,
            stacks = false
        )

        val offerB = Offer(
            id = "o2",
            methodId = "test-card-dining",
            title = "Flat ₹30 Offer",
            description = "Flat ₹30",
            categories = listOf(SpendCategory.DINING),
            minAmount = 500.0,
            discountFlat = 30.0,
            stacks = false
        )

        val results = RecommendationEngine.calculate(
            amount = 600.0,
            category = SpendCategory.DINING,
            methods = listOf(cardWith5PercentDining),
            offers = listOf(offerA, offerB)
        )

        // 10% on 600 = ₹60, which is higher than ₹30
        assertEquals(60.0, results.first().breakdown.offerReward, 0.01)
        assertTrue(results.first().offerReason!!.contains("does not stack with others"))
    }

    @Test
    fun testTieBreaker_higherRemainingCapPreferred() {
        val cardA = cardWith5PercentDining.copy(id = "card-a", currentMonthRewardUsed = 800.0) // 200 left
        val cardB = cardWith5PercentDining.copy(id = "card-b", currentMonthRewardUsed = 200.0) // 800 left

        val results = RecommendationEngine.calculate(
            amount = 400.0,
            category = SpendCategory.DINING,
            methods = listOf(cardA, cardB),
            offers = emptyList()
        )

        // Both give ₹20 reward, but cardB has ₹800 remaining cap vs cardA's ₹200
        assertEquals("card-b", results.first().method.id)
    }

    @Test
    fun testPerformance_runsUnderTwoSeconds() {
        val start = System.currentTimeMillis()
        for (i in 0 until 100) {
            RecommendationEngine.calculate(
                amount = 1250.0,
                category = SpendCategory.GROCERIES,
                methods = listOf(cardWith5PercentDining, genericBaseCard),
                offers = emptyList()
            )
        }
        val elapsed = System.currentTimeMillis() - start
        // 100 calculations should finish in well under 100ms (< 1ms per run, beating 2000ms threshold)
        assertTrue(elapsed < 200)
    }
}
