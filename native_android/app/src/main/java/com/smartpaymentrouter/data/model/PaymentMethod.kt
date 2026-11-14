package com.smartpaymentrouter.data.model

enum class PaymentMethodType(val displayName: String) {
    CREDIT_CARD("Credit Card"),
    DEBIT_CARD("Debit Card"),
    UPI("UPI App")
}

data class RewardRule(
    val category: SpendCategory?, // null means applicable to 'all'
    val rate: Double, // percentage, e.g. 5.0 for 5%
    val capPeriod: String = "monthly", // monthly, per_transaction, none
    val capAmount: Double? = null,
    val minSpend: Double? = null
)

data class PaymentMethod(
    val id: String,
    val name: String,
    val type: PaymentMethodType,
    val issuer: String,
    val network: String, // Visa, Mastercard, RuPay, UPI, Amex
    val baseRate: Double, // base cashback percentage, e.g. 1.0%
    val rules: List<RewardRule> = emptyList(),
    val exclusions: List<SpendCategory> = emptyList(), // US-04 exclusions
    val monthlyCap: Double? = null, // in rupees, null if unlimited
    val currentMonthRewardUsed: Double = 0.0, // rupees rewarded this month
    val ruleLastConfirmed: String = "", // ISO date e.g. "2026-11-01"
    val isActive: Boolean = true,
    val cardLast4: String? = null,
    val upiVpa: String? = null
) {
    /**
     * Checks if reward rules are older than the threshold (US-07: 90 days)
     */
    fun isOutdated(daysThreshold: Int = 90): Boolean {
        if (ruleLastConfirmed.isBlank()) return true
        return try {
            val sdf = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US)
            val confirmedDate = sdf.parse(ruleLastConfirmed) ?: return true
            val diffMs = System.currentTimeMillis() - confirmedDate.time
            val diffDays = java.util.concurrent.TimeUnit.MILLISECONDS.toDays(diffMs)
            diffDays > daysThreshold
        } catch (e: Exception) {
            true
        }
    }

    /**
     * Returns how many days ago rules were confirmed, or -1 if unknown
     */
    fun daysSinceConfirmed(): Long {
        if (ruleLastConfirmed.isBlank()) return -1L
        return try {
            val sdf = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US)
            val confirmedDate = sdf.parse(ruleLastConfirmed) ?: return -1L
            val diffMs = System.currentTimeMillis() - confirmedDate.time
            java.util.concurrent.TimeUnit.MILLISECONDS.toDays(diffMs)
        } catch (e: Exception) {
            -1L
        }
    }
}
