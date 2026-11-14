import 'spend_category.dart';

class SavedMerchant {
  final String name;
  final String normalizedName;
  final String? vpa;
  final SpendCategory category;
  final String? mcc;
  final String lastUsedAt;
  final int usageCount;

  const SavedMerchant({
    required this.name,
    required this.normalizedName,
    this.vpa,
    required this.category,
    this.mcc,
    this.lastUsedAt = '',
    this.usageCount = 1,
  });

  Map<String, dynamic> toJson() => {
    'name': name,
    'normalizedName': normalizedName,
    'vpa': vpa,
    'category': category.name,
    'mcc': mcc,
    'lastUsedAt': lastUsedAt,
    'usageCount': usageCount,
  };

  factory SavedMerchant.fromJson(Map<String, dynamic> json) {
    return SavedMerchant(
      name: json['name'] as String,
      normalizedName: json['normalizedName'] as String,
      vpa: json['vpa'] as String?,
      category: SpendCategory.fromKey(json['category'] as String?),
      mcc: json['mcc'] as String?,
      lastUsedAt: json['lastUsedAt'] as String,
      usageCount: (json['usageCount'] as num?)?.toInt() ?? 1,
    );
  }

  SavedMerchant copyWith({
    String? name,
    String? normalizedName,
    String? vpa,
    SpendCategory? category,
    String? mcc,
    String? lastUsedAt,
    int? usageCount,
  }) {
    return SavedMerchant(
      name: name ?? this.name,
      normalizedName: normalizedName ?? this.normalizedName,
      vpa: vpa ?? this.vpa,
      category: category ?? this.category,
      mcc: mcc ?? this.mcc,
      lastUsedAt: lastUsedAt ?? this.lastUsedAt,
      usageCount: usageCount ?? this.usageCount,
    );
  }
}
