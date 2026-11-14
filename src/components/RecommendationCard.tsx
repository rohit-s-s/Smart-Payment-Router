import React, { useState } from 'react';
import { Recommendation, SpendCategory } from '../types';
import { 
  Trophy, ArrowUpRight, AlertTriangle, ShieldCheck, ChevronDown, 
  ChevronUp, Tag, CreditCard, Sparkles, Check, Info, CheckCircle2
} from 'lucide-react';
import { CategoryBadge } from './CategoryBadge';

interface Props {
  amount: number;
  category: SpendCategory;
  payeeName?: string;
  recommendations: Recommendation[];
  onSelectMethodToPay: (rec: Recommendation) => void;
  onModifyPayment: () => void;
}

export const RecommendationCard: React.FC<Props> = ({
  amount,
  category,
  payeeName,
  recommendations,
  onSelectMethodToPay,
  onModifyPayment,
}) => {
  const [showAllRunnersUp, setShowAllRunnersUp] = useState<boolean>(false);
  const [showBreakdown, setShowBreakdown] = useState<boolean>(false);

  if (!recommendations || recommendations.length === 0) {
    return (
      <div className="p-8 text-center bg-slate-800/80 rounded-2xl border border-slate-700">
        <CreditCard className="w-12 h-12 text-slate-500 mx-auto mb-3" />
        <h3 className="text-lg font-bold text-white mb-1">No Active Payment Cards</h3>
        <p className="text-sm text-slate-400 mb-4">
          Please add or activate at least one payment method to receive routing recommendations.
        </p>
        <button
          type="button"
          onClick={onModifyPayment}
          className="px-4 py-2 rounded-xl bg-indigo-600 text-white text-sm font-semibold hover:bg-indigo-500 transition"
        >
          Back to Payment
        </button>
      </div>
    );
  }

  const topPick = recommendations[0];
  const runnersUp = recommendations.slice(1);

  return (
    <div className="space-y-6 animate-fade-in" role="region" aria-label="Payment Routing Recommendations">
      {/* Transaction Summary Header */}
      <div className="p-4 rounded-xl bg-slate-800/70 border border-slate-700 flex flex-wrap items-center justify-between gap-3">
        <div>
          <p className="text-xs text-slate-400">Payment Routing For</p>
          <div className="flex items-center gap-2 mt-0.5">
            <span className="text-2xl font-black text-white">₹{amount.toFixed(2)}</span>
            {payeeName && (
              <span className="text-xs text-slate-300 font-medium px-2 py-0.5 rounded bg-slate-700">
                to {payeeName}
              </span>
            )}
          </div>
        </div>

        <div className="flex items-center gap-2">
          <CategoryBadge category={category} showSource={false} />
          <button
            type="button"
            onClick={onModifyPayment}
            aria-label="Change amount or category"
            className="text-xs text-indigo-400 hover:text-indigo-300 font-medium underline underline-offset-2 ml-1"
          >
            Edit
          </button>
        </div>
      </div>

      {/* TOP PICK CARD (Prominently Highlighted) */}
      <div className="relative rounded-2xl bg-gradient-to-b from-indigo-950/90 via-slate-900 to-slate-900 border-2 border-indigo-500/80 shadow-2xl p-6 overflow-hidden">
        {/* Top Winner Pill */}
        <div className="flex items-center justify-between mb-4">
          <div className="inline-flex items-center gap-1.5 px-3 py-1 rounded-full bg-amber-400 text-slate-950 font-bold text-xs shadow-md">
            <Trophy className="w-3.5 h-3.5 fill-current" />
            <span>HIGHEST REWARD PICK</span>
          </div>

          <span className="text-xs font-semibold text-emerald-400 bg-emerald-950/60 px-2.5 py-1 rounded-full border border-emerald-800/50">
            {topPick.effectiveRate}% effective return
          </span>
        </div>

        {/* Card Title & Net Estimated Reward */}
        <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4 mb-5 pb-5 border-b border-slate-800">
          <div>
            <div className="flex items-center gap-2">
              <h3 className="text-xl sm:text-2xl font-black text-white tracking-tight">
                {topPick.method.name}
              </h3>
            </div>
            <p className="text-xs text-slate-400 mt-1 flex items-center gap-2">
              <span>{topPick.method.issuer}</span>
              <span>•</span>
              <span className="capitalize">{topPick.method.type.replace('_', ' ')}</span>
              <span>•</span>
              <span>{topPick.method.network}</span>
              {topPick.method.cardLast4 && <span>(•••• {topPick.method.cardLast4})</span>}
            </p>
          </div>

          <div className="sm:text-right shrink-0">
            <p className="text-xs font-semibold text-slate-400 uppercase tracking-wider">Estimated Reward</p>
            <div className="text-3xl sm:text-4xl font-black text-emerald-400 flex items-center sm:justify-end gap-1">
              <span>+₹{topPick.effectiveReward.toFixed(2)}</span>
            </div>
          </div>
        </div>

        {/* Transparent Reason & Applied Offers */}
        <div className="space-y-3 mb-6">
          <div className="p-3.5 rounded-xl bg-slate-800/80 border border-slate-700/80 text-xs text-slate-200">
            <div className="flex items-start gap-2">
              <Sparkles className="w-4 h-4 text-amber-400 shrink-0 mt-0.5" />
              <div>
                <span className="font-semibold text-white">Why this method: </span>
                <span>{topPick.reason}</span>
              </div>
            </div>

            {topPick.offerReason && (
              <div className="flex items-start gap-2 mt-2 pt-2 border-t border-slate-700/60 text-indigo-300">
                <Tag className="w-3.5 h-3.5 text-indigo-400 shrink-0 mt-0.5" />
                <span>{topPick.offerReason}</span>
              </div>
            )}
          </div>

          {/* Outdated Rules Warning (FR-11) */}
          {topPick.ruleWarning && (
            <div className="p-3 rounded-xl bg-amber-500/10 border border-amber-500/30 text-amber-300 text-xs flex items-start gap-2">
              <AlertTriangle className="w-4 h-4 shrink-0 text-amber-400 mt-0.5" />
              <span>{topPick.ruleWarning}</span>
            </div>
          )}

          {/* Cap Progress Info */}
          {topPick.method.monthlyCap && (
            <div className="text-xs text-slate-400 px-1 flex items-center justify-between">
              <span>
                Monthly reward cap: ₹{topPick.method.monthlyCap} (₹{topPick.method.currentMonthRewardUsed} used)
              </span>
              <span className="font-mono text-slate-300">
                ₹{Math.max(0, topPick.breakdown.remainingCapAfter).toFixed(0)} remaining
              </span>
            </div>
          )}
        </div>

        {/* Expandable Reward Breakdown Toggle */}
        <div className="mb-6">
          <button
            type="button"
            onClick={() => setShowBreakdown(!showBreakdown)}
            aria-expanded={showBreakdown}
            className="text-xs text-slate-400 hover:text-slate-200 flex items-center gap-1 transition"
          >
            <span>{showBreakdown ? 'Hide calculation details' : 'View calculation details'}</span>
            {showBreakdown ? <ChevronUp className="w-3.5 h-3.5" /> : <ChevronDown className="w-3.5 h-3.5" />}
          </button>

          {showBreakdown && (
            <div className="mt-3 p-3.5 rounded-xl bg-slate-950/60 border border-slate-800 text-xs space-y-2 text-slate-300 font-mono">
              <div className="flex justify-between">
                <span>Base Rate ({topPick.method.baseRate}%):</span>
                <span>₹{topPick.breakdown.baseReward.toFixed(2)}</span>
              </div>
              {topPick.breakdown.categoryBonus > 0 && (
                <div className="flex justify-between text-indigo-300">
                  <span>Category Accelerated Bonus:</span>
                  <span>+₹{topPick.breakdown.categoryBonus.toFixed(2)}</span>
                </div>
              )}
              {topPick.breakdown.excessAtBaseRate > 0 && (
                <div className="flex justify-between text-slate-400">
                  <span>Spend Above Cap at Base Rate:</span>
                  <span>+₹{topPick.breakdown.excessAtBaseRate.toFixed(2)}</span>
                </div>
              )}
              {topPick.breakdown.offerReward > 0 && (
                <div className="flex justify-between text-emerald-300 font-semibold">
                  <span>Card / Merchant Offer Bonus:</span>
                  <span>+₹{topPick.breakdown.offerReward.toFixed(2)}</span>
                </div>
              )}
              <div className="flex justify-between pt-2 border-t border-slate-800 font-bold text-white">
                <span>Total Expected Reward:</span>
                <span>₹{topPick.effectiveReward.toFixed(2)}</span>
              </div>
            </div>
          )}
        </div>

        {/* Proceed to Pay Action Button */}
        <button
          type="button"
          onClick={() => onSelectMethodToPay(topPick)}
          aria-label={`Pay with ${topPick.method.name} and earn ₹${topPick.effectiveReward.toFixed(2)}`}
          className="w-full py-4 px-6 rounded-xl font-bold text-base text-white bg-gradient-to-r from-emerald-600 via-teal-600 to-indigo-600 hover:from-emerald-500 hover:to-indigo-500 shadow-lg shadow-emerald-900/30 flex items-center justify-center gap-2 hover:scale-[1.01] active:scale-[0.99] transition cursor-pointer"
        >
          <span>Pay with {topPick.method.name}</span>
          <ArrowUpRight className="w-5 h-5" />
        </button>
      </div>

      {/* RUNNER-UP COMPARISON LIST (Requirements FR-6) */}
      {runnersUp.length > 0 && (
        <div className="space-y-3">
          <div className="flex items-center justify-between">
            <h4 className="text-sm font-bold text-slate-300">Other Eligible Cards & Methods</h4>
            <span className="text-xs text-slate-500">{runnersUp.length} alternatives</span>
          </div>

          <div className="space-y-2">
            {(showAllRunnersUp ? runnersUp : runnersUp.slice(0, 2)).map((rec, index) => {
              const diff = topPick.effectiveReward - rec.effectiveReward;
              return (
                <div
                  key={rec.method.id}
                  className="p-4 rounded-xl bg-slate-800/70 border border-slate-700/80 hover:border-slate-600 transition flex items-center justify-between gap-3"
                >
                  <div className="min-w-0 flex-1">
                    <div className="flex items-center gap-2">
                      <span className="text-xs font-mono text-slate-500">#{index + 2}</span>
                      <h5 className="text-sm font-bold text-slate-200 truncate">{rec.method.name}</h5>
                    </div>
                    <p className="text-xs text-slate-400 mt-0.5 line-clamp-1">{rec.reason}</p>
                  </div>

                  <div className="text-right shrink-0 flex items-center gap-3">
                    <div>
                      <p className="text-sm font-bold text-slate-300">₹{rec.effectiveReward.toFixed(2)}</p>
                      {diff > 0 && (
                        <p className="text-[10px] text-rose-400">-₹{diff.toFixed(2)} less</p>
                      )}
                    </div>

                    <button
                      type="button"
                      onClick={() => onSelectMethodToPay(rec)}
                      aria-label={`Select ${rec.method.name} instead`}
                      className="px-3 py-1.5 rounded-lg text-xs font-semibold bg-slate-700 hover:bg-slate-600 text-slate-200 transition"
                    >
                      Use
                    </button>
                  </div>
                </div>
              );
            })}
          </div>

          {runnersUp.length > 2 && (
            <button
              type="button"
              onClick={() => setShowAllRunnersUp(!showAllRunnersUp)}
              className="text-xs text-indigo-400 hover:text-indigo-300 font-semibold block text-center w-full py-1.5 transition"
            >
              {showAllRunnersUp ? 'Show fewer cards' : `View all ${runnersUp.length} methods`}
            </button>
          )}
        </div>
      )}

      {/* Compliance Disclaimer (FR-14) */}
      <p className="text-[11px] text-slate-500 text-center leading-relaxed">
        Disclaimer: Smart Payment Router does not process payments or store card numbers. Estimated rewards are calculated from user-maintained rules and active offers.
      </p>
    </div>
  );
};
