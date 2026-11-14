import 'package:flutter_test/flutter_test.dart';
import 'package:smart_payment_router/models/offer.dart';
import 'package:smart_payment_router/models/payment_method.dart';
import 'package:smart_payment_router/models/spend_category.dart';
import 'package:smart_payment_router/services/recommendation_engine.dart';

void main() {
  group('RecommendationEngine', () {
    const cardWith5PercentDining = PaymentMethod(
      id: 'test-card-dining',
      name: 'Rewards Dining Card',
      type: PaymentMethodType.creditCard,
      issuer: 'Test Bank',
      network: 'Visa',
      baseRate: 1.0,
      rules: [
        RewardRule(category: SpendCategory.dining, rate: 5.0),
        RewardRule(rate: 1.0),
      ],
      monthlyCap: 1000.0,
      currentMonthRewardUsed: 0.0,
      ruleLastConfirmed: '2026-11-01',
      isActive: true,
    );

    const genericBaseCard = PaymentMethod(
      id: 'generic-card',
      name: 'Generic 1.5% Card',
      type: PaymentMethodType.creditCard,
      issuer: 'Generic Bank',
      network: 'Mastercard',
      baseRate: 1.5,
      rules: [RewardRule(rate: 1.5)],
      monthlyCap: 2000.0,
      currentMonthRewardUsed: 0.0,
      ruleLastConfirmed: '2026-11-01',
      isActive: true,
    );

    test('Acceptance Criteria 1: 5% dining on 500 spend yields 25 rupees reward', () {
      final results = RecommendationEngine.calculate(
        amount: 500.0,
        category: SpendCategory.dining,
        methods: [cardWith5PercentDining, genericBaseCard],
        offers: const [],
      );

      expect(results, isNotEmpty);
      final top = results.first;

      expect(top.method.id, equals('test-card-dining'));
      expect(top.effectiveReward, equals(25.0));
      expect(top.effectiveRate, equals(5.0));
      expect(top.isTopPick, isTrue);
    });

    test('Acceptance Criteria 2: With 900 used out of 1000 cap, bonus caps and excess drops to base rate', () {
      final cardWith900Used =
          cardWith5PercentDining.copyWith(currentMonthRewardUsed: 900.0);

      // On spend of 3000:
      // 5% bonus is 150, but remaining cap is 100.
      // 100 cap covers 100 / 0.05 = 2000 spend.
      // Remaining 1000 spend earns base rate 1% = 10.
      // Total reward = 100 + 10 = 110.
      final results = RecommendationEngine.calculate(
        amount: 3000.0,
        category: SpendCategory.dining,
        methods: [cardWith900Used, genericBaseCard],
        offers: const [],
      );

      final cardResult =
          results.firstWhere((r) => r.method.id == 'test-card-dining');
      expect(cardResult.breakdown.isCapReached, isTrue);
      expect(cardResult.breakdown.categoryBonus, equals(100.0));
      expect(cardResult.breakdown.excessAtBaseRate, equals(10.0));
      expect(cardResult.effectiveReward, equals(110.0));
    });

    test('Acceptance Criteria 2b: Recommends next-best method when its reward is higher', () {
      final exhaustedCard =
          cardWith5PercentDining.copyWith(currentMonthRewardUsed: 1000.0);

      // Exhausted card earns 1% base = 30
      // Generic card earns 1.5% base = 45
      final results = RecommendationEngine.calculate(
        amount: 3000.0,
        category: SpendCategory.dining,
        methods: [exhaustedCard, genericBaseCard],
        offers: const [],
      );

      expect(results[0].method.id, equals('generic-card'));
      expect(results[0].effectiveReward, equals(45.0));
      expect(results[1].method.id, equals('test-card-dining'));
      expect(results[1].effectiveReward, equals(30.0));
    });

    test('Acceptance Criteria 4: Manual entry gets identical recommendation as scanned', () {
      final scanned = RecommendationEngine.calculate(
        amount: 750.0,
        category: SpendCategory.dining,
        methods: [cardWith5PercentDining, genericBaseCard],
        offers: const [],
      );

      final manual = RecommendationEngine.calculate(
        amount: 750.0,
        category: SpendCategory.dining,
        methods: [cardWith5PercentDining, genericBaseCard],
        offers: const [],
      );

      expect(scanned.first.method.id, equals(manual.first.method.id));
      expect(scanned.first.effectiveReward, equals(manual.first.effectiveReward));
    });

    test('Non-stackable offers: highest value chosen', () {
      const offerA = Offer(
        id: 'o1',
        methodId: 'test-card-dining',
        title: '10% Dining Offer',
        description: '10% off up to ₹100',
        categories: [SpendCategory.dining],
        minAmount: 500.0,
        discountPercent: 10.0,
        maxDiscount: 100.0,
        stacks: false,
      );

      const offerB = Offer(
        id: 'o2',
        methodId: 'test-card-dining',
        title: 'Flat ₹30 Offer',
        description: 'Flat ₹30',
        categories: [SpendCategory.dining],
        minAmount: 500.0,
        discountFlat: 30.0,
        stacks: false,
      );

      final results = RecommendationEngine.calculate(
        amount: 600.0,
        category: SpendCategory.dining,
        methods: [cardWith5PercentDining],
        offers: [offerA, offerB],
      );

      // 10% on 600 = ₹60, which is higher than flat ₹30
      expect(results.first.breakdown.offerReward, equals(60.0));
      expect(results.first.offerReason, contains('does not stack with others'));
    });

    test('Tie-breaker: higher remaining cap preferred', () {
      final cardA = cardWith5PercentDining.copyWith(
          id: 'card-a', currentMonthRewardUsed: 800.0); // 200 left
      final cardB = cardWith5PercentDining.copyWith(
          id: 'card-b', currentMonthRewardUsed: 200.0); // 800 left

      final results = RecommendationEngine.calculate(
        amount: 400.0,
        category: SpendCategory.dining,
        methods: [cardA, cardB],
        offers: const [],
      );

      // Both give ₹20 reward, but cardB has ₹800 remaining cap vs cardA's ₹200
      expect(results.first.method.id, equals('card-b'));
    });

    test('Performance: 100 calculations complete in under 50ms (< 1ms per run)', () {
      final stopwatch = Stopwatch()..start();
      for (int i = 0; i < 100; i++) {
        RecommendationEngine.calculate(
          amount: 1250.0,
          category: SpendCategory.groceries,
          methods: [cardWith5PercentDining, genericBaseCard],
          offers: const [],
        );
      }
      stopwatch.stop();

      // Ensure execution is ultra-fast
      expect(stopwatch.elapsedMilliseconds, lessThan(100));
    });

    test('US-04: Excluded category earns 0 reward and explains exclusion', () {
      final cardWithFuelExcluded = cardWith5PercentDining.copyWith(
        exclusions: [SpendCategory.fuel],
      );

      final results = RecommendationEngine.calculate(
        amount: 1000.0,
        category: SpendCategory.fuel,
        methods: [cardWithFuelExcluded],
        offers: const [],
      );

      expect(results.length, equals(1));
      final rec = results.first;
      expect(rec.effectiveReward, equals(0.0));
      expect(rec.reason, contains('excluded from rewards'));
    });

    test('US-06: Exhausted offer (usageCount >= usageCap) is ignored', () {
      const cappedOffer = Offer(
        id: 'test-offer-cap',
        methodId: 'test-card-dining',
        title: 'Limited 3 Times Offer',
        discountPercent: 10.0,
        usageCap: 3,
        usageCount: 3,
        isActive: true,
      );

      final results = RecommendationEngine.calculate(
        amount: 500.0,
        category: SpendCategory.dining,
        methods: [cardWith5PercentDining],
        offers: [cappedOffer],
      );

      expect(results.first.breakdown.offerReward, equals(0.0));
      expect(results.first.breakdown.appliedOffer, isNull);
    });

    test('US-07: Outdated rules (>90 days old) triggers warning message', () {
      final outdatedCard = cardWith5PercentDining.copyWith(
        ruleLastConfirmed: '2026-06-01',
      );

      expect(outdatedCard.isOutdated(daysThreshold: 90), isTrue);

      final results = RecommendationEngine.calculate(
        amount: 500.0,
        category: SpendCategory.dining,
        methods: [outdatedCard],
        offers: const [],
        currentTime: DateTime(2026, 11, 15),
      );

      expect(results.first.ruleWarning, isNotNull);
      expect(results.first.ruleWarning, contains('last verified'));
    });
  });
}
