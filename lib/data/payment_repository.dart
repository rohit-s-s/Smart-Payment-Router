import 'dart:convert';
import 'package:flutter/foundation.dart';
import 'package:shared_preferences/shared_preferences.dart';
import '../models/offer.dart';
import '../models/payment_log.dart';
import '../models/payment_method.dart';
import '../models/saved_merchant.dart';
import '../models/spend_category.dart';
import 'default_data_provider.dart';

class PaymentRepository extends ChangeNotifier {
  List<PaymentMethod> _methods = [];
  List<Offer> _offers = [];
  List<SavedMerchant> _savedMerchants = [];
  List<PaymentLog> _paymentLogs = [];
  bool _isInitialized = false;

  List<PaymentMethod> get methods => List.unmodifiable(_methods);
  List<Offer> get offers => List.unmodifiable(_offers);
  List<SavedMerchant> get savedMerchants => List.unmodifiable(_savedMerchants);
  List<SavedMerchant> get merchants => List.unmodifiable(_savedMerchants);
  List<PaymentLog> get paymentLogs => List.unmodifiable(_paymentLogs);
  List<PaymentLog> get logs => List.unmodifiable(_paymentLogs);
  bool get isInitialized => _isInitialized;

  PaymentRepository() {
    _init();
  }

  Future<void> init() async {
    if (!_isInitialized) {
      await _init();
    }
  }

  Future<void> _init() async {
    try {
      final prefs = await SharedPreferences.getInstance();

      // Load methods
      final methodsJson = prefs.getString('saved_methods_json');
      if (methodsJson != null && methodsJson.isNotEmpty) {
        final decoded = jsonDecode(methodsJson) as List<dynamic>;
        _methods = decoded
            .map((e) => PaymentMethod.fromJson(e as Map<String, dynamic>))
            .toList();
      } else {
        _methods = DefaultDataProvider.getDefaultPaymentMethods();
      }

      // Load offers
      final offersJson = prefs.getString('saved_offers_json');
      if (offersJson != null && offersJson.isNotEmpty) {
        final decoded = jsonDecode(offersJson) as List<dynamic>;
        _offers = decoded
            .map((e) => Offer.fromJson(e as Map<String, dynamic>))
            .toList();
      } else {
        _offers = DefaultDataProvider.getDefaultOffers();
      }

      // Load merchants
      final merchantsJson = prefs.getString('saved_merchants_json');
      if (merchantsJson != null && merchantsJson.isNotEmpty) {
        final decoded = jsonDecode(merchantsJson) as List<dynamic>;
        _savedMerchants = decoded
            .map((e) => SavedMerchant.fromJson(e as Map<String, dynamic>))
            .toList();
      } else {
        _savedMerchants = DefaultDataProvider.getDefaultSavedMerchants();
      }

      // Load logs
      final logsJson = prefs.getString('saved_logs_json');
      if (logsJson != null && logsJson.isNotEmpty) {
        final decoded = jsonDecode(logsJson) as List<dynamic>;
        _paymentLogs = decoded
            .map((e) => PaymentLog.fromJson(e as Map<String, dynamic>))
            .toList();
      } else {
        _paymentLogs = [];
      }
    } catch (e) {
      if (kDebugMode) print('Error loading data from SharedPreferences: $e');
      _methods = DefaultDataProvider.getDefaultPaymentMethods();
      _offers = DefaultDataProvider.getDefaultOffers();
      _savedMerchants = DefaultDataProvider.getDefaultSavedMerchants();
      _paymentLogs = [];
    }

    _isInitialized = true;
    notifyListeners();
  }

  // ==========================================
  // PAYMENT METHODS MANAGEMENT (US-04, US-05, US-07)
  // ==========================================

  Future<void> addMethod(PaymentMethod method) async {
    _methods.insert(0, method);
    notifyListeners();
    await _saveMethods();
  }

  Future<void> editMethod(PaymentMethod updated) async {
    final index = _methods.indexWhere((m) => m.id == updated.id);
    if (index >= 0) {
      _methods[index] = updated;
      notifyListeners();
      await _saveMethods();
    }
  }

  Future<void> updateMethod(PaymentMethod updated) => editMethod(updated);

  Future<void> deleteMethod(String methodId) async {
    _methods.removeWhere((m) => m.id == methodId);
    notifyListeners();
    await _saveMethods();
  }

  Future<void> toggleMethod(String methodId) async {
    final index = _methods.indexWhere((m) => m.id == methodId);
    if (index >= 0) {
      final current = _methods[index];
      _methods[index] = current.copyWith(isActive: !current.isActive);
      notifyListeners();
      await _saveMethods();
    }
  }

  /// Confirms that reward rules for this card are current as of today (US-07)
  Future<void> confirmMethodRules(String methodId) async {
    final today = DateTime.now().toIso8601String().substring(0, 10);
    final index = _methods.indexWhere((m) => m.id == methodId);
    if (index >= 0) {
      final current = _methods[index];
      _methods[index] = current.copyWith(ruleLastConfirmed: today);
      notifyListeners();
      await _saveMethods();
    }
  }

  Future<void> confirmRuleFreshness(String methodId) =>
      confirmMethodRules(methodId);

  // ==========================================
  // OFFERS MANAGEMENT (US-06)
  // ==========================================

  Future<void> addOffer(Offer offer) async {
    _offers.insert(0, offer);
    notifyListeners();
    await _saveOffers();
  }

  Future<void> editOffer(Offer updated) async {
    final index = _offers.indexWhere((o) => o.id == updated.id);
    if (index >= 0) {
      _offers[index] = updated;
      notifyListeners();
      await _saveOffers();
    }
  }

  Future<void> updateOffer(Offer updated) => editOffer(updated);

