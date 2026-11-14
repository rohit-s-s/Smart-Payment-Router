import { PaymentMethod, Offer, SavedMerchant } from '../types';

export const DEFAULT_PAYMENT_METHODS: PaymentMethod[] = [
  {
    id: 'hdfc-millennia',
    name: 'HDFC Millennia Credit Card',
    type: 'credit_card',
    issuer: 'HDFC Bank',
    network: 'Mastercard',
    baseRate: 1.0,
    rules: [
      { category: 'dining', rate: 5.0, capPeriod: 'monthly', capAmount: 1000 },
      { category: 'shopping', rate: 5.0, capPeriod: 'monthly', capAmount: 1000 },
      { category: 'entertainment', rate: 5.0, capPeriod: 'monthly', capAmount: 1000 },
      { category: 'all', rate: 1.0 }
    ],
    monthlyCap: 1000,
    currentMonthRewardUsed: 200,
    ruleLastConfirmed: '2026-10-15',
    isActive: true,
    colorGradient: 'from-blue-700 via-indigo-800 to-slate-900',
    cardLast4: '4192'
  },
  {
    id: 'sbi-cashback',
    name: 'SBI Cashback Credit Card',
    type: 'credit_card',
    issuer: 'SBI Card',
    network: 'Visa',
    baseRate: 1.0,
    rules: [
      { category: 'shopping', rate: 5.0, capPeriod: 'monthly', capAmount: 5000 },
      { category: 'groceries', rate: 5.0, capPeriod: 'monthly', capAmount: 5000 },
      { category: 'utilities', rate: 5.0, capPeriod: 'monthly', capAmount: 5000 },
      { category: 'dining', rate: 5.0, capPeriod: 'monthly', capAmount: 5000 },
      { category: 'all', rate: 1.0 }
    ],
    monthlyCap: 5000,
    currentMonthRewardUsed: 850,
    ruleLastConfirmed: '2026-11-01',
    isActive: true,
    colorGradient: 'from-sky-600 via-blue-700 to-indigo-950',
    cardLast4: '8831'
  },
  {
    id: 'icici-amazon-pay',
    name: 'Amazon Pay ICICI Credit Card',
    type: 'credit_card',
    issuer: 'ICICI Bank',
    network: 'Visa',
    baseRate: 1.0,
    rules: [
      { category: 'shopping', rate: 5.0 }, // 5% unlimited on Amazon
      { category: 'utilities', rate: 2.0 },
      { category: 'dining', rate: 2.0 },
      { category: 'all', rate: 1.0 }
    ],
    monthlyCap: undefined, // Unlimited!
    currentMonthRewardUsed: 340,
    ruleLastConfirmed: '2026-09-10', // >60 days old
    isActive: true,
    colorGradient: 'from-amber-600 via-orange-700 to-stone-900',
    cardLast4: '1049'
  },
  {
    id: 'axis-atlas',
    name: 'Axis Atlas Credit Card',
    type: 'credit_card',
    issuer: 'Axis Bank',
    network: 'Visa',
    baseRate: 2.0,
    rules: [
      { category: 'travel', rate: 10.0, capPeriod: 'monthly', capAmount: 2500 },
      { category: 'dining', rate: 4.0, capPeriod: 'monthly', capAmount: 2500 },
      { category: 'all', rate: 2.0 }
    ],
    monthlyCap: 2500,
    currentMonthRewardUsed: 400,
    ruleLastConfirmed: '2026-10-20',
    isActive: true,
    colorGradient: 'from-purple-800 via-violet-900 to-slate-950',
    cardLast4: '9920'
  },
  {
    id: 'iocl-rupay',
    name: 'IndianOil RuPay Credit Card',
    type: 'credit_card',
    issuer: 'Axis Bank / IndianOil',
    network: 'RuPay',
    baseRate: 1.0,
    rules: [
      { category: 'fuel', rate: 4.0, capPeriod: 'monthly', capAmount: 250 },
      { category: 'groceries', rate: 1.0 },
      { category: 'all', rate: 1.0 }
    ],
    monthlyCap: 250,
    currentMonthRewardUsed: 75,
    ruleLastConfirmed: '2026-08-01', // >90 days old to test FR-11 warning!
    isActive: true,
    colorGradient: 'from-red-600 via-rose-700 to-neutral-900',
    cardLast4: '6312'
  },
  {
    id: 'gpay-upi',
    name: 'Google Pay (UPI / Savings Account)',
    type: 'upi',
    issuer: 'NPCI / HDFC Bank',
    network: 'UPI',
    baseRate: 0.5,
    rules: [
      { category: 'all', rate: 0.5, capPeriod: 'monthly', capAmount: 100 }
    ],
    monthlyCap: 100,
    currentMonthRewardUsed: 15,
    ruleLastConfirmed: '2026-11-05',
    isActive: true,
    colorGradient: 'from-emerald-600 via-teal-700 to-slate-900',
    upiVpa: 'user@okhdfcbank'
  }
];

