import React, { useState } from 'react';
import { Offer, PaymentMethod, SpendCategory, SPEND_CATEGORIES } from '../types';
import { Tag, Plus, Trash2, Check, X, AlertTriangle, Layers } from 'lucide-react';

interface Props {
  offers: Offer[];
  methods: PaymentMethod[];
  onUpdateOffers: (updated: Offer[]) => void;
}

export const OffersManager: React.FC<Props> = ({ offers, methods, onUpdateOffers }) => {
  const [isAdding, setIsAdding] = useState<boolean>(false);
  const [newOffer, setNewOffer] = useState<Offer>({
    id: '',
    methodId: methods[0]?.id || '',
    title: '',
    description: '',
    categories: ['dining'],
    minAmount: 500,
    discountPercent: 10,
    maxDiscount: 100,
    startDate: new Date().toISOString().split('T')[0],
    endDate: '2026-12-31',
    usageCap: 5,
    usageCount: 0,
    stacks: true,
    requiresActivation: false,
    isActive: true
  });

  const toggleActive = (id: string) => {
    const updated = offers.map(o => o.id === id ? { ...o, isActive: !o.isActive } : o);
    onUpdateOffers(updated);
  };

  const deleteOffer = (id: string) => {
    if (confirm('Delete this offer?')) {
      onUpdateOffers(offers.filter(o => o.id !== id));
    }
  };

  const handleCreateOffer = () => {
    if (!newOffer.title) return;
    const toSave: Offer = {
      ...newOffer,
      id: `offer_${Date.now()}`
    };
    onUpdateOffers([...offers, toSave]);
    setIsAdding(false);
  };

  return (
    <div className="space-y-6">
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-3">
        <div>
          <h3 className="text-xl font-black text-white">Active Offers & Boosters</h3>
          <p className="text-xs text-slate-400">
            Card-specific promotions, festival discounts, and surcharge waivers.
          </p>
        </div>
        <button
          type="button"
          onClick={() => setIsAdding(true)}
          className="inline-flex items-center gap-2 px-4 py-2.5 rounded-xl bg-indigo-600 hover:bg-indigo-500 text-white font-semibold text-xs transition self-start shadow-md"
        >
          <Plus className="w-4 h-4" />
          <span>Add Offer</span>
        </button>
      </div>

      <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
        {offers.map((offer) => {
          const matchedCard = methods.find(m => m.id === offer.methodId);
          return (
            <div
              key={offer.id}
              className={`p-5 rounded-2xl border transition-all ${
                offer.isActive 
                  ? 'bg-slate-800/90 border-slate-700 shadow-xl' 
                  : 'bg-slate-900/50 border-slate-800 opacity-60'
              }`}
            >
              <div className="flex items-start justify-between gap-3 mb-2">
                <div className="flex items-center gap-2.5">
                  <div className="w-9 h-9 rounded-xl bg-indigo-500/20 text-indigo-400 flex items-center justify-center">
                    <Tag className="w-4 h-4" />
                  </div>
                  <div>
                    <h4 className="text-sm font-bold text-white leading-tight">{offer.title}</h4>
                    <p className="text-xs text-slate-400 mt-0.5">{matchedCard?.name || 'All Cards'}</p>
                  </div>
                </div>

                <button
                  type="button"
                  onClick={() => toggleActive(offer.id)}
                  className={`px-2.5 py-1 rounded-full text-[10px] font-bold uppercase tracking-wider transition ${
                    offer.isActive 
                      ? 'bg-emerald-950 text-emerald-400 border border-emerald-800/60' 
                      : 'bg-slate-800 text-slate-500 border border-slate-700'
                  }`}
                >
                  {offer.isActive ? 'Active' : 'Paused'}
                </button>
              </div>

              <p className="text-xs text-slate-300 mt-2 mb-3 leading-relaxed">
                {offer.description}
              </p>

              <div className="flex flex-wrap gap-2 text-[11px] text-slate-400 pt-2 border-t border-slate-700/60">
                <span className="px-2 py-0.5 rounded bg-slate-700/80 text-slate-300">
                  Min spend: ₹{offer.minAmount}
                </span>
                <span className="px-2 py-0.5 rounded bg-slate-700/80 text-slate-300">
                  {offer.discountPercent ? `${offer.discountPercent}% (max ₹${offer.maxDiscount || '∞'})` : `Flat ₹${offer.discountFlat}`}
                </span>
                {offer.stacks ? (
                  <span className="px-2 py-0.5 rounded bg-emerald-950/60 text-emerald-400 border border-emerald-800/40 flex items-center gap-1">
                    <Layers className="w-3 h-3" /> Stacks with reward
                  </span>
                ) : (
                  <span className="px-2 py-0.5 rounded bg-amber-950/60 text-amber-400 border border-amber-800/40">
                    Non-stackable
                  </span>
                )}
                {offer.requiresActivation && (
                  <span className="px-2 py-0.5 rounded bg-rose-950/60 text-rose-400 border border-rose-800/40 flex items-center gap-1">
                    <AlertTriangle className="w-3 h-3" /> Activation required
                  </span>
                )}
              </div>

              <div className="mt-4 pt-3 border-t border-slate-700/60 flex items-center justify-between text-[11px] text-slate-500">
                <span>Usage: {offer.usageCount} / {offer.usageCap}</span>
                <button
                  type="button"
                  onClick={() => deleteOffer(offer.id)}
                  className="text-slate-400 hover:text-rose-400 transition"
                  aria-label={`Delete offer ${offer.title}`}
                >
                  <Trash2 className="w-4 h-4" />
                </button>
              </div>
            </div>
          );
        })}
      </div>

      {/* Add Offer Modal */}
      {isAdding && (
        <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-slate-950/80 backdrop-blur-sm">
          <div className="w-full max-w-lg rounded-2xl bg-slate-900 border border-slate-700 p-6 space-y-4 max-h-[90vh] overflow-y-auto">
            <div className="flex items-center justify-between">
              <h3 className="text-lg font-bold text-white">Add New Card Offer</h3>
              <button 
                type="button" 
                onClick={() => setIsAdding(false)}
                className="text-slate-400 hover:text-white"
              >
                <X className="w-5 h-5" />
              </button>
            </div>

            <div className="space-y-3 text-xs">
              <div>
                <label className="block text-slate-400 mb-1">Applicable Payment Method</label>
                <select
                  value={newOffer.methodId}
                  onChange={(e) => setNewOffer({ ...newOffer, methodId: e.target.value })}
                  className="w-full px-3 py-2 bg-slate-800 border border-slate-700 rounded-lg text-white"
                >
                  {methods.map(m => (
                    <option key={m.id} value={m.id}>{m.name} ({m.issuer})</option>
                  ))}
                </select>
              </div>

              <div>
                <label className="block text-slate-400 mb-1">Offer Title</label>
                <input
                  type="text"
                  value={newOffer.title}
                  onChange={(e) => setNewOffer({ ...newOffer, title: e.target.value })}
                  placeholder="e.g. 10% Extra on Dining with HDFC"
                  className="w-full px-3 py-2 bg-slate-800 border border-slate-700 rounded-lg text-white"
                />
              </div>

              <div>
                <label className="block text-slate-400 mb-1">Description</label>
                <textarea
                  value={newOffer.description}
                  onChange={(e) => setNewOffer({ ...newOffer, description: e.target.value })}
                  rows={2}
                  className="w-full px-3 py-2 bg-slate-800 border border-slate-700 rounded-lg text-white"
                />
              </div>

              <div className="grid grid-cols-2 gap-3">
                <div>
                  <label className="block text-slate-400 mb-1">Minimum Spend (₹)</label>
                  <input
                    type="number"
                    value={newOffer.minAmount}
                    onChange={(e) => setNewOffer({ ...newOffer, minAmount: parseFloat(e.target.value) || 0 })}
                    className="w-full px-3 py-2 bg-slate-800 border border-slate-700 rounded-lg text-white"
                  />
                </div>
                <div>
                  <label className="block text-slate-400 mb-1">Discount % or Flat ₹</label>
                  <input
                    type="number"
                    value={newOffer.discountPercent || ''}
                    placeholder="% discount"
                    onChange={(e) => setNewOffer({ ...newOffer, discountPercent: parseFloat(e.target.value) || 0 })}
                    className="w-full px-3 py-2 bg-slate-800 border border-slate-700 rounded-lg text-white"
                  />
                </div>
              </div>

              <div className="flex items-center gap-4 pt-2">
                <label className="flex items-center gap-2 cursor-pointer text-slate-300">
                  <input
                    type="checkbox"
                    checked={newOffer.stacks}
                    onChange={(e) => setNewOffer({ ...newOffer, stacks: e.target.checked })}
                    className="rounded bg-slate-800 border-slate-700 text-indigo-600"
                  />
                  <span>Stacks with base reward</span>
                </label>

                <label className="flex items-center gap-2 cursor-pointer text-slate-300">
                  <input
                    type="checkbox"
                    checked={newOffer.requiresActivation}
                    onChange={(e) => setNewOffer({ ...newOffer, requiresActivation: e.target.checked })}
                    className="rounded bg-slate-800 border-slate-700 text-indigo-600"
                  />
                  <span>Requires card app activation</span>
                </label>
              </div>
            </div>

            <div className="pt-3 border-t border-slate-800 flex justify-end gap-2">
              <button
                type="button"
                onClick={() => setIsAdding(false)}
                className="px-4 py-2 rounded-xl bg-slate-800 hover:bg-slate-700 text-slate-300 text-xs font-semibold"
              >
                Cancel
              </button>
              <button
                type="button"
                onClick={handleCreateOffer}
                className="px-5 py-2 rounded-xl bg-indigo-600 hover:bg-indigo-500 text-white text-xs font-bold shadow-md"
              >
                Save Offer
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};
