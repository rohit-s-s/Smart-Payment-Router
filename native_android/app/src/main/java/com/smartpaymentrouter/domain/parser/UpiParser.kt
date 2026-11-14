package com.smartpaymentrouter.domain.parser

import android.net.Uri
import com.smartpaymentrouter.data.local.MccDirectory
import com.smartpaymentrouter.data.model.*
import java.net.URLDecoder
import java.nio.charset.StandardCharsets

object UpiParser {

    /**
     * Parses a scanned or uploaded QR code payload
     */
    fun parse(raw: String, savedMerchants: List<SavedMerchant> = emptyList()): ParsedUpi {
        val trimmed = raw.trim()
        if (trimmed.isEmpty()) {
            return ParsedUpi(rawString = "", isUpi = false)
        }

        // Check for UPI Scheme
        if (trimmed.startsWith("upi://pay", ignoreCase = true)) {
            try {
                val query = if (trimmed.contains("?")) trimmed.substringAfter("?") else ""
                val paramMap = parseQueryParams(query)

                val pa = paramMap["pa"]
                val pn = paramMap["pn"]
                val amStr = paramMap["am"]
                val mc = paramMap["mc"]
                val tr = paramMap["tr"]
                val tn = paramMap["tn"]
                val cu = paramMap["cu"] ?: "INR"

                val amount = amStr?.toDoubleOrNull()?.let { if (it > 0) it else null }

                // Category Resolution according to US-03 priority:
                // 1. Saved Merchant History
                // 2. MCC from QR
                // 3. Name Heuristic
                // 4. Default to OTHER
                var category = SpendCategory.OTHER
                var source = CategorySource.DEFAULT

                val matchedMerchant = findSavedMerchant(savedMerchants, pa, pn)
                if (matchedMerchant != null) {
                    category = matchedMerchant.category
                    source = CategorySource.MERCHANT_HISTORY
                } else if (!mc.isNullOrBlank()) {
                    val (mappedCat, _) = MccDirectory.mapMccToCategory(mc)
                    category = mappedCat
                    source = CategorySource.MCC
                } else {
                    category = detectCategoryFromKeywords(pn, tn)
                    if (category != SpendCategory.OTHER) {
                        source = CategorySource.DEFAULT
                    }
                }

                return ParsedUpi(
                    rawString = trimmed,
                    isUpi = true,
                    isNonUpiUrl = false,
                    payeeAddress = pa,
                    payeeName = pn,
                    amount = amount,
                    mcc = mc,
                    transactionRef = tr,
                    transactionNote = tn,
                    currency = cu,
                    suggestedCategory = category,
                    categorySource = source
                )
            } catch (e: Exception) {
                // Fallback to non-UPI
            }
        }

        // Check if it's a website or non-UPI URL (US-01 bullet 3)
        val isUrl = trimmed.startsWith("http://", ignoreCase = true) || trimmed.startsWith("https://", ignoreCase = true)
        val extractedName = if (isUrl) {
            try {
                Uri.parse(trimmed).host?.removePrefix("www.") ?: trimmed
            } catch (e: Exception) {
                trimmed
            }
        } else {
            if (trimmed.length < 40) trimmed else "Scanned Merchant"
        }

        val matchedMerchant = findSavedMerchant(savedMerchants, null, extractedName)

        return ParsedUpi(
            rawString = trimmed,
            isUpi = false,
            isNonUpiUrl = isUrl,
            payeeName = extractedName,
            amount = null, // requires user entry
            suggestedCategory = matchedMerchant?.category ?: SpendCategory.OTHER,
            categorySource = if (matchedMerchant != null) CategorySource.MERCHANT_HISTORY else CategorySource.DEFAULT
        )
    }

    private fun parseQueryParams(queryString: String): Map<String, String> {
        val result = mutableMapOf<String, String>()
        if (queryString.isBlank()) return result

        val pairs = queryString.split("&")
        for (pair in pairs) {
            val idx = pair.indexOf("=")
            if (idx > 0) {
                val key = pair.substring(0, idx).lowercase().trim()
                val rawVal = pair.substring(idx + 1)
                val value = try {
                    URLDecoder.decode(rawVal, StandardCharsets.UTF_8.name())
                } catch (e: Exception) {
                    rawVal
                }
                result[key] = value.trim()
            }
        }
        return result
    }

    private fun findSavedMerchant(
        savedMerchants: List<SavedMerchant>,
        vpa: String?,
        name: String?
    ): SavedMerchant? {
        if (savedMerchants.isEmpty()) return null

        if (!vpa.isNullOrBlank()) {
            val match = savedMerchants.find { it.vpa != null && it.vpa.equals(vpa.trim(), ignoreCase = true) }
            if (match != null) return match
        }

        if (!name.isNullOrBlank()) {
            val normalized = name.lowercase().replace(Regex("[^a-z0-9]"), "").trim()
            if (normalized.isNotBlank()) {
                val exact = savedMerchants.find { it.normalizedName == normalized }
                if (exact != null) return exact

                val partial = savedMerchants.find {
                    it.normalizedName.contains(normalized) || normalized.contains(it.normalizedName)
                }
                if (partial != null) return partial
            }
        }

        return null
    }

    private fun detectCategoryFromKeywords(pn: String?, tn: String?): SpendCategory {
        val combined = "${pn ?: ""} ${tn ?: ""}".lowercase()
        return when {
            combined.contains("petrol") || combined.contains("fuel") || combined.contains("hpcl") || combined.contains("iocl") || combined.contains("shell") -> SpendCategory.FUEL
            combined.contains("restaurant") || combined.contains("cafe") || combined.contains("swiggy") || combined.contains("zomato") || combined.contains("food") -> SpendCategory.DINING
            combined.contains("mart") || combined.contains("supermarket") || combined.contains("grocery") || combined.contains("blinkit") || combined.contains("zepto") -> SpendCategory.GROCERIES
            combined.contains("uber") || combined.contains("ola") || combined.contains("irctc") || combined.contains("flight") || combined.contains("hotel") -> SpendCategory.TRAVEL
            combined.contains("bescom") || combined.contains("electricity") || combined.contains("bill") || combined.contains("broadband") || combined.contains("jio") -> SpendCategory.UTILITIES
            combined.contains("cinema") || combined.contains("movie") || combined.contains("pvr") || combined.contains("bookmyshow") -> SpendCategory.ENTERTAINMENT
            combined.contains("amazon") || combined.contains("flipkart") || combined.contains("store") || combined.contains("cloth") -> SpendCategory.SHOPPING
            else -> SpendCategory.OTHER
        }
    }

    /**
     * Builds a standard UPI deep link Uri for launching UPI apps (FR-7)
     */
    fun buildUpiUri(
        payeeAddress: String,
        payeeName: String?,
        amount: Double?,
        category: SpendCategory,
        mcc: String?
    ): Uri {
        val builder = Uri.parse("upi://pay").buildUpon()
            .appendQueryParameter("pa", payeeAddress)
            .appendQueryParameter("cu", "INR")

        if (!payeeName.isNullOrBlank()) {
            builder.appendQueryParameter("pn", payeeName)
        }
        if (amount != null && amount > 0) {
            builder.appendQueryParameter("am", String.format(java.util.Locale.US, "%.2f", amount))
        }
        if (!mcc.isNullOrBlank()) {
            builder.appendQueryParameter("mc", mcc)
        }
        builder.appendQueryParameter("tn", "Payment via Smart Payment Router (${category.displayName})")

        return builder.build()
    }
}
