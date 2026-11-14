import 'offer.dart';
import 'payment_method.dart';

class RewardBreakdown {
  final double baseReward;
  final double categoryBonus;
  final double excessAtBaseRate;
  final double offerReward;
  final Offer? appliedOffer;
  final bool isCapReached;
  final double remainingCapBefore;
  final double remainingCapAfter;

  const RewardBreakdown({
    required this.baseReward,
    required this.categoryBonus,
    required this.excessAtBaseRate,
    required this.offerReward,
    this.appliedOffer,
    this.isCapReached = false,
    this.remainingCapBefore = 0.0,
    this.remainingCapAfter = 0.0,
  });
}

class Recommendation {
  final PaymentMethod method;
  final double effectiveReward;
  final double effectiveRate;
  final RewardBreakdown breakdown;
  final String reason;
  final String? offerReason;
  final String? ruleWarning;
  final bool isTopPick;

  const Recommendation({
    required this.method,
    required this.effectiveReward,
    required this.effectiveRate,
    required this.breakdown,
    required this.reason,
    this.offerReason,
    this.ruleWarning,
    this.isTopPick = false,
  });
}
