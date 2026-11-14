import 'spend_category.dart';

class Offer {
  final String id;
  final String methodId;
  final String title;
  final String description;
  final List<SpendCategory> categories; // empty means applicable to all categories
  final double minAmount;
  final double? discountPercent;
  final double? discountFlat;
  final double? maxDiscount;
  final String startDate;
  final String endDate;
  final int usageCap;
  final int usageCount;
  final bool stacks;
  final bool requiresActivation;
  final bool isActive;

  const Offer({
    required this.id,
    required this.methodId,
    required this.title,
    this.description = '',
    this.categories = const [],
    this.minAmount = 0.0,
    this.discountPercent,
    this.discountFlat,
    this.maxDiscount,
    this.startDate = '',
    this.endDate = '',
    this.usageCap = 10,
    this.usageCount = 0,
    this.stacks = true,
    this.requiresActivation = false,
    this.isActive = true,
  });

  bool get isExhausted => usageCap > 0 && usageCount >= usageCap;

  Offer copyWith({
    String? id,
    String? methodId,
    String? title,
    String? description,
    List<SpendCategory>? categories,
    double? minAmount,
    double? discountPercent,
    double? discountFlat,
    double? maxDiscount,
    String? startDate,
    String? endDate,
    int? usageCap,
    int? usageCount,
    bool? stacks,
    bool? requiresActivation,
    bool? isActive,
  }) {
    return Offer(
      id: id ?? this.id,
      methodId: methodId ?? this.methodId,
      title: title ?? this.title,
      description: description ?? this.description,
      categories: categories ?? this.categories,
      minAmount: minAmount ?? this.minAmount,
      discountPercent: discountPercent ?? this.discountPercent,
      discountFlat: discountFlat ?? this.discountFlat,
      maxDiscount: maxDiscount ?? this.maxDiscount,
      startDate: startDate ?? this.startDate,
      endDate: endDate ?? this.endDate,
      usageCap: usageCap ?? this.usageCap,
      usageCount: usageCount ?? this.usageCount,
      stacks: stacks ?? this.stacks,
      requiresActivation: requiresActivation ?? this.requiresActivation,
      isActive: isActive ?? this.isActive,
    );
  }

  Map<String, dynamic> toJson() => {
    'id': id,
    'methodId': methodId,
    'title': title,
    'description': description,
    'categories': categories.map((c) => c.name).toList(),
    'minAmount': minAmount,
    'discountPercent': discountPercent,
    'discountFlat': discountFlat,
    'maxDiscount': maxDiscount,
    'startDate': startDate,
    'endDate': endDate,
    'usageCap': usageCap,
    'usageCount': usageCount,
    'stacks': stacks,
    'requiresActivation': requiresActivation,
    'isActive': isActive,
  };

  factory Offer.fromJson(Map<String, dynamic> json) {
    return Offer(
      id: json['id'] as String,
      methodId: json['methodId'] as String,
      title: json['title'] as String,
      description: json['description'] as String? ?? '',
      categories: (json['categories'] as List<dynamic>?)
              ?.map((e) => SpendCategory.fromKey(e as String?))
              .toList() ??
          [],
      minAmount: (json['minAmount'] as num?)?.toDouble() ?? 0.0,
      discountPercent: (json['discountPercent'] as num?)?.toDouble(),
      discountFlat: (json['discountFlat'] as num?)?.toDouble(),
      maxDiscount: (json['maxDiscount'] as num?)?.toDouble(),
      startDate: json['startDate'] as String? ?? '',
      endDate: json['endDate'] as String? ?? '',
      usageCap: (json['usageCap'] as num?)?.toInt() ?? 10,
      usageCount: (json['usageCount'] as num?)?.toInt() ?? 0,
      stacks: json['stacks'] as bool? ?? true,
      requiresActivation: json['requiresActivation'] as bool? ?? false,
      isActive: json['isActive'] as bool? ?? true,
    );
  }
}
