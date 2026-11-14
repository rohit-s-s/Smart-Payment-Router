import React, { useState, useEffect } from 'react';
import { PaymentDraft, SpendCategory, SPEND_CATEGORIES, SavedMerchant } from '../types';
import { 
  Utensils, ShoppingCart, Fuel, Plane, Zap, ShoppingBag, Film, HelpCircle, 
  ArrowRight, Sparkles, Building, User, AlertCircle, CheckCircle2, RotateCcw,
  Info
} from 'lucide-react';
import { findSavedMerchant } from '../utils/upiParser';

interface Props {
  draft: PaymentDraft;
  savedMerchants: SavedMerchant[];
  onChangeDraft: (updated: Partial<PaymentDraft>) => void;
  onSubmit: () => void;
  onReset?: () => void;
  isScanned?: boolean;
}

const ICON_MAP: Record<SpendCategory, React.ComponentType<{ className?: string }>> = {
  dining: Utensils,
  groceries: ShoppingCart,
  fuel: Fuel,
  travel: Plane,
  utilities: Zap,
  shopping: ShoppingBag,
  entertainment: Film,
  other: HelpCircle,
};

export const ManualPaymentForm: React.FC<Props> = ({
  draft,
  savedMerchants,
  onChangeDraft,
  onSubmit,
  onReset,
  isScanned = false,
}) => {
  const [amountTouched, setAmountTouched] = useState<boolean>(false);
  const [merchantSuggestions, setMerchantSuggestions] = useState<SavedMerchant[]>([]);
  const [showMerchantSuggestions, setShowMerchantSuggestions] = useState<boolean>(false);

  // Watch merchant name change to suggest category from saved history (US-03)
  const handleMerchantNameChange = (name: string) => {
    onChangeDraft({ payeeName: name });

    if (!name.trim()) {
      setMerchantSuggestions([]);
      return;
    }

    // Filter matching saved merchants
    const matches = savedMerchants.filter(m => 
      m.name.toLowerCase().includes(name.toLowerCase()) ||
      (m.vpa && m.vpa.toLowerCase().includes(name.toLowerCase()))
    );
    setMerchantSuggestions(matches);
    setShowMerchantSuggestions(matches.length > 0);

    // If an exact or close match exists and user hasn't manually overridden yet
    const matched = findSavedMerchant(savedMerchants, draft.payeeAddress, name);
    if (matched && draft.categorySource !== 'manual') {
      onChangeDraft({
        category: matched.category,
        categorySource: 'merchant_history',
        payeeAddress: draft.payeeAddress || matched.vpa || ''
      });
    }
  };

  // Select a suggestion
  const handleSelectMerchant = (m: SavedMerchant) => {
    onChangeDraft({
      payeeName: m.name,
      payeeAddress: m.vpa || draft.payeeAddress,
      category: m.category,
      categorySource: 'merchant_history'
    });
    setShowMerchantSuggestions(false);
  };

  const handleAmountChange = (valStr: string) => {
    setAmountTouched(true);
    if (valStr === '') {
      onChangeDraft({ amount: '' });
      return;
    }
    const parsed = parseFloat(valStr);
    if (!isNaN(parsed) && parsed >= 0) {
      onChangeDraft({ amount: parsed });
    }
  };

  const addAmount = (delta: number) => {
    setAmountTouched(true);
    const curr = typeof draft.amount === 'number' ? draft.amount : 0;
    onChangeDraft({ amount: curr + delta });
  };

  const isAmountValid = typeof draft.amount === 'number' && draft.amount > 0;
  const isFormValid = isAmountValid && Boolean(draft.category);

  return (
    <div 
      className="space-y-6"
      role="region"
      aria-label="Payment Entry Form"
    >
      {/* Non-UPI QR Alert (US-01 bullet 3) */}
      {draft.isNonUPIUrl && (
        <div 
          role="alert"
          className="p-4 rounded-xl bg-amber-500/10 border border-amber-500/30 text-amber-200 text-xs sm:text-sm flex items-start gap-3"
        >
          <Info className="w-5 h-5 text-amber-400 shrink-0 mt-0.5" />
          <div>
            <p className="font-semibold text-amber-300">Non-UPI Link Scanned</p>
            <p className="mt-0.5 text-amber-200/90">
              The scanned QR code is a web link. Switched to manual entry. Please confirm the amount and spend category to calculate the highest cashback.
            </p>
          </div>
        </div>
      )}

      {/* Scanned Payee Banner if present */}
      {isScanned && draft.payeeName && (
        <div className="p-4 rounded-xl bg-indigo-950/40 border border-indigo-500/30 flex items-center justify-between">
          <div className="flex items-center gap-3">
            <div className="w-10 h-10 rounded-xl bg-indigo-500/20 text-indigo-400 flex items-center justify-center font-bold">
              <Building className="w-5 h-5" />
            </div>
            <div>
              <p className="text-xs text-indigo-300 uppercase font-semibold">Scanned Payee</p>
              <h4 className="text-sm sm:text-base font-bold text-white">{draft.payeeName}</h4>
              {draft.payeeAddress && (
                <p className="text-xs text-slate-400 font-mono">{draft.payeeAddress}</p>
              )}
            </div>
          </div>
          {onReset && (
            <button
              type="button"
              onClick={onReset}
              aria-label="Scan another code"
              className="text-xs text-slate-400 hover:text-white flex items-center gap-1.5 p-2 rounded-lg hover:bg-slate-800 transition"
            >
              <RotateCcw className="w-3.5 h-3.5" />
              <span>Rescan</span>
            </button>
          )}
        </div>
      )}

      {/* Amount Input (US-01 & US-02: Required before recommendation is produced) */}
      <div className="space-y-2">
        <div className="flex items-center justify-between">
          <label 
            htmlFor="payment-amount" 
            className="text-sm font-semibold text-slate-200 flex items-center gap-1.5"
          >
            <span>Payment Amount</span>
            <span className="text-rose-400" title="Required">*</span>
          </label>
          {isScanned && typeof draft.amount === 'number' && draft.amount > 0 && (
            <span className="text-xs text-emerald-400 flex items-center gap-1">
              <CheckCircle2 className="w-3 h-3" /> Pre-filled from QR
            </span>
          )}
        </div>

        <div className="relative">
          <div className="absolute inset-y-0 left-0 pl-4 flex items-center pointer-events-none text-slate-400 text-2xl font-bold">
            ₹
          </div>
          <input
            id="payment-amount"
            type="number"
            step="any"
            min="1"
            placeholder="0.00"
            value={draft.amount}
            onChange={(e) => handleAmountChange(e.target.value)}
            onBlur={() => setAmountTouched(true)}
            aria-label="Payment amount in Indian Rupees (required)"
            aria-required="true"
            aria-invalid={amountTouched && !isAmountValid}
            className={`w-full pl-11 pr-4 py-4 rounded-xl text-2xl font-bold bg-slate-800/90 border-2 transition ${
              amountTouched && !isAmountValid
                ? 'border-rose-500 focus:border-rose-400 focus:ring-2 focus:ring-rose-500/20 text-rose-200'
                : 'border-slate-700 focus:border-indigo-500 focus:ring-2 focus:ring-indigo-500/20 text-white'
            }`}
          />
        </div>

        {/* Quick Amount Helpers */}
        <div className="flex flex-wrap items-center gap-2 pt-1">
          <span className="text-[11px] text-slate-400 mr-1">Quick add:</span>
          {[100, 250, 500, 1000, 2000].map((amt) => (
            <button
              key={amt}
              type="button"
              onClick={() => addAmount(amt)}
              aria-label={`Add ${amt} rupees to payment amount`}
              className="text-xs px-2.5 py-1 rounded-lg bg-slate-800 hover:bg-slate-700 text-slate-300 border border-slate-700 hover:border-slate-600 transition"
            >
              +₹{amt}
            </button>
          ))}
          {draft.amount !== '' && (
            <button
              type="button"
              onClick={() => onChangeDraft({ amount: '' })}
              aria-label="Clear payment amount"
              className="text-xs px-2 py-1 rounded-lg text-slate-400 hover:text-rose-400 transition ml-auto"
            >
              Clear
            </button>
          )}
        </div>

        {amountTouched && !isAmountValid && (
          <p className="text-xs text-rose-400 flex items-center gap-1 pt-1" role="alert">
            <AlertCircle className="w-3.5 h-3.5" /> Please enter a valid payment amount greater than ₹0.
          </p>
        )}
      </div>

      {/* Merchant Details (Optional / Helpful for Merchant History US-03) */}
      <div className="space-y-2 relative">
        <label 
          htmlFor="merchant-name" 
          className="text-sm font-semibold text-slate-200 flex items-center justify-between"
        >
          <span>Merchant or Store Name</span>
          <span className="text-xs font-normal text-slate-400">Optional</span>
        </label>
        <div className="relative">
          <div className="absolute inset-y-0 left-0 pl-3.5 flex items-center pointer-events-none text-slate-400">
            <Building className="w-4 h-4" />
          </div>
          <input
            id="merchant-name"
            type="text"
            placeholder="e.g. Swiggy, Blinkit, HPCL, Shell, Amazon, Tata Power..."
            value={draft.payeeName}
            onChange={(e) => handleMerchantNameChange(e.target.value)}
            onFocus={() => {
              if (merchantSuggestions.length > 0) setShowMerchantSuggestions(true);
            }}
            aria-label="Merchant or store name (optional)"
            className="w-full pl-10 pr-4 py-3 rounded-xl bg-slate-800/90 border border-slate-700 focus:border-indigo-500 focus:ring-2 focus:ring-indigo-500/20 text-white text-sm"
          />
        </div>

        {/* Saved Merchant Autocomplete Dropdown */}
        {showMerchantSuggestions && merchantSuggestions.length > 0 && (
          <div className="absolute left-0 right-0 top-full mt-1.5 z-20 rounded-xl bg-slate-800 border border-slate-700 shadow-2xl overflow-hidden divide-y divide-slate-700/60">
            <div className="px-3 py-1.5 bg-slate-900/80 text-[11px] font-semibold text-slate-400 flex items-center justify-between">
              <span>Saved Merchants (Past Choices)</span>
              <button 
                type="button" 
                onClick={() => setShowMerchantSuggestions(false)}
                className="text-slate-400 hover:text-white"
              >
                Close
              </button>
            </div>
            {merchantSuggestions.slice(0, 4).map((m) => (
              <button
                key={m.name}
                type="button"
                onClick={() => handleSelectMerchant(m)}
                className="w-full text-left px-3.5 py-2.5 hover:bg-slate-700/70 transition flex items-center justify-between group"
              >
                <div>
                  <p className="text-xs font-semibold text-slate-200 group-hover:text-indigo-300">{m.name}</p>
                  {m.vpa && <p className="text-[10px] text-slate-400 font-mono">{m.vpa}</p>}
                </div>
                <div className="text-right">
                  <span className="text-[10px] font-medium px-2 py-0.5 rounded-full bg-slate-700 text-slate-300 capitalize">
                    {m.category}
                  </span>
                  <p className="text-[9px] text-slate-500 mt-0.5">{m.usageCount} past visits</p>
                </div>
              </button>
            ))}
          </div>
        )}
      </div>

      {/* Spend Category Selection (US-03) */}
      <div className="space-y-3">
        <div className="flex items-center justify-between">
          <label className="text-sm font-semibold text-slate-200 flex items-center gap-1.5">
            <span>Spend Category</span>
            <span className="text-rose-400" title="Required">*</span>
          </label>
          
          {/* Category Source Indicator / Override reminder */}
          {draft.categorySource && (
            <div className="flex items-center gap-1.5 text-xs">
              <span className="text-slate-400">
                {draft.categorySource === 'mcc' ? 'From QR MCC' :
                 draft.categorySource === 'merchant_history' ? 'From Past Choice' :
                 draft.categorySource === 'manual' ? 'Manually Chosen' : 'Default'}
              </span>
              <span className="text-slate-500">· (tap any to change)</span>
            </div>
          )}
        </div>

        {/* 8 Fixed Categories Grid */}
        <div 
          className="grid grid-cols-2 sm:grid-cols-4 gap-2.5"
          role="radiogroup" 
          aria-label="Select spend category"
        >
          {SPEND_CATEGORIES.map((cat) => {
            const isSelected = draft.category === cat.id;
            const IconComponent = ICON_MAP[cat.id] || HelpCircle;

            return (
              <button
                key={cat.id}
                type="button"
                role="radio"
                aria-checked={isSelected}
                aria-label={`Select ${cat.label} category. ${cat.description}`}
                onClick={() => {
                  onChangeDraft({
                    category: cat.id,
                    categorySource: 'manual' // user manually overrode category (US-03)
                  });
                }}
                className={`relative p-3 rounded-xl border text-left transition-all ${
                  isSelected
                    ? 'bg-indigo-950/60 border-indigo-500 ring-2 ring-indigo-500/30 text-white shadow-md'
                    : 'bg-slate-800/80 border-slate-700/80 hover:bg-slate-800 hover:border-slate-600 text-slate-300'
                }`}
              >
                <div className="flex items-center gap-2.5 mb-1.5">
                  <div className={`w-7 h-7 rounded-lg bg-gradient-to-r ${cat.color} flex items-center justify-center text-white shrink-0`}>
                    <IconComponent className="w-4 h-4" />
                  </div>
                  <span className="text-xs font-bold leading-tight line-clamp-1">{cat.label}</span>
                </div>
                <p className="text-[10px] text-slate-400 line-clamp-1">{cat.description}</p>

                {isSelected && (
                  <div className="absolute top-2 right-2 w-2 h-2 rounded-full bg-indigo-400 ring-4 ring-indigo-400/20" />
                )}
              </button>
            );
          })}
        </div>
      </div>

      {/* Submit / Find Recommendation Button */}
      <div className="pt-2">
        <button
          type="button"
          disabled={!isFormValid}
          onClick={onSubmit}
          aria-label="Calculate best payment card recommendation"
          className={`w-full py-4 px-6 rounded-xl font-bold text-base flex items-center justify-center gap-2.5 shadow-xl transition-all ${
            isFormValid
              ? 'bg-gradient-to-r from-indigo-600 via-indigo-700 to-indigo-800 hover:from-indigo-500 hover:to-indigo-600 text-white shadow-indigo-600/30 hover:scale-[1.01] active:scale-[0.99] cursor-pointer'
              : 'bg-slate-800 text-slate-500 border border-slate-700 cursor-not-allowed opacity-70'
          }`}
        >
          <Sparkles className="w-5 h-5 text-amber-300" />
          <span>Find Highest Reward Method</span>
          <ArrowRight className="w-5 h-5 ml-1" />
        </button>

        {!isFormValid && (
          <p className="text-center text-xs text-slate-400 mt-2">
            Enter an amount greater than ₹0 and select a spend category to view recommendations
          </p>
        )}
      </div>
    </div>
  );
};