  Future<void> deleteOffer(String offerId) async {
    _offers.removeWhere((o) => o.id == offerId);
    notifyListeners();
    await _saveOffers();
  }

  Future<void> toggleOffer(String offerId) async {
    final index = _offers.indexWhere((o) => o.id == offerId);
    if (index >= 0) {
      final current = _offers[index];
      _offers[index] = current.copyWith(isActive: !current.isActive);
      notifyListeners();
      await _saveOffers();
    }
  }

  /// Marks an offer as used (US-06). Increments usageCount.
  /// If usageCap reached, stops recommending.
  Future<void> markOfferUsed(String offerId) async {
    final index = _offers.indexWhere((o) => o.id == offerId);
    if (index >= 0) {
      final current = _offers[index];
      final newCount = current.usageCount + 1;
      final stillActive =
          current.usageCap > 0 ? newCount < current.usageCap : true;
      _offers[index] = current.copyWith(
        usageCount: newCount,
        isActive: stillActive,
      );
      notifyListeners();
      await _saveOffers();
    }
  }

  Future<void> incrementOfferUsage(String offerId) => markOfferUsed(offerId);

  // ==========================================
  // MERCHANT HISTORY & PAYMENT LOGS
  // ==========================================

  Future<void> recordMerchantVisit(
    String merchantName,
    String? vpa,
    SpendCategory category, [
    String? mcc,
  ]) async {
    if (merchantName.trim().isEmpty && (vpa == null || vpa.trim().isEmpty)) {
      return;
    }

    final normalized = merchantName
        .toLowerCase()
        .replaceAll(RegExp(r'[^a-z0-9]'), '')
        .trim();
    final nowIso = DateTime.now().toUtc().toIso8601String();

    final index = _savedMerchants.indexWhere((m) =>
        (vpa != null &&
            vpa.isNotEmpty &&
            m.vpa != null &&
            m.vpa!.toLowerCase() == vpa.toLowerCase()) ||
        (normalized.isNotEmpty && m.normalizedName == normalized));

    if (index >= 0) {
      final existing = _savedMerchants[index];
      _savedMerchants[index] = existing.copyWith(
        category: category,
        lastUsedAt: nowIso,
        usageCount: existing.usageCount + 1,
        vpa: vpa ?? existing.vpa,
        mcc: mcc ?? existing.mcc,
      );
    } else {
      _savedMerchants.insert(
        0,
        SavedMerchant(
          name: merchantName.isNotEmpty
              ? merchantName
              : (vpa ?? 'Saved Merchant'),
          normalizedName: normalized,
          vpa: vpa,
          category: category,
          mcc: mcc,
          lastUsedAt: nowIso,
          usageCount: 1,
        ),
      );
    }
    notifyListeners();
    await _saveMerchants();
  }

  Future<void> saveMerchant(SavedMerchant merchant) => recordMerchantVisit(
        merchant.name,
        merchant.vpa,
        merchant.category,
        merchant.mcc,
      );

  Future<void> addPaymentLog(PaymentLog log) async {
    _paymentLogs.insert(0, log);

    // Update monthly cap usage on the chosen card
    if (log.expectedReward > 0) {
      final mIndex = _methods.indexWhere((m) => m.id == log.chosenMethodId);
      if (mIndex >= 0) {
        final current = _methods[mIndex];
        _methods[mIndex] = current.copyWith(
          currentMonthRewardUsed:
              current.currentMonthRewardUsed + log.expectedReward,
        );
        await _saveMethods();
      }
    }

    // Record merchant preference (US-03)
    await recordMerchantVisit(
      log.merchantName,
      log.payeeAddress,
      log.category,
    );

    notifyListeners();
    await _saveLogs();
  }

  Future<void> addLog(PaymentLog log) => addPaymentLog(log);

  Future<void> clearLogs() async {
    _paymentLogs.clear();
    notifyListeners();
    await _saveLogs();
  }

  Future<void> resetToDefaults() async {
    _methods = DefaultDataProvider.getDefaultPaymentMethods();
    _offers = DefaultDataProvider.getDefaultOffers();
    _savedMerchants = DefaultDataProvider.getDefaultSavedMerchants();
    _paymentLogs.clear();
    notifyListeners();
    await _saveMethods();
    await _saveOffers();
    await _saveMerchants();
    await _saveLogs();
  }

  // ==========================================
  // PERSISTENCE HELPERS
  // ==========================================

  Future<void> _saveMethods() async {
    try {
      final prefs = await SharedPreferences.getInstance();
      final encoded = jsonEncode(_methods.map((m) => m.toJson()).toList());
      await prefs.setString('saved_methods_json', encoded);
    } catch (e) {
      if (kDebugMode) print('Error saving methods: $e');
    }
  }

  Future<void> _saveOffers() async {
    try {
      final prefs = await SharedPreferences.getInstance();
      final encoded = jsonEncode(_offers.map((o) => o.toJson()).toList());
      await prefs.setString('saved_offers_json', encoded);
    } catch (e) {
      if (kDebugMode) print('Error saving offers: $e');
    }
  }

  Future<void> _saveMerchants() async {
    try {
      final prefs = await SharedPreferences.getInstance();
      final encoded =
          jsonEncode(_savedMerchants.map((m) => m.toJson()).toList());
      await prefs.setString('saved_merchants_json', encoded);
    } catch (e) {
      if (kDebugMode) print('Error saving merchants: $e');
    }
  }

  Future<void> _saveLogs() async {
    try {
      final prefs = await SharedPreferences.getInstance();
      final encoded =
          jsonEncode(_paymentLogs.map((l) => l.toJson()).toList());
      await prefs.setString('saved_logs_json', encoded);
    } catch (e) {
      if (kDebugMode) print('Error saving logs: $e');
    }
  }
}
