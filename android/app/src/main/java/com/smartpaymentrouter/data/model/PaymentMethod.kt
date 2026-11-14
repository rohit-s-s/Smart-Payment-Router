package com.smartpaymentrouter.data.model

enum class PaymentMethodType {
    CREDIT_CARD,
    DEBIT_CARD,
    UPI
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
    val network: String, // Visa, Mastercard, RuPay, UPI
    val baseRate: Double, // base cashback percentage, e.g. 1.0%
    val rules: List<RewardRule>,
    val monthlyCap: Double? = null, // in rupees, null if unlimited
    val currentMonthRewardUsed: Double = 0.0, // rupees rewarded this month
    val ruleLastConfirmed: String = "", // ISO date e.g. "2026-11-01"
    val isActive: Boolean = true,
    val cardLast4: String? = null,
    val upiVpa: String? = null
)
