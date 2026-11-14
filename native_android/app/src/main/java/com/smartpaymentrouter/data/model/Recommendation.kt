package com.smartpaymentrouter.data.model

data class RewardBreakdown(
    val baseReward: Double,
    val categoryBonus: Double,
    val excessAtBaseRate: Double,
    val offerReward: Double,
    val appliedOffer: Offer? = null,
    val isCapReached: Boolean = false,
    val remainingCapBefore: Double = 0.0,
    val remainingCapAfter: Double = 0.0
)

data class Recommendation(
    val method: PaymentMethod,
    val effectiveReward: Double,
    val effectiveRate: Double,
    val breakdown: RewardBreakdown,
    val reason: String,
    val offerReason: String? = null,
    val ruleWarning: String? = null,
    val isTopPick: Boolean = false
)

data class PaymentLog(
    val id: String,
    val timestamp: String,
    val amount: Double,
    val category: SpendCategory,
    val merchantName: String,
    val payeeAddress: String,
    val source: String, // "scan" or "manual"
    val recommendedMethodId: String,
    val recommendedMethodName: String,
    val chosenMethodId: String,
    val chosenMethodName: String,
    val expectedReward: Double,
    val actualReward: Double? = null
)

data class SavedMerchant(
    val name: String,
    val normalizedName: String,
    val vpa: String? = null,
    val category: SpendCategory,
    val mcc: String? = null,
    val lastUsedAt: String,
    val usageCount: Int = 1
)
