package com.smartpaymentrouter.data.local

import android.content.Context
import com.smartpaymentrouter.data.model.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*

class PaymentRepository(context: Context) {

    private val prefs = context.getSharedPreferences("smart_payment_router_prefs", Context.MODE_PRIVATE)

    private val _methods = MutableStateFlow<List<PaymentMethod>>(loadMethods())
    val methods: StateFlow<List<PaymentMethod>> = _methods.asStateFlow()

    private val _offers = MutableStateFlow<List<Offer>>(loadOffers())
    val offers: StateFlow<List<Offer>> = _offers.asStateFlow()

    private val _savedMerchants = MutableStateFlow(DefaultDataProvider.getDefaultSavedMerchants())
    val savedMerchants: StateFlow<List<SavedMerchant>> = _savedMerchants.asStateFlow()

    private val _paymentLogs = MutableStateFlow<List<PaymentLog>>(emptyList())
    val paymentLogs: StateFlow<List<PaymentLog>> = _paymentLogs.asStateFlow()

    // ==========================================
    // PAYMENT METHOD MANAGEMENT (US-04, US-05, US-07)
    // ==========================================

    fun addMethod(method: PaymentMethod) {
        val list = _methods.value.toMutableList()
        list.add(0, method)
        _methods.value = list
        saveMethods(list)
    }

    fun editMethod(updated: PaymentMethod) {
        val list = _methods.value.map {
            if (it.id == updated.id) updated else it
        }
        _methods.value = list
        saveMethods(list)
    }

    fun deleteMethod(methodId: String) {
        val list = _methods.value.filter { it.id != methodId }
        _methods.value = list
        saveMethods(list)
    }

    fun toggleMethod(methodId: String) {
        val list = _methods.value.map {
            if (it.id == methodId) it.copy(isActive = !it.isActive) else it
        }
        _methods.value = list
        saveMethods(list)
    }

    /**
     * Confirms that the reward rules for this method are current as of today (US-07)
     */
    fun confirmMethodRules(methodId: String) {
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
        val list = _methods.value.map {
            if (it.id == methodId) it.copy(ruleLastConfirmed = today) else it
        }
        _methods.value = list
        saveMethods(list)
    }

    fun updateMethods(newMethods: List<PaymentMethod>) {
        _methods.value = newMethods
        saveMethods(newMethods)
    }

    // ==========================================
    // OFFER MANAGEMENT (US-06)
    // ==========================================

    fun addOffer(offer: Offer) {
        val list = _offers.value.toMutableList()
        list.add(0, offer)
        _offers.value = list
        saveOffers(list)
    }

    fun editOffer(updated: Offer) {
        val list = _offers.value.map {
            if (it.id == updated.id) updated else it
        }
        _offers.value = list
        saveOffers(list)
    }

    fun deleteOffer(offerId: String) {
        val list = _offers.value.filter { it.id != offerId }
        _offers.value = list
        saveOffers(list)
    }

    fun toggleOffer(offerId: String) {
        val list = _offers.value.map {
            if (it.id == offerId) it.copy(isActive = !it.isActive) else it
        }
        _offers.value = list
        saveOffers(list)
    }

    /**
     * Marks an offer as used (US-06). Increments usage count.
     * When usageCap is reached, the offer is stopped from being recommended.
     */
    fun markOfferUsed(offerId: String) {
        val list = _offers.value.map { offer ->
            if (offer.id == offerId) {
                val newCount = offer.usageCount + 1
                val isStillActive = if (offer.usageCap > 0) newCount < offer.usageCap else true
                offer.copy(
                    usageCount = newCount,
                    isActive = isStillActive
                )
            } else {
                offer
            }
        }
        _offers.value = list
        saveOffers(list)
    }

    fun updateOffers(newOffers: List<Offer>) {
        _offers.value = newOffers
        saveOffers(newOffers)
    }

