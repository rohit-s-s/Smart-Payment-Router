import { PaymentMethod, Offer, SavedMerchant, PaymentLog } from '../types';
import { DEFAULT_PAYMENT_METHODS, DEFAULT_OFFERS, DEFAULT_SAVED_MERCHANTS } from '../data/defaultData';
import { normalizeMerchantName } from './upiParser';

const STORAGE_KEYS = {
  METHODS: 'spr_payment_methods_v1',
  OFFERS: 'spr_offers_v1',
  MERCHANTS: 'spr_saved_merchants_v1',
  LOGS: 'spr_payment_logs_v1',
};

export function getStoredMethods(): PaymentMethod[] {
  try {
    const raw = localStorage.getItem(STORAGE_KEYS.METHODS);
    if (!raw) {
      saveStoredMethods(DEFAULT_PAYMENT_METHODS);
      return DEFAULT_PAYMENT_METHODS;
    }
    return JSON.parse(raw);
  } catch {
    return DEFAULT_PAYMENT_METHODS;
  }
}

export function saveStoredMethods(methods: PaymentMethod[]): void {
  try {
    localStorage.setItem(STORAGE_KEYS.METHODS, JSON.stringify(methods));
  } catch (e) {
    console.error('Failed to save methods to localStorage', e);
  }
}

export function getStoredOffers(): Offer[] {
  try {
    const raw = localStorage.getItem(STORAGE_KEYS.OFFERS);
    if (!raw) {
      saveStoredOffers(DEFAULT_OFFERS);
      return DEFAULT_OFFERS;
    }
    return JSON.parse(raw);
  } catch {
    return DEFAULT_OFFERS;
  }
}

export function saveStoredOffers(offers: Offer[]): void {
  try {
    localStorage.setItem(STORAGE_KEYS.OFFERS, JSON.stringify(offers));
  } catch (e) {
    console.error('Failed to save offers to localStorage', e);
  }
}

export function getStoredMerchants(): SavedMerchant[] {
  try {
    const raw = localStorage.getItem(STORAGE_KEYS.MERCHANTS);
    if (!raw) {
      saveStoredMerchants(DEFAULT_SAVED_MERCHANTS);
      return DEFAULT_SAVED_MERCHANTS;
    }
    return JSON.parse(raw);
  } catch {
    return DEFAULT_SAVED_MERCHANTS;
  }
}

export function saveStoredMerchants(merchants: SavedMerchant[]): void {
  try {
    localStorage.setItem(STORAGE_KEYS.MERCHANTS, JSON.stringify(merchants));
  } catch (e) {
    console.error('Failed to save merchants to localStorage', e);
  }
}

export function recordMerchantVisit(merchant: {
  name: string;
  category: SavedMerchant['category'];
  vpa?: string;
  mcc?: string;
}): void {
  if (!merchant.name && !merchant.vpa) return;

  const list = getStoredMerchants();
  const normalized = normalizeMerchantName(merchant.name || '');

  const index = list.findIndex(m => 
    (m.vpa && merchant.vpa && m.vpa.toLowerCase() === merchant.vpa.toLowerCase()) ||
    (normalized && m.normalizedName === normalized)
  );

  const now = new Date().toISOString();

  if (index >= 0) {
    list[index].lastUsedAt = now;
    list[index].usageCount += 1;
    list[index].category = merchant.category; // remember user preference
    if (merchant.mcc) list[index].mcc = merchant.mcc;
    if (merchant.vpa) list[index].vpa = merchant.vpa;
  } else {
    list.unshift({
      name: merchant.name || merchant.vpa || 'Saved Merchant',
      normalizedName: normalized,
      vpa: merchant.vpa,
      category: merchant.category,
      mcc: merchant.mcc,
      lastUsedAt: now,
      usageCount: 1
    });
  }

  saveStoredMerchants(list);
}

export function getStoredLogs(): PaymentLog[] {
  try {
    const raw = localStorage.getItem(STORAGE_KEYS.LOGS);
    if (!raw) return [];
    return JSON.parse(raw);
  } catch {
    return [];
  }
}

export function saveStoredLogs(logs: PaymentLog[]): void {
  try {
    localStorage.setItem(STORAGE_KEYS.LOGS, JSON.stringify(logs));
  } catch (e) {
    console.error('Failed to save logs to localStorage', e);
  }
}

export function addPaymentLog(log: PaymentLog): void {
  const current = getStoredLogs();
  current.unshift(log);
  saveStoredLogs(current);

  // Update card cap usage
  const methods = getStoredMethods();
  const targetMethod = methods.find(m => m.id === log.chosenMethodId);
  if (targetMethod && log.expectedReward > 0) {
    targetMethod.currentMonthRewardUsed = (targetMethod.currentMonthRewardUsed || 0) + log.expectedReward;
    saveStoredMethods(methods);
  }

  // Record merchant preference memory (US-03)
  recordMerchantVisit({
    name: log.merchantName,
    vpa: log.payeeAddress,
    category: log.category
  });
}

export function resetAllDataToDefault(): void {
  localStorage.removeItem(STORAGE_KEYS.METHODS);
  localStorage.removeItem(STORAGE_KEYS.OFFERS);
  localStorage.removeItem(STORAGE_KEYS.MERCHANTS);
  localStorage.removeItem(STORAGE_KEYS.LOGS);
  saveStoredMethods(DEFAULT_PAYMENT_METHODS);
  saveStoredOffers(DEFAULT_OFFERS);
  saveStoredMerchants(DEFAULT_SAVED_MERCHANTS);
}
