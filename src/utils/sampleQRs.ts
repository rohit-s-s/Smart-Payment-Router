export interface SampleQR {
  id: string;
  title: string;
  description: string;
  tag: string;
  upiString: string;
  expectedOutcome: string;
}

export const SAMPLE_QR_CODES: SampleQR[] = [
  {
    id: 'swiggy-dining',
    title: 'Swiggy Dining (Amount + MCC 5812)',
    description: 'upi://pay?pa=swiggy@icici&pn=Swiggy+Food&am=650.00&cu=INR&mc=5812',
    tag: 'US-01 Fixed Amount',
    upiString: 'upi://pay?pa=swiggy@icici&pn=Swiggy+Food&am=650.00&cu=INR&mc=5812&tn=Dinner+Bill',
    expectedOutcome: 'Pre-fills ₹650.00, detects MCC 5812 -> Dining & Food, applies 5% cashback'
  },
  {
    id: 'hpcl-fuel-no-amount',
    title: 'HPCL Petrol Pump (Missing Amount, MCC 5541)',
    description: 'upi://pay?pa=hpclpump@upi&pn=HPCL+AutoCare&cu=INR&mc=5541',
    tag: 'US-01 Missing Amount',
    upiString: 'upi://pay?pa=hpclpump@upi&pn=HPCL+AutoCare&cu=INR&mc=5541&tn=Pump+Bay+3',
    expectedOutcome: 'Detects Fuel category from MCC 5541, prompts user to type payment amount'
  },
  {
    id: 'blinkit-grocery',
    title: 'Blinkit Groceries (Amount ₹380 + MCC 5411)',
    description: 'upi://pay?pa=blinkit@kotak&pn=Blinkit+Quick&am=380.00&cu=INR&mc=5411',
    tag: 'US-01 & US-03',
    upiString: 'upi://pay?pa=blinkit@kotak&pn=Blinkit+Quick&am=380.00&cu=INR&mc=5411&tn=Grocery+Order',
    expectedOutcome: 'Pre-fills ₹380, detects Groceries category, recommends best cashback card'
  },
  {
    id: 'croma-non-upi-link',
    title: 'Croma Web Link (Non-UPI QR Fallback)',
    description: 'https://www.croma.com/checkout/order-7890',
    tag: 'US-01 Non-UPI Fallback',
    upiString: 'https://www.croma.com/checkout/order-7890',
    expectedOutcome: 'Detects non-UPI link, switches to manual mode, prompts amount & category'
  },
  {
    id: 'saved-bescom-no-mcc',
    title: 'BESCOM Power (Saved Merchant Memory, No MCC)',
    description: 'upi://pay?pa=bescom@billdesk&pn=BESCOM+Electricity+Bill&am=1250.00',
    tag: 'US-03 Merchant Memory',
    upiString: 'upi://pay?pa=bescom@billdesk&pn=BESCOM+Electricity+Bill&am=1250.00&cu=INR',
    expectedOutcome: 'Has no MCC, but auto-maps to Utilities from saved merchant memory!'
  }
];