    // ==========================================
    // MERCHANT & PAYMENT LOG MANAGEMENT
    // ==========================================

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
                category = category,
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
            saveMethods(updatedMethods)
        }

        // Record merchant preference (US-03)
        recordMerchantVisit(log.merchantName, log.payeeAddress, log.category)
    }

    fun clearLogs() {
        _paymentLogs.value = emptyList()
    }

    fun resetToDefaults() {
        val defaultMethods = DefaultDataProvider.getDefaultPaymentMethods()
        val defaultOffers = DefaultDataProvider.getDefaultOffers()
        _methods.value = defaultMethods
        _offers.value = defaultOffers
        _savedMerchants.value = DefaultDataProvider.getDefaultSavedMerchants()
        _paymentLogs.value = emptyList()
        saveMethods(defaultMethods)
        saveOffers(defaultOffers)
    }

    // ==========================================
    // PERSISTENCE (SharedPreferences + JSON)
    // ==========================================

    private fun saveMethods(methods: List<PaymentMethod>) {
        try {
            val array = JSONArray()
            methods.forEach { method ->
                val obj = JSONObject()
                obj.put("id", method.id)
                obj.put("name", method.name)
                obj.put("type", method.type.name)
                obj.put("issuer", method.issuer)
                obj.put("network", method.network)
                obj.put("baseRate", method.baseRate)
                obj.put("monthlyCap", method.monthlyCap ?: JSONObject.NULL)
                obj.put("currentMonthRewardUsed", method.currentMonthRewardUsed)
                obj.put("ruleLastConfirmed", method.ruleLastConfirmed)
                obj.put("isActive", method.isActive)
                obj.put("cardLast4", method.cardLast4 ?: JSONObject.NULL)
                obj.put("upiVpa", method.upiVpa ?: JSONObject.NULL)

                val exclArray = JSONArray()
                method.exclusions.forEach { exclArray.put(it.name) }
                obj.put("exclusions", exclArray)

                val rulesArray = JSONArray()
                method.rules.forEach { rule ->
                    val rObj = JSONObject()
                    rObj.put("category", rule.category?.name ?: JSONObject.NULL)
                    rObj.put("rate", rule.rate)
                    rObj.put("capPeriod", rule.capPeriod)
                    rObj.put("capAmount", rule.capAmount ?: JSONObject.NULL)
                    rObj.put("minSpend", rule.minSpend ?: JSONObject.NULL)
                    rulesArray.put(rObj)
                }
                obj.put("rules", rulesArray)
                array.put(obj)
            }
            prefs.edit().putString("saved_methods_json", array.toString()).apply()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun loadMethods(): List<PaymentMethod> {
        val jsonStr = prefs.getString("saved_methods_json", null)
        if (jsonStr.isNullOrBlank()) {
            return DefaultDataProvider.getDefaultPaymentMethods()
        }
        return try {
            val array = JSONArray(jsonStr)
            val list = mutableListOf<PaymentMethod>()
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                val id = obj.getString("id")
                val name = obj.getString("name")
                val type = try {
                    PaymentMethodType.valueOf(obj.getString("type"))
                } catch (e: Exception) {
                    PaymentMethodType.CREDIT_CARD
                }
                val issuer = obj.optString("issuer", "")
                val network = obj.optString("network", "Visa")
                val baseRate = obj.optDouble("baseRate", 1.0)
                val monthlyCap = if (obj.has("monthlyCap") && !obj.isNull("monthlyCap")) obj.getDouble("monthlyCap") else null
                val currentMonthRewardUsed = obj.optDouble("currentMonthRewardUsed", 0.0)
                val ruleLastConfirmed = obj.optString("ruleLastConfirmed", "")
                val isActive = obj.optBoolean("isActive", true)
                val cardLast4 = if (obj.has("cardLast4") && !obj.isNull("cardLast4")) obj.getString("cardLast4") else null
                val upiVpa = if (obj.has("upiVpa") && !obj.isNull("upiVpa")) obj.getString("upiVpa") else null

                val exclusions = mutableListOf<SpendCategory>()
                val exclArray = obj.optJSONArray("exclusions")
                if (exclArray != null) {
                    for (j in 0 until exclArray.length()) {
                        val catName = exclArray.getString(j)
                        SpendCategory.entries.find { it.name == catName }?.let { exclusions.add(it) }
                    }
                }

                val rules = mutableListOf<RewardRule>()
                val rulesArray = obj.optJSONArray("rules")
                if (rulesArray != null) {
                    for (k in 0 until rulesArray.length()) {
                        val rObj = rulesArray.getJSONObject(k)
                        val cat = if (rObj.has("category") && !rObj.isNull("category")) {
                            SpendCategory.entries.find { it.name == rObj.getString("category") }
                        } else null
                        val rate = rObj.optDouble("rate", 1.0)
                        val capPeriod = rObj.optString("capPeriod", "monthly")
                        val capAmount = if (rObj.has("capAmount") && !rObj.isNull("capAmount")) rObj.getDouble("capAmount") else null
                        val minSpend = if (rObj.has("minSpend") && !rObj.isNull("minSpend")) rObj.getDouble("minSpend") else null
                        rules.add(RewardRule(cat, rate, capPeriod, capAmount, minSpend))
                    }
                }

                list.add(
                    PaymentMethod(
                        id = id,
                        name = name,
                        type = type,
                        issuer = issuer,
                        network = network,
                        baseRate = baseRate,
                        rules = rules,
                        exclusions = exclusions,
                        monthlyCap = monthlyCap,
                        currentMonthRewardUsed = currentMonthRewardUsed,
                        ruleLastConfirmed = ruleLastConfirmed,
                        isActive = isActive,
                        cardLast4 = cardLast4,
                        upiVpa = upiVpa
                    )
                )
            }
            if (list.isEmpty()) DefaultDataProvider.getDefaultPaymentMethods() else list
        } catch (e: Exception) {
            e.printStackTrace()
            DefaultDataProvider.getDefaultPaymentMethods()
        }
    }

    private fun saveOffers(offers: List<Offer>) {
        try {
            val array = JSONArray()
            offers.forEach { offer ->
                val obj = JSONObject()
                obj.put("id", offer.id)
                obj.put("methodId", offer.methodId)
                obj.put("title", offer.title)
                obj.put("description", offer.description)
                obj.put("minAmount", offer.minAmount)
                obj.put("discountPercent", offer.discountPercent ?: JSONObject.NULL)
                obj.put("discountFlat", offer.discountFlat ?: JSONObject.NULL)
                obj.put("maxDiscount", offer.maxDiscount ?: JSONObject.NULL)
                obj.put("startDate", offer.startDate)
                obj.put("endDate", offer.endDate)
                obj.put("usageCap", offer.usageCap)
                obj.put("usageCount", offer.usageCount)
                obj.put("stacks", offer.stacks)
                obj.put("requiresActivation", offer.requiresActivation)
                obj.put("isActive", offer.isActive)

                val cats = JSONArray()
                offer.categories.forEach { cats.put(it.name) }
                obj.put("categories", cats)
                array.put(obj)
            }
            prefs.edit().putString("saved_offers_json", array.toString()).apply()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun loadOffers(): List<Offer> {
        val jsonStr = prefs.getString("saved_offers_json", null)
        if (jsonStr.isNullOrBlank()) {
            return DefaultDataProvider.getDefaultOffers()
        }
        return try {
            val array = JSONArray(jsonStr)
            val list = mutableListOf<Offer>()
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                val id = obj.getString("id")
                val methodId = obj.getString("methodId")
                val title = obj.getString("title")
                val description = obj.optString("description", "")
                val minAmount = obj.optDouble("minAmount", 0.0)
                val discountPercent = if (obj.has("discountPercent") && !obj.isNull("discountPercent")) obj.getDouble("discountPercent") else null
                val discountFlat = if (obj.has("discountFlat") && !obj.isNull("discountFlat")) obj.getDouble("discountFlat") else null
                val maxDiscount = if (obj.has("maxDiscount") && !obj.isNull("maxDiscount")) obj.getDouble("maxDiscount") else null
                val startDate = obj.optString("startDate", "")
                val endDate = obj.optString("endDate", "")
                val usageCap = obj.optInt("usageCap", 10)
                val usageCount = obj.optInt("usageCount", 0)
                val stacks = obj.optBoolean("stacks", true)
                val requiresActivation = obj.optBoolean("requiresActivation", false)
                val isActive = obj.optBoolean("isActive", true)

                val categories = mutableListOf<SpendCategory>()
                val cats = obj.optJSONArray("categories")
                if (cats != null) {
                    for (j in 0 until cats.length()) {
                        val catName = cats.getString(j)
                        SpendCategory.entries.find { it.name == catName }?.let { categories.add(it) }
                    }
                }

                list.add(
                    Offer(
                        id = id,
                        methodId = methodId,
                        title = title,
                        description = description,
                        categories = categories,
                        minAmount = minAmount,
                        discountPercent = discountPercent,
                        discountFlat = discountFlat,
                        maxDiscount = maxDiscount,
                        startDate = startDate,
                        endDate = endDate,
                        usageCap = usageCap,
                        usageCount = usageCount,
                        stacks = stacks,
                        requiresActivation = requiresActivation,
                        isActive = isActive
                    )
                )
            }
            if (list.isEmpty()) DefaultDataProvider.getDefaultOffers() else list
        } catch (e: Exception) {
            e.printStackTrace()
            DefaultDataProvider.getDefaultOffers()
        }
    }
}
