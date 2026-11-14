import 'spend_category.dart';

class PaymentLog {
  final String id;
  final String timestamp;
  final double amount;
  final SpendCategory category;
  final String merchantName;
  final String payeeAddress;
  final String source; // 'scan' or 'manual'
  final String recommendedMethodId;
  final String recommendedMethodName;
  final String chosenMethodId;
  final String chosenMethodName;
  final double expectedReward;
  final double? actualReward;
  final String? offerApplied;

  const PaymentLog({
    required this.id,
    required this.timestamp,
    required this.amount,
    required this.category,
    required this.merchantName,
    this.payeeAddress = '',
    this.source = 'scan',
    required this.recommendedMethodId,
    required this.recommendedMethodName,
    required this.chosenMethodId,
    required this.chosenMethodName,
    required this.expectedReward,
    this.actualReward,
    this.offerApplied,
  });

  // Convenient property aliases
  String get methodName => chosenMethodName;
  double get rewardEarned => expectedReward;
  String get date => timestamp;
  String? get payeeName => merchantName.isNotEmpty ? merchantName : null;

  Map<String, dynamic> toJson() => {
    'id': id,
    'timestamp': timestamp,
    'amount': amount,
    'category': category.name,
    'merchantName': merchantName,
    'payeeAddress': payeeAddress,
    'source': source,
    'recommendedMethodId': recommendedMethodId,
    'recommendedMethodName': recommendedMethodName,
    'chosenMethodId': chosenMethodId,
    'chosenMethodName': chosenMethodName,
    'expectedReward': expectedReward,
    'actualReward': actualReward,
    'offerApplied': offerApplied,
  };

  factory PaymentLog.fromJson(Map<String, dynamic> json) {
    return PaymentLog(
      id: json['id'] as String,
      timestamp: json['timestamp'] as String? ?? '',
      amount: (json['amount'] as num).toDouble(),
      category: SpendCategory.fromKey(json['category'] as String?),
      merchantName: json['merchantName'] as String? ?? 'Merchant',
      payeeAddress: json['payeeAddress'] as String? ?? '',
      source: json['source'] as String? ?? 'scan',
      recommendedMethodId: json['recommendedMethodId'] as String? ?? '',
      recommendedMethodName: json['recommendedMethodName'] as String? ?? '',
      chosenMethodId: json['chosenMethodId'] as String? ?? '',
      chosenMethodName: json['chosenMethodName'] as String? ?? '',
      expectedReward: (json['expectedReward'] as num?)?.toDouble() ?? 0.0,
      actualReward: (json['actualReward'] as num?)?.toDouble(),
      offerApplied: json['offerApplied'] as String?,
    );
  }
}