export const DEFAULT_OFFERS: Offer[] = [
  {
    id: 'offer-swiggy-zomato',
    methodId: 'hdfc-millennia',
    title: 'Weekend Dining Bonanza: 10% Extra Cashback',
    description: 'Get extra 10% cashback up to ₹150 on dining orders over ₹400 with HDFC cards',
    categories: ['dining'],
    minAmount: 400,
    discountPercent: 10,
    maxDiscount: 150,
    startDate: '2026-11-01',
    endDate: '2026-11-30',
    usageCap: 4,
    usageCount: 1,
    stacks: true,
    requiresActivation: true,
    isActive: true
  },
  {
    id: 'offer-fuel-surcharge',
    methodId: 'iocl-rupay',
    title: 'Fuel Cashback Boost: Flat ₹30 Back',
    description: 'Flat ₹30 additional reward on fuel transactions of ₹500 or more',
    categories: ['fuel'],
    minAmount: 500,
    discountFlat: 30,
    startDate: '2026-11-01',
    endDate: '2026-12-31',
    usageCap: 2,
    usageCount: 0,
    stacks: true,
    requiresActivation: false,
    isActive: true
  },
  {
    id: 'offer-bill-pay-sbi',
    methodId: 'sbi-cashback',
    title: 'SBI Smart Utility Booster: Extra ₹50',
    description: 'Flat ₹50 cashback on Utility bill payments above ₹1,000',
    categories: ['utilities'],
    minAmount: 1000,
    discountFlat: 50,
    startDate: '2026-11-01',
    endDate: '2026-11-25',
    usageCap: 2,
    usageCount: 0,
    stacks: false,
    requiresActivation: true,
    isActive: true
  }
];

export const DEFAULT_SAVED_MERCHANTS: SavedMerchant[] = [
  {
    name: 'Swiggy',
    normalizedName: 'swiggy',
    vpa: 'swiggy@icici',
    category: 'dining',
    mcc: '5812',
    lastUsedAt: '2026-11-12T14:30:00Z',
    usageCount: 14
  },
  {
    name: 'Blinkit Quick Groceries',
    normalizedName: 'blinkit quick groceries',
    vpa: 'blinkit@kotak',
    category: 'groceries',
    mcc: '5411',
    lastUsedAt: '2026-11-13T10:15:00Z',
    usageCount: 8
  },
  {
    name: 'HPCL Auto Fuel Care',
    normalizedName: 'hpcl auto fuel care',
    vpa: 'hpclpump@upi',
    category: 'fuel',
    mcc: '5541',
    lastUsedAt: '2026-11-10T18:45:00Z',
    usageCount: 5
  },
  {
    name: 'Uber India',
    normalizedName: 'uber india',
    vpa: 'uber.rides@axis',
    category: 'travel',
    mcc: '4121',
    lastUsedAt: '2026-11-09T09:20:00Z',
    usageCount: 11
  },
  {
    name: 'BESCOM Electricity Bill',
    normalizedName: 'bescom electricity bill',
    vpa: 'bescom@billdesk',
    category: 'utilities',
    mcc: '4900',
    lastUsedAt: '2026-11-05T12:00:00Z',
    usageCount: 6
  },
  {
    name: 'BookMyShow Movies',
    normalizedName: 'bookmyshow movies',
    vpa: 'bookmyshow@hdfcbank',
    category: 'entertainment',
    mcc: '7922',
    lastUsedAt: '2026-11-08T20:00:00Z',
    usageCount: 4
  }
];
