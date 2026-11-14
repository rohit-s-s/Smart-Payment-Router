import 'spend_category.dart';

enum PaymentMethodType {
  creditCard('Credit Card'),
  debitCard('Debit Card'),
  upi('UPI App');

  final String displayName;
  const PaymentMethodType(this.displayName);

  static PaymentMethodType fromString(String val) {
    for (final t in PaymentMethodType.values) {
      if (t.name == val) return t;
    }
    return PaymentMethodType.creditCard;
  }
}

class RewardRule {
  final SpendCategory? category; // null means applicable to all categories
  final double rate; // percentage, e.g. 5.0 for 5%
  final String capPeriod; // 'monthly', 'per_transaction', 'none'
  final double? capAmount;
  final double? minSpend;

  const RewardRule({
    this.category,
    required this.rate,
    this.capPeriod = 'monthly',
    this.capAmount,
    this.minSpend,
  });

  Map<String, dynamic> toJson() => {
    'category': category?.name,
    'rate': rate,
    'capPeriod': capPeriod,
    'capAmount': capAmount,
    'minSpend': minSpend,
  };

  factory RewardRule.fromJson(Map<String, dynamic> json) {
    final catName = json['category'] as String?;
    return RewardRule(
      category: catName != null ? SpendCategory.fromKey(catName) : null,
      rate: (json['rate'] as num).toDouble(),
      capPeriod: json['capPeriod'] as String? ?? 'monthly',
      capAmount: (json['capAmount'] as num?)?.toDouble(),
      minSpend: (json['minSpend'] as num?)?.toDouble(),
    );
  }
}

class PaymentMethod {
  final String id;
  final String name;
  final PaymentMethodType type;
  final String issuer;
  final String network;
  final double baseRate;
  final List<RewardRule> rules;
  final List<SpendCategory> exclusions;
  final double? monthlyCap;
  final double currentMonthRewardUsed;
  final String ruleLastConfirmed; // ISO date e.g. '2026-11-01'
  final bool isActive;
  final String? cardLast4;
  final String? upiVpa;

  const PaymentMethod({
    required this.id,
    required this.name,
    required this.type,
    required this.issuer,
    this.network = 'Visa',
    this.baseRate = 1.0,
    this.rules = const [],
    this.exclusions = const [],
    this.monthlyCap,
    this.currentMonthRewardUsed = 0.0,
    this.ruleLastConfirmed = '',
    this.isActive = true,
    this.cardLast4,
    this.upiVpa,
  });

  bool isOutdated({int daysThreshold = 90}) {
    if (ruleLastConfirmed.isEmpty) return true;
    try {
      final confirmed = DateTime.parse(ruleLastConfirmed);
      final difference = DateTime.now().difference(confirmed).inDays;
      return difference > daysThreshold;
    } catch (_) {
      return true;
    }
  }

  int daysSinceConfirmed() {
    if (ruleLastConfirmed.isEmpty) return -1;
    try {
      final confirmed = DateTime.parse(ruleLastConfirmed);
      return DateTime.now().difference(confirmed).inDays;
    } catch (_) {
      return -1;
    }
  }

  PaymentMethod copyWith({
    String? id,
    String? name,
    PaymentMethodType? type,
    String? issuer,
    String? network,
    double? baseRate,
    List<RewardRule>? rules,
    List<SpendCategory>? exclusions,
    double? monthlyCap,
    double? currentMonthRewardUsed,
    String? ruleLastConfirmed,
    bool? isActive,
    String? cardLast4,
    String? upiVpa,
  }) {
    return PaymentMethod(
      id: id ?? this.id,
      name: name ?? this.name,
      type: type ?? this.type,
      issuer: issuer ?? this.issuer,
      network: network ?? this.network,
      baseRate: baseRate ?? this.baseRate,
      rules: rules ?? this.rules,
      exclusions: exclusions ?? this.exclusions,
      monthlyCap: monthlyCap ?? this.monthlyCap,
      currentMonthRewardUsed: currentMonthRewardUsed ?? this.currentMonthRewardUsed,
      ruleLastConfirmed: ruleLastConfirmed ?? this.ruleLastConfirmed,
      isActive: isActive ?? this.isActive,
      cardLast4: cardLast4 ?? this.cardLast4,
      upiVpa: upiVpa ?? this.upiVpa,
    );
  }

  Map<String, dynamic> toJson() => {
    'id': id,
    'name': name,
    'type': type.name,
    'issuer': issuer,
    'network': network,
    'baseRate': baseRate,
    'rules': rules.map((r) => r.toJson()).toList(),
    'exclusions': exclusions.map((e) => e.name).toList(),
    'monthlyCap': monthlyCap,
    'currentMonthRewardUsed': currentMonthRewardUsed,
    'ruleLastConfirmed': ruleLastConfirmed,
    'isActive': isActive,
    'cardLast4': cardLast4,
    'upiVpa': upiVpa,
  };

  factory PaymentMethod.fromJson(Map<String, dynamic> json) {
    return PaymentMethod(
      id: json['id'] as String,
      name: json['name'] as String,
      type: PaymentMethodType.fromString(json['type'] as String? ?? ''),
      issuer: json['issuer'] as String? ?? '',
      network: json['network'] as String? ?? 'Visa',
      baseRate: (json['baseRate'] as num?)?.toDouble() ?? 1.0,
      rules: (json['rules'] as List<dynamic>?)
              ?.map((e) => RewardRule.fromJson(e as Map<String, dynamic>))
              .toList() ??
          [],
      exclusions: (json['exclusions'] as List<dynamic>?)
              ?.map((e) => SpendCategory.fromKey(e as String?))
              .toList() ??
          [],
      monthlyCap: (json['monthlyCap'] as num?)?.toDouble(),
      currentMonthRewardUsed: (json['currentMonthRewardUsed'] as num?)?.toDouble() ?? 0.0,
      ruleLastConfirmed: json['ruleLastConfirmed'] as String? ?? '',
      isActive: json['isActive'] as bool? ?? true,
      cardLast4: json['cardLast4'] as String?,
      upiVpa: json['upiVpa'] as String?,
    );
  }
}
