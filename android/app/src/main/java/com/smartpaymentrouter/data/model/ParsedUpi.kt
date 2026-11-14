package com.smartpaymentrouter.data.model

enum class CategorySource {
    MCC,
    MERCHANT_HISTORY,
    MANUAL,
    DEFAULT
}

data class ParsedUpi(
    val rawString: String,
    val isUpi: Boolean,
    val isNonUpiUrl: Boolean = false,
    val payeeAddress: String? = null, // pa
    val payeeName: String? = null,    // pn
    val amount: Double? = null,       // am
    val mcc: String? = null,          // mc
    val transactionRef: String? = null,
    val transactionNote: String? = null,
    val currency: String = "INR",
    val suggestedCategory: SpendCategory = SpendCategory.OTHER,
    val categorySource: CategorySource = CategorySource.DEFAULT
)

data class PaymentDraft(
    val source: String = "manual", // "scan" or "manual"
    val payeeName: String = "",
    val payeeAddress: String = "",
    val amount: Double? = null,
    val category: SpendCategory = SpendCategory.DINING,
    val categorySource: CategorySource = CategorySource.DEFAULT,
    val mcc: String? = null,
    val rawQr: String? = null,
    val isNonUpiUrl: Boolean = false
)
