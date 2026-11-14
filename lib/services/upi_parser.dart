import 'dart:convert';
import '../data/mcc_directory.dart';
import '../models/parsed_upi.dart';
import '../models/saved_merchant.dart';
import '../models/spend_category.dart';

class UpiParser {
  /// Parses a scanned or uploaded QR code payload
  static ParsedUpi parse(
    String raw, {
    List<SavedMerchant> savedMerchants = const [],
  }) {
    final trimmed = raw.trim();
    if (trimmed.isEmpty) {
      return const ParsedUpi(
        rawUri: '',
        isValidUpi: false,
        suggestedCategory: SpendCategory.other,
        categorySource: CategorySource.defaultValue,
      );
    }

    // Check for UPI Scheme
    if (trimmed.toLowerCase().startsWith('upi://pay')) {
      try {
        final queryString =
            trimmed.contains('?') ? trimmed.substring(trimmed.indexOf('?') + 1) : '';
        final paramMap = _parseQueryParams(queryString);

        final pa = paramMap['pa'];
        final pn = paramMap['pn'];
        final amStr = paramMap['am'];
        final mc = paramMap['mc'];
        final tn = paramMap['tn'];

        double? amount;
        if (amStr != null) {
          final parsedAm = double.tryParse(amStr);
          if (parsedAm != null && parsedAm > 0) {
            amount = parsedAm;
          }
        }

        // Category Resolution according to Epic 1 US-03 priority:
        // 1. Saved Merchant History
        // 2. MCC from QR
        // 3. Name & note Heuristic
        // 4. Default to OTHER
        SpendCategory category = SpendCategory.other;
        CategorySource source = CategorySource.defaultValue;

        final matchedMerchant = _findSavedMerchant(savedMerchants, pa, pn);
        if (matchedMerchant != null) {
          category = matchedMerchant.category;
          source = CategorySource.merchantHistory;
        } else if (mc != null && mc.trim().isNotEmpty) {
          final mappedCat = MccDirectory.lookup(mc);
          if (mappedCat != null) {
            category = mappedCat;
            source = CategorySource.mcc;
          } else {
            category = _detectCategoryFromKeywords(pn, tn);
            source = CategorySource.defaultValue;
          }
        } else {
          category = _detectCategoryFromKeywords(pn, tn);
          source = CategorySource.defaultValue;
        }

        return ParsedUpi(
          rawUri: trimmed,
          isValidUpi: true,
          payeeAddress: pa,
          payeeName: pn,
          amount: amount,
          mcc: mc,
          suggestedCategory: category,
          categorySource: source,
        );
      } catch (_) {
        // Fallback to non-UPI
      }
    }

    // Check if it's a website or non-UPI URL
    final isUrl = trimmed.toLowerCase().startsWith('http://') ||
        trimmed.toLowerCase().startsWith('https://');

    String extractedName;
    if (isUrl) {
      try {
        final uri = Uri.parse(trimmed);
        final host = uri.host;
        extractedName = host.replaceFirst(RegExp(r'^www\.'), '');
        if (extractedName.isEmpty) extractedName = trimmed;
      } catch (_) {
        extractedName = trimmed;
      }
    } else {
      extractedName = trimmed.length < 40 ? trimmed : 'Scanned Merchant';
    }

    final matchedMerchant = _findSavedMerchant(savedMerchants, null, extractedName);

    return ParsedUpi(
      rawUri: trimmed,
      isValidUpi: false,
      payeeName: extractedName,
      amount: null, // requires user entry
      suggestedCategory: matchedMerchant?.category ?? SpendCategory.other,
      categorySource: matchedMerchant != null
          ? CategorySource.merchantHistory
          : CategorySource.defaultValue,
    );
  }

  static Map<String, String> _parseQueryParams(String queryString) {
    final result = <String, String>{};
    if (queryString.trim().isEmpty) return result;

    final pairs = queryString.split('&');
    for (final pair in pairs) {
      final idx = pair.indexOf('=');
      if (idx > 0) {
        final key = pair.substring(0, idx).toLowerCase().trim();
        final rawVal = pair.substring(idx + 1);
        try {
          result[key] = Uri.decodeComponent(rawVal.replaceAll('+', ' ')).trim();
        } catch (_) {
          result[key] = rawVal.trim();
        }
      }
    }
    return result;
  }

