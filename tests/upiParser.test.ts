import { describe, it, expect } from 'vitest';
import { parseQRCode, buildUPIDeepLink } from '../src/utils/upiParser';
import { SavedMerchant } from '../src/types';

describe('US-01 & US-03: UPI QR Code Parsing and Category Mapping', () => {
  const sampleSavedMerchants: SavedMerchant[] = [
    {
      name: 'Swiggy',
      normalizedName: 'swiggy',
      vpa: 'swiggy@icici',
      category: 'dining',
      mcc: '5812',
      lastUsedAt: '2026-11-10T10:00:00Z',
      usageCount: 10
    },
    {
      name: 'HPCL Fuel Station',
      normalizedName: 'hpcl fuel station',
      vpa: 'hpcl@upi',
      category: 'fuel',
      mcc: '5541',
      lastUsedAt: '2026-11-09T10:00:00Z',
      usageCount: 5
    }
  ];

  it('US-01: should decode valid UPI QR with payee, fixed amount, and MCC', () => {
    const qrString = 'upi://pay?pa=restaurant@okaxis&pn=Green+Bistro&am=650.00&cu=INR&mc=5812&tn=Lunch+Table+4';
    const result = parseQRCode(qrString, sampleSavedMerchants);

    expect(result.isUPI).toBe(true);
    expect(result.payeeAddress).toBe('restaurant@okaxis');
    expect(result.payeeName).toBe('Green Bistro');
    expect(result.amount).toBe(650);
    expect(result.mcc).toBe('5812');
    expect(result.currency).toBe('INR');
    expect(result.suggestedCategory).toBe('dining');
    expect(result.categorySource).toBe('mcc');
  });

  it('US-01 bullet 2: should leave amount undefined if QR has no fixed amount', () => {
    const qrString = 'upi://pay?pa=retailer@upi&pn=City+Shop&cu=INR&mc=5411';
    const result = parseQRCode(qrString, sampleSavedMerchants);

    expect(result.isUPI).toBe(true);
    expect(result.payeeAddress).toBe('retailer@upi');
    expect(result.amount).toBeUndefined(); // triggers amount entry in app
    expect(result.suggestedCategory).toBe('groceries');
    expect(result.categorySource).toBe('mcc');
  });

  it('US-01 bullet 3: should detect non-UPI URL and switch to manual mode', () => {
    const nonUpiUrl = 'https://www.croma.com/checkout/order-9912';
    const result = parseQRCode(nonUpiUrl, sampleSavedMerchants);

    expect(result.isUPI).toBe(false);
    expect(result.isNonUPIUrl).toBe(true);
    expect(result.amount).toBeUndefined(); // requires manual entry
    expect(result.payeeName).toBe('croma.com');
  });

  it('US-03: should prioritize category from saved merchant past choices when MCC is absent', () => {
    const qrWithoutMcc = 'upi://pay?pa=swiggy@icici&pn=Swiggy+Delivery&am=450.00&cu=INR';
    const result = parseQRCode(qrWithoutMcc, sampleSavedMerchants);

    expect(result.isUPI).toBe(true);
    expect(result.suggestedCategory).toBe('dining');
    expect(result.categorySource).toBe('merchant_history');
  });

  it('US-03: should correctly map standard MCC codes to the fixed 8 categories', () => {
    // Fuel MCC 5541
    const fuelQR = parseQRCode('upi://pay?pa=petrol@paytm&am=1000&mc=5541');
    expect(fuelQR.suggestedCategory).toBe('fuel');

    // Groceries MCC 5411
    const groceryQR = parseQRCode('upi://pay?pa=mart@hdfc&am=500&mc=5411');
    expect(groceryQR.suggestedCategory).toBe('groceries');

    // Utilities MCC 4900
    const utilityQR = parseQRCode('upi://pay?pa=power@axis&am=2000&mc=4900');
    expect(utilityQR.suggestedCategory).toBe('utilities');

    // Entertainment MCC 7832
    const movieQR = parseQRCode('upi://pay?pa=cinemas@icici&am=800&mc=7832');
    expect(movieQR.suggestedCategory).toBe('entertainment');
  });

  it('FR-7: should correctly build UPI deep link with pre-filled details', () => {
    const link = buildUPIDeepLink({
      payeeAddress: 'merchant@upi',
      payeeName: 'Cafe Coffee Day',
      amount: 320,
      category: 'dining',
      mcc: '5812'
    });

    expect(link).toContain('upi://pay');
    expect(link).toContain('pa=merchant%40upi');
    expect(link).toContain('pn=Cafe+Coffee+Day');
    expect(link).toContain('am=320.00');
    expect(link).toContain('cu=INR');
  });
});
