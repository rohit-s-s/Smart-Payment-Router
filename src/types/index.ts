// Spend categories as specified in Epic 1 (US-03)
export type SpendCategory = 
  | 'dining' 
  | 'groceries' 
  | 'fuel' 
  | 'travel' 
  | 'utilities' 
  | 'shopping' 
  | 'entertainment' 
  | 'other';

export const SPEND_CATEGORIES: { id: SpendCategory; label: string; icon: string; description: string; color: string }[] = [
  { id: 'dining', label: 'Dining & Food', icon: 'Utensils', description: 'Restaurants, cafes, food delivery (Swiggy, Zomato)', color: 'from-amber-500 to-orange-600' },
  { id: 'groceries', label: 'Groceries', icon: 'ShoppingCart', description: 'Supermarkets, Blinkit, Zepto, daily essentials', color: 'from-emerald-500 to-green-600' },
  { id: 'fuel', label: 'Fuel', icon: 'Fuel', description: 'Petrol pumps, EV charging, HPCL, IOCL, Shell', color: 'from-red-500 to-rose-600' },
  { id: 'travel', label: 'Travel & Transport', icon: 'Plane', description: 'Flights, trains, cabs (Uber, Ola), hotels', color: 'from-blue-500 to-indigo-600' },
  { id: 'utilities', label: 'Utilities & Bills', icon: 'Zap', description: 'Electricity, water, gas, broadband, mobile recharge', color: 'from-yellow-500 to-amber-600' },
  { id: 'shopping', label: 'Shopping', icon: 'ShoppingBag', description: 'Clothing, electronics, retail, Amazon, Flipkart', color: 'from-purple-500 to-violet-600' },
  { id: 'entertainment', label: 'Entertainment', icon: 'Film', description: 'Movies, streaming, events, gaming', color: 'from-pink-500 to-rose-500' },
  { id: 'other', label: 'Other', icon: 'HelpCircle', description: 'General stores, services, miscellaneous spend', color: 'from-slate-500 to-slate-700' },
];

export type PaymentMethodType = 'credit_card' | 'debit_card' | 'upi';

export interface RewardRule {
  category: SpendCategory | 'all';
  rate: number; // percentage (e.g. 5.0 for 5%)
  capPeriod?: 'monthly' | 'transaction' | 'none';
  capAmount?: number; // max reward ₹ in period
  minSpend?: number; // min purchase ₹
  exclusions?: string[];
}

export interface PaymentMethod {
  id: string;
  name: string;
  type: PaymentMethodType;
  issuer: string;
  network: string; // Visa, Mastercard, RuPay, UPI
  baseRate: number; // base reward percentage
  rules: RewardRule[];
  monthlyCap?: number; // overall monthly reward cap in ₹
  currentMonthRewardUsed: number; // ₹ already rewarded this month
  ruleLastConfirmed: string; // ISO date
  isActive: boolean;
  colorGradient: string;
  cardLast4?: string;
  upiVpa?: string;
}

export interface Offer {
  id: string;
  methodId: string;
  title: string;
  description: string;
  categories: (SpendCategory | 'all')[];
  minAmount: number;
  discountPercent?: number;
  discountFlat?: number;
  maxDiscount?: number;
  startDate: string;
  endDate: string;
  usageCap: number; // times usable
  usageCount: number;
  stacks: boolean; // whether stacks with card cashback
  requiresActivation: boolean; // FR-8 alert requirement
  isActive: boolean;
}

export interface ParsedUPI {
  rawString: string;
  isUPI: boolean;
  isNonUPIUrl?: boolean;
  payeeAddress?: string; // pa
  payeeName?: string; // pn
  amount?: number; // am
  mcc?: string; // mc
  transactionRef?: string; // tr
  transactionNote?: string; // tn
  currency?: string; // cu
  suggestedCategory?: SpendCategory;
  categorySource?: 'mcc' | 'merchant_history' | 'manual' | 'default';
  mccDescription?: string;
}

export interface PaymentDraft {
  source: 'scan' | 'manual';
  payeeName: string;
  payeeAddress: string;
  amount: number | '';
  category: SpendCategory;
  categorySource: 'mcc' | 'merchant_history' | 'manual' | 'default';
  mcc?: string;
  rawQR?: string;
  isNonUPIUrl?: boolean;
}

export interface RewardBreakdown {
  baseReward: number;
  categoryBonus: number;
  excessAtBaseRate: number;
  offerReward: number;
  appliedOffer?: Offer;
  isCapReached: boolean;
  remainingCapBefore: number;
  remainingCapAfter: number;
}

export interface Recommendation {
  method: PaymentMethod;
  effectiveReward: number; // in ₹
  effectiveRate: number; // percentage equivalent
  breakdown: RewardBreakdown;
  reason: string;
  offerReason?: string;
  ruleWarning?: string; // e.g. rules older than 90 days
  isTopPick: boolean;
}

export interface PaymentLog {
  id: string;
  timestamp: string;
  amount: number;
  category: SpendCategory;
  merchantName: string;
  payeeAddress: string;
  source: 'scan' | 'manual';
  recommendedMethodId: string;
  recommendedMethodName: string;
  chosenMethodId: string;
  chosenMethodName: string;
  expectedReward: number;
  actualReward?: number;
  notes?: string;
}

export interface SavedMerchant {
  name: string;
  normalizedName: string;
  vpa?: string;
  category: SpendCategory;
  mcc?: string;
  lastUsedAt: string;
  usageCount: number;
}
