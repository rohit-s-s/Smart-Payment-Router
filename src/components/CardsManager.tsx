import React, { useState } from 'react';
import { PaymentMethod, SpendCategory, SPEND_CATEGORIES } from '../types';
import { CreditCard, Plus, Trash2, Edit3, Check, X, ShieldAlert, AlertCircle } from 'lucide-react';

interface Props {
  methods: PaymentMethod[];
  onUpdateMethods: (updated: PaymentMethod[]) => void;
}

export const CardsManager: React.FC<Props> = ({ methods, onUpdateMethods }) => {
  const [editingCard, setEditingCard] = useState<PaymentMethod | null>(null);
  const [isAddingNew, setIsAddingNew] = useState<boolean>(false);

  const toggleActive = (id: string) => {
    const updated = methods.map(m => m.id === id ? { ...m, isActive: !m.isActive } : m);
    onUpdateMethods(updated);
  };

  const deleteCard = (id: string) => {
    if (confirm('Are you sure you want to remove this payment method?')) {
      const updated = methods.filter(m => m.id !== id);
      onUpdateMethods(updated);
    }
  };

  const handleSaveCard = (card: PaymentMethod) => {
    if (isAddingNew) {
      onUpdateMethods([...methods, card]);
      setIsAddingNew(false);
    } else {
      onUpdateMethods(methods.map(m => m.id === card.id ? card : m));
      setEditingCard(null);
    }
  };

  const startNewCard = () => {
    const newCard: PaymentMethod = {
      id: `card_${Date.now()}`,
      name: '',
      type: 'credit_card',
      issuer: '',
      network: 'Visa',
      baseRate: 1.0,
      rules: [
        { category: 'dining', rate: 5.0, capPeriod: 'monthly', capAmount: 1000 },
        { category: 'all', rate: 1.0 }
      ],
      monthlyCap: 1000,
      currentMonthRewardUsed: 0,
      ruleLastConfirmed: new Date().toISOString().split('T')[0],
      isActive: true,
      colorGradient: 'from-indigo-600 to-slate-900'
    };
    setEditingCard(newCard);
    setIsAddingNew(true);
  };

  return (
    <div className="space-y-6">
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-3">
        <div>
          <h3 className="text-xl font-black text-white">Payment Methods Inventory</h3>
          <p className="text-xs text-slate-400">
            Manage your credit cards, debit cards, and UPI apps with reward rules and caps.
          </p>
        </div>
        <button
          type="button"
          onClick={startNewCard}
          className="inline-flex items-center gap-2 px-4 py-2.5 rounded-xl bg-indigo-600 hover:bg-indigo-500 text-white font-semibold text-xs transition self-start shadow-md"
        >
          <Plus className="w-4 h-4" />
          <span>Add New Card</span>
        </button>
      </div>

      {/* Cards Grid */}
      <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
        {methods.map((method) => {
          const capPercent = method.monthlyCap 
            ? Math.min(100, Math.round((method.currentMonthRewardUsed / method.monthlyCap) * 100))
            : 0;

          return (
            <div
              key={method.id}
              className={`p-5 rounded-2xl border transition-all ${
                method.isActive 
                  ? 'bg-slate-800/90 border-slate-700 shadow-xl' 
                  : 'bg-slate-900/50 border-slate-800 opacity-60'
              }`}
            >
              <div className="flex items-start justify-between gap-3 mb-3">
                <div className="flex items-center gap-3">
                  <div className={`w-11 h-11 rounded-xl bg-gradient-to-br ${method.colorGradient} flex items-center justify-center text-white shadow-md`}>
                    <CreditCard className="w-5 h-5" />
                  </div>
                  <div>
                    <h4 className="text-sm font-bold text-white">{method.name}</h4>
                    <p className="text-xs text-slate-400">
                      {method.issuer} · {method.network} {method.cardLast4 ? `(•• ${method.cardLast4})` : ''}
                    </p>
                  </div>
                </div>

                {/* Active switch */}
                <button
                  type="button"
                  onClick={() => toggleActive(method.id)}
                  aria-label={method.isActive ? 'Deactivate method' : 'Activate method'}
                  className={`px-2.5 py-1 rounded-full text-[10px] font-bold uppercase tracking-wider transition ${
                    method.isActive 
                      ? 'bg-emerald-950 text-emerald-400 border border-emerald-800/60' 
                      : 'bg-slate-800 text-slate-500 border border-slate-700'
                  }`}
                >
                  {method.isActive ? 'Active' : 'Disabled'}
                </button>
              </div>

              {/* Rules summary */}
              <div className="space-y-1.5 my-3 text-xs text-slate-300">
                <div className="flex items-center justify-between text-slate-400">
                  <span>Base Rate:</span>
                  <span className="font-semibold text-white">{method.baseRate}%</span>
                </div>
                <div className="flex flex-wrap gap-1.5 pt-1">
                  {method.rules.filter(r => r.category !== 'all').map(r => (
                    <span 
                      key={r.category}
                      className="px-2 py-0.5 rounded bg-slate-700/80 text-[11px] text-indigo-300"
                    >
                      {r.category}: {r.rate}%
                    </span>
                  ))}
                </div>
              </div>

              {/* Monthly Cap Bar */}
              {method.monthlyCap ? (
                <div className="pt-2 border-t border-slate-700/60">
                  <div className="flex justify-between text-[11px] text-slate-400 mb-1">
                    <span>Monthly Cap Used:</span>
                    <span>₹{method.currentMonthRewardUsed} / ₹{method.monthlyCap} ({capPercent}%)</span>
                  </div>
                  <div className="w-full h-1.5 rounded-full bg-slate-700 overflow-hidden">
                    <div 
                      className={`h-full rounded-full transition-all ${
                        capPercent >= 90 ? 'bg-rose-500' : capPercent >= 70 ? 'bg-amber-400' : 'bg-indigo-400'
                      }`}
                      style={{ width: `${capPercent}%` }}
                    />
                  </div>
                </div>
              ) : (
                <div className="pt-2 border-t border-slate-700/60 text-[11px] text-slate-400">
                  Monthly Cap: <span className="text-emerald-400 font-semibold">Unlimited</span>
                </div>
              )}

              {/* Action buttons */}
              <div className="mt-4 pt-3 border-t border-slate-700/60 flex items-center justify-between">
                <span className="text-[10px] text-slate-500">
                  Verified: {method.ruleLastConfirmed || 'Recently'}
                </span>
                <div className="flex items-center gap-2">
                  <button
                    type="button"
                    onClick={() => {
                      setEditingCard(method);
                      setIsAddingNew(false);
                    }}
                    className="p-1.5 text-slate-400 hover:text-white rounded-lg hover:bg-slate-700 transition"
                    aria-label={`Edit ${method.name}`}
                  >
                    <Edit3 className="w-4 h-4" />
                  </button>
                  <button
                    type="button"
                    onClick={() => deleteCard(method.id)}
                    className="p-1.5 text-slate-400 hover:text-rose-400 rounded-lg hover:bg-slate-700 transition"
                    aria-label={`Delete ${method.name}`}
                  >
                    <Trash2 className="w-4 h-4" />
                  </button>
                </div>
              </div>
            </div>
          );
        })}
      </div>

      {/* Edit / Add Modal */}
      {editingCard && (
        <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-slate-950/80 backdrop-blur-sm">
          <div className="w-full max-w-lg rounded-2xl bg-slate-900 border border-slate-700 p-6 space-y-4 max-h-[90vh] overflow-y-auto">
            <div className="flex items-center justify-between">
              <h3 className="text-lg font-bold text-white">
                {isAddingNew ? 'Add Payment Method' : `Edit ${editingCard.name}`}
              </h3>
              <button 
                type="button" 
                onClick={() => { setEditingCard(null); setIsAddingNew(false); }}
                className="text-slate-400 hover:text-white"
              >
                <X className="w-5 h-5" />
              </button>
            </div>

            <div className="space-y-3 text-xs">
              <div>
                <label className="block text-slate-400 mb-1">Card / Method Display Name</label>
                <input
                  type="text"
                  value={editingCard.name}
                  onChange={(e) => setEditingCard({ ...editingCard, name: e.target.value })}
                  className="w-full px-3 py-2 bg-slate-800 border border-slate-700 rounded-lg text-white"
                  placeholder="e.g. HDFC Regalia Gold"
                />
              </div>

              <div className="grid grid-cols-2 gap-3">
                <div>
                  <label className="block text-slate-400 mb-1">Issuer / Bank</label>
                  <input
                    type="text"
                    value={editingCard.issuer}
                    onChange={(e) => setEditingCard({ ...editingCard, issuer: e.target.value })}
                    className="w-full px-3 py-2 bg-slate-800 border border-slate-700 rounded-lg text-white"
                    placeholder="e.g. HDFC Bank"
                  />
                </div>
                <div>
                  <label className="block text-slate-400 mb-1">Network</label>
                  <select
                    value={editingCard.network}
                    onChange={(e) => setEditingCard({ ...editingCard, network: e.target.value })}
                    className="w-full px-3 py-2 bg-slate-800 border border-slate-700 rounded-lg text-white"
                  >
                    <option value="Visa">Visa</option>
                    <option value="Mastercard">Mastercard</option>
                    <option value="RuPay">RuPay</option>
                    <option value="UPI">UPI</option>
                    <option value="Amex">American Express</option>
                  </select>
                </div>
              </div>

              <div className="grid grid-cols-2 gap-3">
                <div>
                  <label className="block text-slate-400 mb-1">Base Reward Rate (%)</label>
                  <input
                    type="number"
                    step="0.1"
                    value={editingCard.baseRate}
                    onChange={(e) => setEditingCard({ ...editingCard, baseRate: parseFloat(e.target.value) || 0 })}
                    className="w-full px-3 py-2 bg-slate-800 border border-slate-700 rounded-lg text-white"
                  />
                </div>
                <div>
                  <label className="block text-slate-400 mb-1">Monthly Reward Cap (₹)</label>
                  <input
                    type="number"
                    value={editingCard.monthlyCap ?? ''}
                    placeholder="Leave empty for unlimited"
                    onChange={(e) => setEditingCard({ 
                      ...editingCard, 
                      monthlyCap: e.target.value === '' ? undefined : (parseFloat(e.target.value) || 0) 
                    })}
                    className="w-full px-3 py-2 bg-slate-800 border border-slate-700 rounded-lg text-white"
                  />
                </div>
              </div>

              <div className="pt-2">
                <p className="text-slate-400 font-semibold mb-2">Category Reward Rates (%):</p>
                <div className="grid grid-cols-2 gap-2">
                  {SPEND_CATEGORIES.map(cat => {
                    const rule = editingCard.rules.find(r => r.category === cat.id);
                    const currentRate = rule ? rule.rate : editingCard.baseRate;
                    return (
                      <div key={cat.id} className="flex items-center justify-between p-2 bg-slate-800/80 rounded-lg border border-slate-700/60">
                        <span className="text-slate-300 capitalize">{cat.id}:</span>
                        <input
                          type="number"
                          step="0.5"
                          value={currentRate}
                          onChange={(e) => {
                            const newRate = parseFloat(e.target.value) || 0;
                            const existingIndex = editingCard.rules.findIndex(r => r.category === cat.id);
                            let newRules = [...editingCard.rules];
                            if (existingIndex >= 0) {
                              newRules[existingIndex] = { ...newRules[existingIndex], rate: newRate };
                            } else {
                              newRules.push({ category: cat.id, rate: newRate });
                            }
                            setEditingCard({ ...editingCard, rules: newRules });
                          }}
                          className="w-16 px-2 py-1 bg-slate-900 border border-slate-700 rounded text-right text-white font-bold"
                        />
                      </div>
                    );
                  })}
                </div>
              </div>
            </div>

            <div className="pt-3 border-t border-slate-800 flex justify-end gap-2">
              <button
                type="button"
                onClick={() => { setEditingCard(null); setIsAddingNew(false); }}
                className="px-4 py-2 rounded-xl bg-slate-800 hover:bg-slate-700 text-slate-300 text-xs font-semibold"
              >
                Cancel
              </button>
              <button
                type="button"
                onClick={() => editingCard.name && handleSaveCard(editingCard)}
                className="px-5 py-2 rounded-xl bg-indigo-600 hover:bg-indigo-500 text-white text-xs font-bold shadow-md"
              >
                Save Payment Method
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};
