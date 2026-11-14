import React, { useState, useEffect, useMemo } from 'react';
import { 
  SpendCategory, PaymentMethod, Offer, SavedMerchant, 
  PaymentLog, PaymentDraft, ParsedUPI, Recommendation 
} from './types';
import { 
  getStoredMethods, saveStoredMethods, 
  getStoredOffers, saveStoredOffers, 
  getStoredMerchants, 
  getStoredLogs, addPaymentLog, saveStoredLogs,
  resetAllDataToDefault 
} from './utils/storage';
import { calculateRecommendations } from './utils/recommendationEngine';
import { Navbar, NavTab } from './components/Navbar';
import { QRScanner } from './components/QRScanner';
import { ManualPaymentForm } from './components/ManualPaymentForm';
import { RecommendationCard } from './components/RecommendationCard';
import { RedirectionModal } from './components/RedirectionModal';
import { CardsManager } from './components/CardsManager';
import { OffersManager } from './components/OffersManager';
import { PaymentHistory } from './components/PaymentHistory';
import { Sparkles, Scan, ArrowLeft, Info, CheckCircle2 } from 'lucide-react';

export const App: React.FC = () => {
  const [activeTab, setActiveTab] = useState<NavTab>('scan');
  const [methods, setMethods] = useState<PaymentMethod[]>([]);
  const [offers, setOffers] = useState<Offer[]>([]);
  const [savedMerchants, setSavedMerchants] = useState<SavedMerchant[]>([]);
  const [paymentLogs, setPaymentLogs] = useState<PaymentLog[]>([]);

  // Draft payment state
  const [draft, setDraft] = useState<PaymentDraft>({
    source: 'manual',
    payeeName: '',
    payeeAddress: '',
    amount: '',
    category: 'dining',
    categorySource: 'default',
  });

  const [recommendations, setRecommendations] = useState<Recommendation[] | null>(null);
  const [selectedForPayment, setSelectedForPayment] = useState<Recommendation | null>(null);
  const [qrNotification, setQrNotification] = useState<string | null>(null);

  // Load stored data on mount
  useEffect(() => {
    setMethods(getStoredMethods());
    setOffers(getStoredOffers());
    setSavedMerchants(getStoredMerchants());
    setPaymentLogs(getStoredLogs());
  }, []);

  const handleUpdateMethods = (updated: PaymentMethod[]) => {
    setMethods(updated);
    saveStoredMethods(updated);
    // Recalculate if recommendations currently open
    if (recommendations && typeof draft.amount === 'number' && draft.amount > 0) {
      const recs = calculateRecommendations({
        amount: draft.amount,
        category: draft.category,
        methods: updated,
        offers
      });
      setRecommendations(recs);
    }
  };

  const handleUpdateOffers = (updated: Offer[]) => {
    setOffers(updated);
    saveStoredOffers(updated);
    if (recommendations && typeof draft.amount === 'number' && draft.amount > 0) {
      const recs = calculateRecommendations({
        amount: draft.amount,
        category: draft.category,
        methods,
        offers: updated
      });
      setRecommendations(recs);
    }
  };

  const handleResetData = () => {
    if (confirm('Reset all cards, offers, and merchant history to initial defaults?')) {
      resetAllDataToDefault();
      setMethods(getStoredMethods());
      setOffers(getStoredOffers());
      setSavedMerchants(getStoredMerchants());
      setPaymentLogs(getStoredLogs());
      setRecommendations(null);
      setDraft({
        source: 'manual',
        payeeName: '',
        payeeAddress: '',
        amount: '',
        category: 'dining',
        categorySource: 'default',
      });
      setActiveTab('scan');
    }
  };

  // Trigger calculation
  const handleCalculateRecommendations = () => {
    if (typeof draft.amount !== 'number' || draft.amount <= 0 || !draft.category) {
      return;
    }

    const recs = calculateRecommendations({
      amount: draft.amount,
      category: draft.category,
      methods,
      offers
    });

    setRecommendations(recs);
  };

  // QR Decoded Handler (US-01)
  const handleQRDecoded = (parsed: ParsedUPI) => {
    const updatedDraft: PaymentDraft = {
      source: 'scan',
      payeeName: parsed.payeeName || parsed.payeeAddress || 'Scanned Merchant',
      payeeAddress: parsed.payeeAddress || '',
      amount: parsed.amount !== undefined ? parsed.amount : '',
      category: parsed.suggestedCategory || 'dining',
      categorySource: parsed.categorySource || 'default',
      mcc: parsed.mcc,
      rawQR: parsed.rawString,
      isNonUPIUrl: false
    };

    setDraft(updatedDraft);

    if (parsed.amount !== undefined && parsed.amount > 0) {
      // Amount is already present in QR code!
      // Calculate recommendation immediately
      const recs = calculateRecommendations({
        amount: parsed.amount,
        category: updatedDraft.category,
        methods,
        offers
      });
      setRecommendations(recs);
      setQrNotification(`Decoded ${parsed.payeeName || 'merchant'} · ₹${parsed.amount} · Category: ${updatedDraft.category}`);
      setActiveTab('manual'); // show payment view with recommendation ready
    } else {
      // US-01 bullet 2: "If the QR has no amount, the app asks me to enter one."
      setQrNotification('Payee details captured from QR. Please enter the payment amount.');
      setRecommendations(null);
      setActiveTab('manual');
    }
  };

  // Switch to manual mode (US-01 bullet 3 for non-UPI links)
  const handleSwitchToManual = (initialData?: Partial<ParsedUPI>) => {
    const updatedDraft: PaymentDraft = {
      source: 'manual',
      payeeName: initialData?.payeeName || '',
      payeeAddress: '',
      amount: '',
      category: initialData?.suggestedCategory || 'shopping',
      categorySource: initialData?.categorySource || 'manual',
      isNonUPIUrl: Boolean(initialData?.isNonUPIUrl)
    };

    setDraft(updatedDraft);
    setRecommendations(null);
    setQrNotification(initialData?.isNonUPIUrl ? 'Non-UPI link detected. Please enter amount and category.' : null);
    setActiveTab('manual');
  };

  // Confirm payment in log
  const handleConfirmPaymentLog = (log: PaymentLog) => {
    addPaymentLog(log);
    setPaymentLogs(getStoredLogs());
    setMethods(getStoredMethods());
    setSavedMerchants(getStoredMerchants());
    setSelectedForPayment(null);
    setRecommendations(null);
    setDraft({
      source: 'manual',
      payeeName: '',
      payeeAddress: '',
      amount: '',
      category: 'dining',
      categorySource: 'default'
    });
    setActiveTab('history');
  };

  return (
    <div className="min-h-screen bg-slate-950 text-slate-100 flex flex-col font-sans">
      <Navbar 
        activeTab={activeTab} 
        onSelectTab={(tab) => {
          setActiveTab(tab);
          setQrNotification(null);
        }}
        onResetData={handleResetData}
      />

      <main className="flex-1 max-w-5xl w-full mx-auto px-4 sm:px-6 py-6">
        {/* Banner notification if any */}
        {qrNotification && (
          <div 
            role="status" 
            className="mb-6 p-4 rounded-xl bg-indigo-500/10 border border-indigo-500/30 text-indigo-200 text-xs sm:text-sm flex items-center justify-between gap-3 animate-fade-in"
          >
            <div className="flex items-center gap-2">
              <CheckCircle2 className="w-4 h-4 text-emerald-400 shrink-0" />
              <span>{qrNotification}</span>
            </div>
            <button 
              type="button" 
              onClick={() => setQrNotification(null)}
              className="text-slate-400 hover:text-white text-xs font-semibold"
            >
              Dismiss
            </button>
          </div>
        )}

        {/* Tab 1: SCAN QR (US-01) */}
        {activeTab === 'scan' && (
          <div className="max-w-2xl mx-auto space-y-6">
            <div className="text-center sm:text-left">
              <h2 className="text-2xl font-black text-white tracking-tight">
                Capture Payment via QR
              </h2>
              <p className="text-sm text-slate-400 mt-1">
                Scan merchant or UPI QR code using your camera or gallery to auto-fill payee, amount, and spend category.
              </p>
            </div>

            <QRScanner
              savedMerchants={savedMerchants}
              onQRDecoded={handleQRDecoded}
              onSwitchToManual={handleSwitchToManual}
            />
          </div>
        )}

        {/* Tab 2: MANUAL PAYMENT & RECOMMENDATION (US-02 & US-03) */}
        {activeTab === 'manual' && (
          <div className="max-w-2xl mx-auto space-y-6">
            <div className="flex items-center justify-between">
              <div>
                <h2 className="text-2xl font-black text-white tracking-tight">
                  {draft.source === 'scan' ? 'Scanned Payment Routing' : 'Enter Payment Manually'}
                </h2>
                <p className="text-sm text-slate-400 mt-1">
                  {draft.source === 'scan' 
                    ? 'Review payee details and confirm spend category' 
                    : 'Enter purchase amount and category for online shopping or bills'}
                </p>
              </div>

              {draft.source === 'scan' && (
                <button
                  type="button"
                  onClick={() => {
                    setRecommendations(null);
                    setActiveTab('scan');
                  }}
                  className="text-xs text-indigo-400 hover:text-indigo-300 flex items-center gap-1 font-semibold"
                >
                  <ArrowLeft className="w-3.5 h-3.5" />
                  <span>Back to Scanner</span>
                </button>
              )}
            </div>

            {/* Recommendation Display if calculated */}
            {recommendations && recommendations.length > 0 ? (
              <RecommendationCard
                amount={typeof draft.amount === 'number' ? draft.amount : 0}
                category={draft.category}
                payeeName={draft.payeeName}
                recommendations={recommendations}
                onSelectMethodToPay={(rec) => setSelectedForPayment(rec)}
                onModifyPayment={() => setRecommendations(null)}
              />
            ) : (
              /* Payment Input Form */
              <div className="p-6 rounded-2xl bg-slate-900 border border-slate-800 shadow-xl">
                <ManualPaymentForm
                  draft={draft}
                  savedMerchants={savedMerchants}
                  isScanned={draft.source === 'scan'}
                  onChangeDraft={(updated) => setDraft(prev => ({ ...prev, ...updated }))}
                  onSubmit={handleCalculateRecommendations}
                  onReset={() => {
                    setDraft({
                      source: 'manual',
                      payeeName: '',
                      payeeAddress: '',
                      amount: '',
                      category: 'dining',
                      categorySource: 'default'
                    });
                    setRecommendations(null);
                  }}
                />
              </div>
            )}
          </div>
        )}

        {/* Tab 3: CARDS & RULES (FR-4) */}
        {activeTab === 'cards' && (
          <CardsManager
            methods={methods}
            onUpdateMethods={handleUpdateMethods}
          />
        )}

        {/* Tab 4: OFFERS (FR-5) */}
        {activeTab === 'offers' && (
          <OffersManager
            offers={offers}
            methods={methods}
            onUpdateOffers={handleUpdateOffers}
          />
        )}

        {/* Tab 5: PAYMENT LOG (FR-9) */}
        {activeTab === 'history' && (
          <PaymentHistory
            logs={paymentLogs}
            onClearLogs={() => {
              saveStoredLogs([]);
              setPaymentLogs([]);
            }}
          />
        )}
      </main>

      {/* Redirection / Payment Confirmation Modal */}
      {selectedForPayment && (
        <RedirectionModal
          recommendation={selectedForPayment}
          draft={draft}
          onClose={() => setSelectedForPayment(null)}
          onConfirmPayment={handleConfirmPaymentLog}
        />
      )}

      {/* Footer */}
      <footer className="mt-auto py-6 border-t border-slate-800/80 bg-slate-900/60 text-center text-xs text-slate-500">
        <p>Smart Payment Router · Epic 1: Capturing a Payment · Built for UPI & Indian Card Ecosystem</p>
      </footer>
    </div>
  );
};
