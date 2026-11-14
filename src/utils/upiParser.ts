import { ParsedUPI, SpendCategory, SavedMerchant } from '../types';
import { mapMccToCategory } from './mccMapper';

/**
 * Normalizes merchant name for fuzzy matching (lowercased, stripped punctuation)
 */
export function normalizeMerchantName(name: string): string {
  return name
    .toLowerCase()
    .replace(/[^\w\s]/gi, '')
    .trim();
}

/**
 * Searches saved merchants by payee VPA or payee name
 */
export function findSavedMerchant(
  savedMerchants: SavedMerchant[],
  vpa?: string,
  name?: string
): SavedMerchant | undefined {
  if (!savedMerchants || savedMerchants.length === 0) return undefined;

  // 1. Exact match on VPA if available
  if (vpa) {
    const cleanVpa = vpa.trim().toLowerCase();
    const vpaMatch = savedMerchants.find(m => m.vpa && m.vpa.trim().toLowerCase() === cleanVpa);
    if (vpaMatch) return vpaMatch;
  }

  // 2. Match on name
  if (name) {
    const normalized = normalizeMerchantName(name);
    if (normalized) {
      // Exact normalized match
      const exactNameMatch = savedMerchants.find(m => m.normalizedName === normalized);
      if (exactNameMatch) return exactNameMatch;

      // Partial containment match
      const partialMatch = savedMerchants.find(m => 
        m.normalizedName.includes(normalized) || normalized.includes(m.normalizedName)
      );
      if (partialMatch) return partialMatch;
    }
  }

  return undefined;
}

/**
 * Parses a QR code string (UPI or otherwise)
 */
export function parseQRCode(
  qrString: string,
  savedMerchants: SavedMerchant[] = []
): ParsedUPI {
  const trimmed = (qrString || '').trim();

  if (!trimmed) {
    return {
      rawString: '',
      isUPI: false,
      isNonUPIUrl: false,
      suggestedCategory: 'other',
      categorySource: 'default'
    };
  }

  // Check if it is a UPI URI
  const isUpiScheme = /^upi:\/\/pay(\/|\?)?/i.test(trimmed);

  if (isUpiScheme) {
    try {
      const queryString = trimmed.includes('?') ? trimmed.substring(trimmed.indexOf('?') + 1) : '';
      const params = new URLSearchParams(queryString);

      // Support case-insensitive parameter lookup
      const getParam = (paramName: string): string | undefined => {
        for (const [key, value] of params.entries()) {
          if (key.toLowerCase() === paramName.toLowerCase()) {
            return decodeURIComponent(value).trim();
          }
        }
        return undefined;
      };

      const pa = getParam('pa'); // payee address (VPA)
      const pn = getParam('pn'); // payee name
      const amStr = getParam('am'); // amount
      const mc = getParam('mc'); // MCC
      const tr = getParam('tr'); // transaction ref
      const tn = getParam('tn'); // transaction note
      const cu = getParam('cu') || 'INR'; // currency

      let amount: number | undefined = undefined;
      if (amStr) {
        const parsed = parseFloat(amStr);
        if (!isNaN(parsed) && parsed > 0) {
          amount = Math.round(parsed * 100) / 100;
        }
      }

      // Determine category according to US-03 priority:
      // 1. Saved merchants memory
      // 2. MCC from QR
      // 3. Name keywords
      // 4. Default other
      let suggestedCategory: SpendCategory = 'other';
      let categorySource: 'mcc' | 'merchant_history' | 'manual' | 'default' = 'default';
      let mccDescription: string | undefined = undefined;

      // 1. Check saved merchants
      const matchedSaved = findSavedMerchant(savedMerchants, pa, pn);
      if (matchedSaved) {
        suggestedCategory = matchedSaved.category;
        categorySource = 'merchant_history';
      } else if (mc) {
        // 2. Check MCC
        const mccResult = mapMccToCategory(mc);
        suggestedCategory = mccResult.category;
        categorySource = 'mcc';
        mccDescription = mccResult.name;
      } else if (pn || tn) {
        // 3. Heuristic from name/note
        const combined = `${pn || ''} ${tn || ''}`.toLowerCase();
        if (combined.match(/petrol|fuel|hpcl|iocl|bpcl|shell|cng/)) {
          suggestedCategory = 'fuel';
        } else if (combined.match(/restaurant|cafe|swiggy|zomato|dining|food|kitchen|bakes|pizza|burger/)) {
          suggestedCategory = 'dining';
        } else if (combined.match(/mart|supermarket|grocery|groceries|blinkit|zepto|kirana|vegetables|fruits/)) {
          suggestedCategory = 'groceries';
        } else if (combined.match(/uber|ola|rapido|irctc|railway|flight|indigo|air india|hotel|travel/)) {
          suggestedCategory = 'travel';
        } else if (combined.match(/bescom|electricity|power|gas|bill|telecom|airtel|jio|broadband/)) {
          suggestedCategory = 'utilities';
        } else if (combined.match(/cinema|movie|pvr|inox|bookmyshow|theatre|ticket/)) {
          suggestedCategory = 'entertainment';
        } else if (combined.match(/store|retail|shopping|amazon|flipkart|myntra|clothes|apparel/)) {
          suggestedCategory = 'shopping';
        }
      }

      return {
        rawString: trimmed,
        isUPI: true,
        isNonUPIUrl: false,
        payeeAddress: pa,
        payeeName: pn,
        amount,
        mcc: mc,
        transactionRef: tr,
        transactionNote: tn,
        currency: cu,
        suggestedCategory,
        categorySource,
        mccDescription
      };
    } catch {
      // Fallback if URLSearchParams fails
    }
  }

  // Check if it's a website or non-UPI URL
  const isUrl = /^https?:\/\//i.test(trimmed);
  let extractedName: string | undefined = undefined;
  if (isUrl) {
    try {
      const parsedUrl = new URL(trimmed);
      extractedName = parsedUrl.hostname.replace(/^www\./, '');
    } catch {
      extractedName = trimmed;
    }
  }

  // Non-UPI QR code: as per US-01 bullet 3:
  // "If the QR is not a UPI string (for example, a website link), the app switches to manual entry and asks for the amount and category."
  const matchedSaved = extractedName ? findSavedMerchant(savedMerchants, undefined, extractedName) : undefined;

  return {
    rawString: trimmed,
    isUPI: false,
    isNonUPIUrl: isUrl,
    payeeName: extractedName || (trimmed.length < 50 ? trimmed : 'Scanned Merchant'),
    suggestedCategory: matchedSaved ? matchedSaved.category : 'other',
    categorySource: matchedSaved ? 'merchant_history' : 'default',
    amount: undefined // QR is not UPI, requires amount entry
  };
}

/**
 * Generates a valid UPI deep link URL
 */
export function buildUPIDeepLink(params: {
  payeeAddress: string;
  payeeName?: string;
  amount?: number;
  category?: SpendCategory;
  mcc?: string;
  transactionNote?: string;
}): string {
  const url = new URL('upi://pay');
  url.searchParams.set('pa', params.payeeAddress);
  if (params.payeeName) {
    url.searchParams.set('pn', params.payeeName);
  }
  if (params.amount && params.amount > 0) {
    url.searchParams.set('am', params.amount.toFixed(2));
  }
  url.searchParams.set('cu', 'INR');
  if (params.mcc) {
    url.searchParams.set('mc', params.mcc);
  }
  if (params.transactionNote) {
    url.searchParams.set('tn', params.transactionNote);
  } else {
    url.searchParams.set('tn', `Payment via Smart Payment Router (${params.category || 'general'})`);
  }
  return url.toString();
}
