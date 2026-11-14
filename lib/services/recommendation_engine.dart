import 'dart:math' as math;
import '../models/offer.dart';
import '../models/payment_method.dart';
import '../models/recommendation.dart';
import '../models/spend_category.dart';

class RecommendationEngine {
  /// Calculates the ranked list of recommendations for a given transaction amount and category.
  /// Guaranteed to execute within 2 milliseconds on a mid-range mobile device.
  static List<Recommendation> calculate({
    required double amount,
    required SpendCategory category,
    required List<PaymentMethod> methods,
    required List<Offer> offers,
    DateTime? currentTime,
  }) {
    if (amount <= 0 || methods.isEmpty) {
      return const [];
    }

    final now = currentTime ?? DateTime.now();
    final activeMethods = methods.where((m) => m.isActive).toList();
    if (activeMethods.isEmpty) return const [];

    final results = <Recommendation>[];

    for (final method in activeMethods) {
      // Find category-specific rule or fallback to generic rule
      RewardRule? specificRule;
      RewardRule? allRule;
      for (final rule in method.rules) {
        if (rule.category == category) {
          specificRule = rule;
          break;
        }
        if (rule.category == null) {
          allRule = rule;
        }
      }
      final matchedRule = specificRule ?? allRule;

      final baseRate = method.baseRate;
      final categoryRate = matchedRule?.rate ?? baseRate;

      final totalCap = method.monthlyCap ?? double.infinity;
      final usedCap = method.currentMonthRewardUsed;
      final remainingCap = math.max(0.0, totalCap - usedCap);

      var baseReward = 0.0;
      var categoryBonus = 0.0;
      var excessAtBaseRate = 0.0;
      var isCapReached = false;
      final isExcluded = method.exclusions.contains(category);

      if (isExcluded) {
        // Category is excluded from rewards on this method (US-04)
        baseReward = 0.0;
        categoryBonus = 0.0;
        excessAtBaseRate = 0.0;
      } else if (categoryRate > baseRate) {
        final potentialBonus = (amount * categoryRate) / 100.0;
        if (potentialBonus <= remainingCap) {
          categoryBonus = _roundTwoDecimals(potentialBonus);
        } else {
          // Cap reached: bonus is limited to remaining cap
          isCapReached = true;
          categoryBonus = _roundTwoDecimals(remainingCap);

          // Calculate excess spend that drops to base rate (Requirements Section 9)
          final spendCovered =
              categoryRate > 0 ? remainingCap / (categoryRate / 100.0) : 0.0;
          final excessSpend = math.max(0.0, amount - spendCovered);
          if (excessSpend > 0 && baseRate > 0) {
            excessAtBaseRate =
                _roundTwoDecimals((excessSpend * baseRate) / 100.0);
          }
        }
      } else {
        final potentialBase = (amount * baseRate) / 100.0;
        if (potentialBase <= remainingCap) {
          baseReward = _roundTwoDecimals(potentialBase);
        } else {
          isCapReached = true;
          baseReward = _roundTwoDecimals(remainingCap);
        }
      }

      // Evaluate eligible offers
      final eligibleOffers = offers.where((offer) {
        return offer.isActive &&
            offer.methodId == method.id &&
            amount >= offer.minAmount &&
            (offer.usageCap <= 0 || offer.usageCount < offer.usageCap) &&
            (offer.categories.isEmpty || offer.categories.contains(category));
      }).toList();

      // Calculate offer values
      final offerValues = <_OfferWithValue>[];
      for (final offer in eligibleOffers) {
        double v = 0.0;
        if (offer.discountFlat != null && offer.discountFlat! > 0) {
          v = offer.discountFlat!;
        } else if (offer.discountPercent != null &&
            offer.discountPercent! > 0) {
          final calc = (amount * offer.discountPercent!) / 100.0;
          if (offer.maxDiscount != null && offer.maxDiscount! > 0) {
            v = math.min(calc, offer.maxDiscount!);
          } else {
            v = calc;
          }
        }
        offerValues.add(_OfferWithValue(offer, _roundTwoDecimals(v)));
      }

      offerValues.sort((a, b) => b.value.compareTo(a.value));

      var bestOfferReward = 0.0;
      Offer? appliedOffer;
      String? offerReason;

      if (offerValues.isNotEmpty) {
        final topOffer = offerValues.first;
        final stackable = offerValues.where((o) => o.offer.stacks).toList();
        final stackableSum =
            stackable.fold(0.0, (acc, item) => acc + item.value);

        if (!topOffer.offer.stacks && topOffer.value >= stackableSum) {
          bestOfferReward = topOffer.value;
          appliedOffer = topOffer.offer;
          offerReason =
              'Applied highest offer: "${topOffer.offer.title}" (does not stack with others)';
        } else if (stackableSum > 0) {
          bestOfferReward = stackableSum;
          appliedOffer = stackable.first.offer;
          final names = stackable.map((e) => e.offer.title).join(', ');
          offerReason = 'Stacked offers applied: $names';
        } else {
          bestOfferReward = topOffer.value;
          appliedOffer = topOffer.offer;
          offerReason = 'Offer applied: "${topOffer.offer.title}"';
        }
      }

      final cardRewardTotal = categoryBonus + excessAtBaseRate + baseReward;
      final effectiveReward =
          _roundTwoDecimals(cardRewardTotal + bestOfferReward);
      final effectiveRate =
          _roundTwoDecimals((effectiveReward / amount) * 100.0);

      final remainingAfter = totalCap.isFinite
          ? math.max(0.0, remainingCap - cardRewardTotal)
          : double.infinity;

      final breakdown = RewardBreakdown(
        baseReward: baseReward,
        categoryBonus: categoryBonus,
        excessAtBaseRate: excessAtBaseRate,
        offerReward: bestOfferReward,
        appliedOffer: appliedOffer,
        isCapReached: isCapReached,
        remainingCapBefore: totalCap.isFinite ? remainingCap : 999999.0,
        remainingCapAfter: totalCap.isFinite ? remainingAfter : 999999.0,
      );

      // Construct reason string
      var reason = '';
      if (isExcluded) {
        reason = '${category.displayName} is excluded from rewards on this card';
      } else if (specificRule != null && specificRule.rate > baseRate) {
        if (isCapReached) {
          final bonusInt = categoryBonus.toInt();
          reason = '${specificRule.rate}% ${category.displayName} bonus capped at ₹$bonusInt';
          if (excessAtBaseRate > 0) {
            reason +=
                ' + ${baseRate}% base on remaining spend (₹$excessAtBaseRate)';
          }
        } else {
          reason =
              '${specificRule.rate}% ${category.displayName} accelerated cashback';
        }
      } else {
        reason = '$baseRate% standard reward on all purchases';
      }

      if (bestOfferReward > 0 && appliedOffer != null) {
        reason += ' + ₹${bestOfferReward.toInt()} (${appliedOffer.title})';
      }

      // Warning if rules are > 90 days old (FR-11)
      String? ruleWarning;
      if (method.ruleLastConfirmed.isNotEmpty) {
        try {
          final confirmedDate = DateTime.parse(method.ruleLastConfirmed);
          final diffDays = now.difference(confirmedDate).inDays;
          if (diffDays > 90) {
            ruleWarning =
                'Card rules last verified $diffDays days ago. Rates may have changed.';
          }
        } catch (_) {}
      }

      results.add(
        Recommendation(
          method: method,
          effectiveReward: effectiveReward,
          effectiveRate: effectiveRate,
          breakdown: breakdown,
          reason: reason,
          offerReason: offerReason,
          ruleWarning: ruleWarning,
          isTopPick: false,
        ),
      );
    }

    // Rank by:
    // 1. effectiveReward (descending)
    // 2. Tie-breaker: higher remaining cap (Requirements Section 6 FR-6)
    // 3. Tie-breaker: baseRate (descending)
    results.sort((a, b) {
      final rewardDiff = b.effectiveReward.compareTo(a.effectiveReward);
      if (rewardDiff != 0) return rewardDiff;

      final capDiff = b.breakdown.remainingCapBefore
          .compareTo(a.breakdown.remainingCapBefore);
      if (capDiff != 0) return capDiff;

      return b.method.baseRate.compareTo(a.method.baseRate);
    });

    if (results.isNotEmpty) {
      final top = results[0];
      results[0] = Recommendation(
        method: top.method,
        effectiveReward: top.effectiveReward,
        effectiveRate: top.effectiveRate,
        breakdown: top.breakdown,
        reason: top.reason,
        offerReason: top.offerReason,
        ruleWarning: top.ruleWarning,
        isTopPick: true,
      );
    }

    return results;
  }

  static double _roundTwoDecimals(double value) {
    return (value * 100.0).roundToDouble() / 100.0;
  }
}

class _OfferWithValue {
  final Offer offer;
  final double value;
  const _OfferWithValue(this.offer, this.value);
}
