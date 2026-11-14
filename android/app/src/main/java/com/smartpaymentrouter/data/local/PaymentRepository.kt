package com.smartpaymentrouter.data.local

import android.content.Context
import com.smartpaymentrouter.data.model.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.text.SimpleDateFormat
import java.util.*

class PaymentRepository(context: Context) {

    private val prefs = context.getSharedPreferences("smart_payment_router_prefs", Context.MODE_PRIVATE)

    private val _methods = MutableStateFlow(DefaultDataProvider.getDefaultPaymentMethods())
    val methods: StateFlow<List<PaymentMethod>> = _methods.asStateFlow()

    private val _offers = MutableStateFlow(DefaultDataProvider.getDefaultOffers())
    val offers: StateFlow<List<Offer>> = _offers.asStateFlow()

    private val _savedMerchants = MutableStateFlow(DefaultDataProvider.getDefaultSavedMerchants())
    val savedMerchants: StateFlow<List<SavedMerchant>> = _savedMerchants.asStateFlow()

    private val _paymentLogs = MutableStateFlow<List<PaymentLog>>(emptyList())
    val paymentLogs: StateFlow<List<PaymentLog>> = _paymentLogs.asStateFlow()

    fun updateMethods(newMethods: List<PaymentMethod>) {
        _methods.value = newMethods
    }

    fun updateOffers(newOffers: List<Offer>) {
        _offers.value = newOffers
    }

    fun recordMerchantVisit(merchantName: String, vpa: String?, category: SpendCategory, mcc: String? = null) {
        if (merchantName.isBlank() && vpa.isNullOrBlank()) return

        val normalized = merchantName.lowercase().replace(Regex("[^a-z0-9]"), "").trim()
        val currentList = _savedMerchants.value.toMutableList()

        val index = currentList.indexOfFirst {
            (!vpa.isNullOrBlank() && !it.vpa.isNullOrBlank() && it.vpa.equals(vpa, ignoreCase = true)) ||
                    (normalized.isNotBlank() && it.normalizedName == normalized)
        }

        val nowIso = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).format(Date())

        if (index >= 0) {
            val existing = currentList[index]
            currentList[index] = existing.copy(
                category = category, // remember updated category preference
                lastUsedAt = nowIso,
                usageCount = existing.usageCount + 1,
                vpa = vpa ?: existing.vpa,
                mcc = mcc ?: existing.mcc
            )
        } else {
            currentList.add(
                0,
                SavedMerchant(
                    name = merchantName.ifBlank { vpa ?: "Saved Merchant" },
                    normalizedName = normalized,
                    vpa = vpa,
                    category = category,
                    mcc = mcc,
                    lastUsedAt = nowIso,
                    usageCount = 1
                )
            )
        }

        _savedMerchants.value = currentList
    }

    fun addPaymentLog(log: PaymentLog) {
        val current = _paymentLogs.value.toMutableList()
        current.add(0, log)
        _paymentLogs.value = current

        // Update card cap usage
        if (log.expectedReward > 0) {
            val updatedMethods = _methods.value.map { method ->
                if (method.id == log.chosenMethodId) {
                    method.copy(currentMonthRewardUsed = method.currentMonthRewardUsed + log.expectedReward)
                } else {
                    method
                }
            }
            _methods.value = updatedMethods
        }

        // Record merchant preference (US-03)
        recordMerchantVisit(log.merchantName, log.payeeAddress, log.category)
    }

    fun clearLogs() {
        _paymentLogs.value = emptyList()
    }

    fun resetToDefaults() {
        _methods.value = DefaultDataProvider.getDefaultPaymentMethods()
        _offers.value = DefaultDataProvider.getDefaultOffers()
        _savedMerchants.value = DefaultDataProvider.getDefaultSavedMerchants()
        _paymentLogs.value = emptyList()
    }
}
