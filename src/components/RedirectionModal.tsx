import React, { useState } from 'react';
import { Recommendation, PaymentDraft, SpendCategory, PaymentLog } from '../types';
import { 
  X, Check, Copy, ExternalLink, AlertTriangle, ArrowRight, ShieldCheck, 
  Smartphone, CreditCard, Sparkles, CheckCircle2 
} from 'lucide-react';
import { buildUPIDeepLink } from '../utils/upiParser';

interface Props {
  recommendation: Recommendation;
  draft: PaymentDraft;
  onClose: () => void;
  onConfirmPayment: (log: PaymentLog) => void;
}

export const RedirectionModal: React.FC<Props> = ({
  recommendation,
  draft,
  onClose,
  onConfirmPayment,
}) => {
  const appliedOffer = recommendation.breakdown.appliedOffer;
  // If offer requires activation, start on activation prompt (FR-8)
  const [activationConfirmed, setActivationConfirmed] = useState<boolean>(
    !appliedOffer?.requiresActivation
  );
  const [copiedPayee, setCopiedPayee] = useState<boolean>(false);
  const [copiedAmount, setCopiedAmount] = useState<boolean>(false);
  const [finalAmount, setFinalAmount] = useState<number>(
    typeof draft.amount === 'number' ? draft.amount : 0
  );
  const [paymentConfirmed, setPaymentConfirmed] = useState<boolean>(false);

  const amountVal = typeof draft.amount === 'number' ? draft.amount : 0;

  // Build UPI URI
  const upiDeepLink = buildUPIDeepLink({
    payeeAddress: draft.payeeAddress || 'merchant@upi',
    payeeName: draft.payeeName,
    amount: amountVal,
    category: draft.category,
    mcc: draft.mcc,
    transactionNote: `Payment via Smart Payment Router (${draft.category})`
  });

  const copyToClipboard = async (text: string, type: 'payee' | 'amount') => {
    try {
      await navigator.clipboard.writeText(text);
      if (type === 'payee') {
        setCopiedPayee(true);
        setTimeout(() => setCopiedPayee(false), 2500);
      } else {
        setCopiedAmount(true);
        setTimeout(() => setCopiedAmount(false), 2500);
      }
    } catch (err) {
      console.warn('Clipboard write error:', err);
    }
  };

  const handleLaunchPayment = () => {
    // Open UPI deep link
    window.location.href = upiDeepLink;
  };

  const handleCompletePayment = () => {
    const log: PaymentLog = {
      id: `pay_${Date.now()}_${Math.random().toString(36).substr(2, 6)}`,
      timestamp: new Date().toISOString(),
      amount: finalAmount,
      category: draft.category,
      merchantName: draft.payeeName || draft.payeeAddress || 'Merchant',
      payeeAddress: draft.payeeAddress || '',
      source: draft.source,
      recommendedMethodId: recommendation.method.id,
      recommendedMethodName: recommendation.method.name,
      chosenMethodId: recommendation.method.id,
      chosenMethodName: recommendation.method.name,
      expectedReward: recommendation.effectiveReward,
      actualReward: recommendation.effectiveReward,
      notes: `Used ${recommendation.method.name} for ${draft.category} spend`
    };

    setPaymentConfirmed(true);
    setTimeout(() => {
      onConfirmPayment(log);
    }, 1200);
  };

  return (
    <div 
      className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-slate-950/80 backdrop-blur-sm animate-fade-in"
      role="dialog"
      aria-modal="true"
      aria-labelledby="modal-title"
    >
      <div className="relative w-full max-w-lg rounded-2xl bg-slate-900 border border-slate-700 shadow-2xl p-6 sm:p-7 overflow-hidden max-h-[90vh] overflow-y-auto">
        {/* Close Button */}
        <button
          type="button"
          onClick={onClose}
          aria-label="Close dialog"
          className="absolute top-4 right-4 p-2 rounded-xl text-slate-400 hover:text-white hover:bg-slate-800 transition"
        >
          <X className="w-5 h-5" />
        </button>

        {paymentConfirmed ? (
          /* Payment Confirmed Celebration Screen */
          <div className="py-8 text-center space-y-4">
            <div className="w-16 h-16 rounded-full bg-emerald-500/20 text-emerald-400 border border-emerald-500/30 flex items-center justify-center mx-auto shadow-inner">
              <CheckCircle2 className="w-9 h-9" />
            </div>
            <h3 className="text-xl font-black text-white">Payment Logged Successfully!</h3>
            <p className="text-sm text-slate-300">
              Recorded <span className="font-bold text-white">₹{finalAmount.toFixed(2)}</span> with{' '}
              <span className="text-indigo-400 font-semibold">{recommendation.method.name}</span>.
            </p>
            <div className="p-3 rounded-xl bg-slate-800/80 border border-slate-700 max-w-xs mx-auto">
              <p className="text-xs text-slate-400">Expected Reward Earned</p>
              <p className="text-2xl font-black text-emerald-400">+₹{recommendation.effectiveReward.toFixed(2)}</p>
            </div>
          </div>
        ) : !activationConfirmed && appliedOffer ? (
          /* FR-8: Offer Applied Reminder & Activation Prompt */
          <div className="space-y-5">
            <div className="flex items-center gap-3">
              <div className="w-11 h-11 rounded-xl bg-amber-500/20 text-amber-400 border border-amber-500/30 flex items-center justify-center shrink-0">
                <AlertTriangle className="w-6 h-6" />
              </div>
              <div>
                <span className="text-xs font-bold text-amber-400 uppercase tracking-wider">Offer Activation Required</span>
                <h3 id="modal-title" className="text-lg font-bold text-white">
                  Check Offer in Card App
                </h3>
              </div>
            </div>

            <div className="p-4 rounded-xl bg-amber-500/10 border border-amber-500/20 space-y-2">
              <h4 className="text-sm font-bold text-amber-300">{appliedOffer.title}</h4>
              <p className="text-xs text-amber-200/90 leading-relaxed">{appliedOffer.description}</p>
            </div>

            <p className="text-xs text-slate-400 leading-relaxed">
              Some bank offers must be manually activated or unlocked in your{' '}
              <span className="text-white font-medium">{recommendation.method.issuer}</span> mobile app or net banking before the transaction to qualify for the cashback.
            </p>

            <div className="pt-2 flex flex-col sm:flex-row gap-3">
              <button
                type="button"
                onClick={() => setActivationConfirmed(true)}
                className="flex-1 py-3 px-4 rounded-xl bg-amber-500 hover:bg-amber-400 text-slate-950 font-bold text-sm shadow-md transition"
              >
                Yes, I Have Activated It
              </button>
              <button
                type="button"
                onClick={onClose}
                className="py-3 px-4 rounded-xl bg-slate-800 hover:bg-slate-700 text-slate-300 text-sm font-semibold transition"
              >
                Cancel
              </button>
            </div>
          </div>
        ) : (
          /* FR-7: Redirection & Fallback Execution */
          <div className="space-y-6">
            <div>
              <span className="text-xs font-semibold text-emerald-400 uppercase tracking-wider">
                Step 2 of 2 · Complete Payment
              </span>
              <h3 id="modal-title" className="text-xl font-black text-white mt-0.5">
                Pay with {recommendation.method.name}
              </h3>
              <p className="text-xs text-slate-400 mt-1">
                Amount: <span className="text-white font-bold">₹{amountVal.toFixed(2)}</span> · Reward: <span className="text-emerald-400 font-bold">+₹{recommendation.effectiveReward.toFixed(2)}</span>
              </p>
            </div>

            {/* Launch App Button */}
            <div className="p-4 rounded-xl bg-indigo-950/40 border border-indigo-500/30 space-y-3">
              <div className="flex items-center justify-between">
                <span className="text-xs font-bold text-indigo-300 flex items-center gap-1.5">
                  <Smartphone className="w-4 h-4" /> UPI App Deep Link
                </span>
                <span className="text-[10px] text-slate-400 font-mono">upi://pay</span>
              </div>
              <p className="text-xs text-slate-300">
                Launch your UPI payment app (GPay, PhonePe, Paytm, CRED) with payee and amount pre-filled:
              </p>
              <button
                type="button"
                onClick={handleLaunchPayment}
                className="w-full py-3.5 px-4 rounded-xl bg-indigo-600 hover:bg-indigo-500 text-white font-bold text-sm shadow-lg shadow-indigo-600/30 flex items-center justify-center gap-2 transition"
              >
                <span>Open UPI Payment App</span>
                <ExternalLink className="w-4 h-4" />
              </button>
            </div>

            {/* Clipboard Fallback (For Credit Cards, Online Checkout, or Unsupported Apps) */}
            <div className="space-y-3">
              <div className="flex items-center justify-between">
                <h4 className="text-xs font-bold text-slate-300 flex items-center gap-1.5">
                  <Copy className="w-3.5 h-3.5" /> Manual Copy Fallback
                </h4>
                <span className="text-[10px] text-slate-500">For banking apps & card checkout</span>
              </div>

              <div className="space-y-2 text-xs">
                {/* Payee VPA */}
                {draft.payeeAddress && (
                  <div className="p-2.5 rounded-lg bg-slate-800/90 border border-slate-700 flex items-center justify-between">
                    <div>
                      <span className="text-[10px] text-slate-400 block">Payee Address (VPA)</span>
                      <span className="font-mono text-slate-200">{draft.payeeAddress}</span>
                    </div>
                    <button
                      type="button"
                      onClick={() => copyToClipboard(draft.payeeAddress, 'payee')}
                      className="px-2.5 py-1 rounded bg-slate-700 hover:bg-slate-600 text-slate-200 text-xs font-medium flex items-center gap-1 transition"
                    >
                      {copiedPayee ? <Check className="w-3.5 h-3.5 text-emerald-400" /> : <Copy className="w-3.5 h-3.5" />}
                      <span>{copiedPayee ? 'Copied' : 'Copy'}</span>
                    </button>
                  </div>
                )}

                {/* Amount */}
                <div className="p-2.5 rounded-lg bg-slate-800/90 border border-slate-700 flex items-center justify-between">
                  <div>
                    <span className="text-[10px] text-slate-400 block">Amount</span>
                    <span className="font-mono font-bold text-slate-200">₹{amountVal.toFixed(2)}</span>
                  </div>
                  <button
                    type="button"
                    onClick={() => copyToClipboard(amountVal.toFixed(2), 'amount')}
                    className="px-2.5 py-1 rounded bg-slate-700 hover:bg-slate-600 text-slate-200 text-xs font-medium flex items-center gap-1 transition"
                  >
                    {copiedAmount ? <Check className="w-3.5 h-3.5 text-emerald-400" /> : <Copy className="w-3.5 h-3.5" />}
                    <span>{copiedAmount ? 'Copied' : 'Copy'}</span>
                  </button>
                </div>
              </div>
            </div>

            {/* Confirm Payment Completion Button (FR-9) */}
            <div className="pt-2 border-t border-slate-800 space-y-3">
              <div className="flex items-center justify-between text-xs">
                <span className="text-slate-400">Final Amount Paid:</span>
                <input
                  type="number"
                  step="any"
                  value={finalAmount}
                  onChange={(e) => setFinalAmount(parseFloat(e.target.value) || 0)}
                  className="w-24 px-2 py-1 bg-slate-800 border border-slate-700 rounded text-right font-mono text-white text-xs font-bold"
                />
              </div>

              <button
                type="button"
                onClick={handleCompletePayment}
                className="w-full py-3.5 px-4 rounded-xl bg-emerald-600 hover:bg-emerald-500 text-white font-bold text-sm shadow-md transition flex items-center justify-center gap-2"
              >
                <Check className="w-4 h-4" />
                <span>Confirm Payment Completed</span>
              </button>
            </div>
          </div>
        )}
      </div>
    </div>
  );
};
