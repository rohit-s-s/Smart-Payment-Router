package com.smartpaymentrouter.data.model

data class Offer(
    val id: String,
    val methodId: String,
    val title: String,
    val description: String,
    val categories: List<SpendCategory> = emptyList(), // empty means 'all'
    val minAmount: Double = 0.0,
    val discountPercent: Double? = null,
    val discountFlat: Double? = null,
    val maxDiscount: Double? = null,
    val startDate: String = "",
    val endDate: String = "",
    val usageCap: Int = 10,
    val usageCount: Int = 0,
    val stacks: Boolean = true,
    val requiresActivation: Boolean = false, // FR-8 requirement
    val isActive: Boolean = true
)
