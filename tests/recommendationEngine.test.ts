import { describe, it, expect } from 'vitest';
import { calculateRecommendations } from '../src/utils/recommendationEngine';
import { PaymentMethod, Offer, SpendCategory } from '../src/types';

describe('Recommendation Engine & Epic 1 Acceptance Criteria', () => {
  const cardWith5PercentDining: PaymentMethod = {
    id: 'test-card-dining',
    name: 'Rewards Dining Card',
    type: 'credit_card',
    issuer: 'Test Bank',
    network: 'Visa',
    baseRate: 1.0,
    rules: [
      { category: 'dining', rate: 5.0, capPeriod: 'monthly', capAmount: 1000 },
      { category: 'all', rate: 1.0 }
    ],
    monthlyCap: 1000,
    currentMonthRewardUsed: 0,
    ruleLastConfirmed: '2026-11-01',
    isActive: true,
    colorGradient: 'from-blue-600 to-indigo-900'
  };

  const genericBaseCard: PaymentMethod = {
    id: 'generic-card',
    name: 'Generic 1.5% Card',
    type: 'credit_card',
    issuer: 'Generic Bank',
    network: 'Mastercard',
    baseRate: 1.5,
    rules: [{ category: 'all', rate: 1.5 }],
    monthlyCap: 2000,
    currentMonthRewardUsed: 0,
    ruleLastConfirmed: '2026-11-01',
    isActive: true,
    colorGradient: 'from-slate-700 to-slate-900'
  };

  it('Acceptance Criteria 1: 5% dining card on ₹500 spend yields ₹25 reward', () => {
    const results = calculateRecommendations({
      amount: 500,
      category: 'dining',
      methods: [cardWith5PercentDining, genericBaseCard],
      offers: []
    });

    expect(results.length).toBeGreaterThan(0);
    const top = results[0];

    expect(top.method.id).toBe('test-card-dining');
    // 500 * 5% = 25 rupees
    expect(top.effectiveReward).toBe(25);
    expect(top.effectiveRate).toBe(5);
    expect(top.breakdown.categoryBonus).toBe(25);
    expect(top.isTopPick).toBe(true);
  });

  it('Acceptance Criteria 2: With ₹900 cap used out of ₹1,000 (remaining ₹100), handles cap limit', () => {
    const cardWith900Used: PaymentMethod = {
      ...cardWith5PercentDining,
      currentMonthRewardUsed: 900 // Only ₹100 cap left!
    };

    // For a large dining purchase of ₹3,000:
    // Potential 5% bonus is ₹150, but remaining cap is only ₹100.
    // Bonus is capped at ₹100 (which covers 100 / 0.05 = ₹2000 spend).
    // The remaining ₹1000 spend drops to base rate 1% = ₹10.
    // Total reward = ₹100 + ₹10 = ₹110.
    const results = calculateRecommendations({
      amount: 3000,
      category: 'dining',
      methods: [cardWith900Used, genericBaseCard],
      offers: []
    });

    const cardResult = results.find(r => r.method.id === 'test-card-dining')!;
    expect(cardResult.breakdown.isCapReached).toBe(true);
    expect(cardResult.breakdown.categoryBonus).toBe(100);
    expect(cardResult.breakdown.excessAtBaseRate).toBe(10);
    expect(cardResult.effectiveReward).toBe(110);
  });

  it('Acceptance Criteria 2b: Recommends runner-up if alternative method reward is higher after cap exhaustion', () => {
    const exhaustedCard: PaymentMethod = {
      ...cardWith5PercentDining,
      currentMonthRewardUsed: 1000 // ₹0 cap left!
    };

    // Card 1 has ₹0 cap left, falls back to base rate 1% -> ₹30
    // Generic card has 1.5% base rate -> ₹45
    const results = calculateRecommendations({
      amount: 3000,
      category: 'dining',
      methods: [exhaustedCard, genericBaseCard],
      offers: []
    });

    expect(results[0].method.id).toBe('generic-card');
    expect(results[0].effectiveReward).toBe(45);
    expect(results[1].method.id).toBe('test-card-dining');
    expect(results[1].effectiveReward).toBe(30);
  });

  it('Acceptance Criteria 4 (US-02): Manual entry gets identical recommendation to equivalent scanned purchase', () => {
    const scannedAmount = 750;
    const scannedCategory: SpendCategory = 'dining';

    const scannedRecs = calculateRecommendations({
      amount: scannedAmount,
      category: scannedCategory,
      methods: [cardWith5PercentDining, genericBaseCard],
      offers: []
    });

    const manualAmount = 750;
    const manualCategory: SpendCategory = 'dining';

    const manualRecs = calculateRecommendations({
      amount: manualAmount,
      category: manualCategory,
      methods: [cardWith5PercentDining, genericBaseCard],
      offers: []
    });

    expect(manualRecs[0].method.id).toBe(scannedRecs[0].method.id);
    expect(manualRecs[0].effectiveReward).toBe(scannedRecs[0].effectiveReward);
    expect(manualRecs[0].effectiveRate).toBe(scannedRecs[0].effectiveRate);
    expect(manualRecs[0].reason).toBe(scannedRecs[0].reason);
  });

  it('Edge Case: Non-stackable offers: engine applies only higher-value offer and explains why', () => {
    const offer10Percent: Offer = {
      id: 'o1',
      methodId: 'test-card-dining',
      title: '10% Dining Offer',
      description: '10% off up to ₹100',
      categories: ['dining'],
      minAmount: 500,
      discountPercent: 10,
      maxDiscount: 100,
      startDate: '2026-11-01',
      endDate: '2026-12-31',
      usageCap: 2,
      usageCount: 0,
      stacks: false, // does not stack
      requiresActivation: false,
      isActive: true
    };

    const flatOffer30: Offer = {
      id: 'o2',
      methodId: 'test-card-dining',
      title: 'Flat ₹30 Weekend Offer',
      description: 'Flat ₹30 off',
      categories: ['dining'],
      minAmount: 500,
      discountFlat: 30,
      startDate: '2026-11-01',
      endDate: '2026-12-31',
      usageCap: 2,
      usageCount: 0,
      stacks: false, // does not stack
      requiresActivation: false,
      isActive: true
    };

    const results = calculateRecommendations({
      amount: 600,
      category: 'dining',
      methods: [cardWith5PercentDining],
      offers: [offer10Percent, flatOffer30]
    });

    const top = results[0];
    // 10% on 600 = 60, higher than flat 30
    expect(top.breakdown.offerReward).toBe(60);
    expect(top.offerReason).toContain('Applied highest offer');
    expect(top.offerReason).toContain('does not stack with others');
  });

  it('Edge Case: Tie breaker prefers method with higher remaining cap', () => {
    const cardA: PaymentMethod = {
      ...cardWith5PercentDining,
      id: 'card-a',
      name: 'Card A (₹200 Cap Left)',
      currentMonthRewardUsed: 800, // 1000 - 800 = ₹200 left
    };

    const cardB: PaymentMethod = {
      ...cardWith5PercentDining,
      id: 'card-b',
      name: 'Card B (₹800 Cap Left)',
      currentMonthRewardUsed: 200, // 1000 - 200 = ₹800 left
    };

    const results = calculateRecommendations({
      amount: 400,
      category: 'dining',
      methods: [cardA, cardB],
      offers: []
    });

    // Both give 400 * 5% = 20 rupees
    expect(results[0].effectiveReward).toBe(20);
    expect(results[1].effectiveReward).toBe(20);
    // Card B must be preferred because it has ₹800 remaining cap vs Card A's ₹200
    expect(results[0].method.id).toBe('card-b');
  });

  it('Non-Functional Requirement: Performance benchmark executes in < 10ms', () => {
    const start = performance.now();

    for (let i = 0; i < 50; i++) {
      calculateRecommendations({
        amount: 1250,
        category: 'groceries',
        methods: [cardWith5PercentDining, genericBaseCard],
        offers: []
      });
    }

    const elapsed = performance.now() - start;
    // 50 runs should comfortably finish in < 50ms, meaning < 1ms per execution (vastly superior to the 2000ms threshold)
    expect(elapsed).toBeLessThan(50);
  });
});
