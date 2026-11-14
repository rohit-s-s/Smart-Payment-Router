import 'spend_category.dart';

enum CategorySource {
  mcc('MCC Code'),
  merchantHistory('Saved Merchant'),
  manual('User Override'),
  defaultValue('Default');

  final String label;
  const CategorySource(this.label);
}

class ParsedUpi {
  final String rawUri;
  final String? payeeAddress; // pa
  final String? payeeName; // pn
  final double? amount; // am
  final String? mcc; // mc
  final SpendCategory suggestedCategory;
  final CategorySource categorySource;
  final bool isValidUpi;

  const ParsedUpi({
    required this.rawUri,
    this.payeeAddress,
    this.payeeName,
    this.amount,
    this.mcc,
    required this.suggestedCategory,
    required this.categorySource,
    required this.isValidUpi,
  });
}

class PaymentDraft {
  final double? amount;
  final SpendCategory category;
  final CategorySource categorySource;
  final String? payeeName;
  final String? payeeAddress;
  final String? mcc;
  final String source; // 'scan' or 'manual'

  const PaymentDraft({
    this.amount,
    this.category = SpendCategory.dining,
    this.categorySource = CategorySource.defaultValue,
    this.payeeName,
    this.payeeAddress,
    this.mcc,
    this.source = 'scan',
  });

  PaymentDraft copyWith({
    double? amount,
    SpendCategory? category,
    CategorySource? categorySource,
    String? payeeName,
    String? payeeAddress,
    String? mcc,
    String? source,
  }) {
    return PaymentDraft(
      amount: amount ?? this.amount,
      category: category ?? this.category,
      categorySource: categorySource ?? this.categorySource,
      payeeName: payeeName ?? this.payeeName,
      payeeAddress: payeeAddress ?? this.payeeAddress,
      mcc: mcc ?? this.mcc,
      source: source ?? this.source,
    );
  }
}