  static SavedMerchant? _findSavedMerchant(
    List<SavedMerchant> savedMerchants,
    String? vpa,
    String? name,
  ) {
    if (savedMerchants.isEmpty) return null;

    if (vpa != null && vpa.trim().isNotEmpty) {
      final cleanVpa = vpa.trim().toLowerCase();
      for (final m in savedMerchants) {
        if (m.vpa != null && m.vpa!.trim().toLowerCase() == cleanVpa) {
          return m;
        }
      }
    }

    if (name != null && name.trim().isNotEmpty) {
      final normalized =
          name.toLowerCase().replaceAll(RegExp(r'[^a-z0-9]'), '').trim();
      if (normalized.isNotEmpty) {
        for (final m in savedMerchants) {
          if (m.normalizedName == normalized) return m;
        }
        for (final m in savedMerchants) {
          if (m.normalizedName.contains(normalized) ||
              normalized.contains(m.normalizedName)) {
            return m;
          }
        }
      }
    }

    return null;
  }

  static SpendCategory _detectCategoryFromKeywords(String? pn, String? tn) {
    final combined = '${pn ?? ''} ${tn ?? ''}'.toLowerCase();
    if (combined.contains('petrol') ||
        combined.contains('fuel') ||
        combined.contains('hpcl') ||
        combined.contains('iocl') ||
        combined.contains('shell')) {
      return SpendCategory.fuel;
    }
    if (combined.contains('restaurant') ||
        combined.contains('cafe') ||
        combined.contains('swiggy') ||
        combined.contains('zomato') ||
        combined.contains('food') ||
        combined.contains('dhaba') ||
        combined.contains('pizza')) {
      return SpendCategory.dining;
    }
    if (combined.contains('mart') ||
        combined.contains('supermarket') ||
        combined.contains('grocery') ||
        combined.contains('blinkit') ||
        combined.contains('zepto') ||
        combined.contains('instamart') ||
        combined.contains('provision')) {
      return SpendCategory.groceries;
    }
    if (combined.contains('uber') ||
        combined.contains('ola') ||
        combined.contains('irctc') ||
        combined.contains('flight') ||
        combined.contains('hotel') ||
        combined.contains('makemytrip') ||
        combined.contains('redbus')) {
      return SpendCategory.travel;
    }
    if (combined.contains('bescom') ||
        combined.contains('electricity') ||
        combined.contains('bill') ||
        combined.contains('broadband') ||
        combined.contains('airtel') ||
        combined.contains('jio') ||
        combined.contains('recharge')) {
      return SpendCategory.utilities;
    }
    if (combined.contains('cinema') ||
        combined.contains('movie') ||
        combined.contains('pvr') ||
        combined.contains('inox') ||
        combined.contains('bookmyshow')) {
      return SpendCategory.entertainment;
    }
    if (combined.contains('amazon') ||
        combined.contains('flipkart') ||
        combined.contains('myntra') ||
        combined.contains('store') ||
        combined.contains('cloth') ||
        combined.contains('trends')) {
      return SpendCategory.shopping;
    }
    return SpendCategory.other;
  }

  /// Builds a standard UPI deep link Uri for launching UPI apps (FR-7)
  static Uri buildUpiUri({
    required String payeeAddress,
    String? payeeName,
    double? amount,
    required SpendCategory category,
    String? mcc,
  }) {
    final queryParameters = <String, String>{
      'pa': payeeAddress,
      'cu': 'INR',
    };

    if (payeeName != null && payeeName.trim().isNotEmpty) {
      queryParameters['pn'] = payeeName.trim();
    }
    if (amount != null && amount > 0) {
      queryParameters['am'] = amount.toStringAsFixed(2);
    }
    if (mcc != null && mcc.trim().isNotEmpty) {
      queryParameters['mc'] = mcc.trim();
    }
    queryParameters['tn'] =
        'Payment via Smart Payment Router (${category.displayName})';

    return Uri(
      scheme: 'upi',
      host: 'pay',
      queryParameters: queryParameters,
    );
  }
}
