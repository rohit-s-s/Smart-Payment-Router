import React from 'react';
import { PaymentLog } from '../types';
import { CategoryBadge } from './CategoryBadge';
import { History, TrendingUp, CreditCard, Scan, Edit3, Award, Clock } from 'lucide-react';

interface Props {
  logs: PaymentLog[];
  onClearLogs: () => void;
}

export const PaymentHistory: React.FC<Props> = ({ logs, onClearLogs }) => {
  const totalRewards = logs.reduce((sum, log) => sum + (log.expectedReward || 0), 0);
  const totalSpend = logs.reduce((sum, log) => sum + log.amount, 0);
  const avgReturn = totalSpend > 0 ? ((totalRewards / totalSpend) * 100).toFixed(1) : '0.0';

  const scannedCount = logs.filter(l => l.source === 'scan').length;
  const manualCount = logs.filter(l => l.source === 'manual').length;

  return (
    <div className="space-y-6">
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-3">
        <div>
          <h3 className="text-xl font-black text-white">Payment Log & Rewards Earned</h3>
          <p className="text-xs text-slate-400">
            Track confirmed payments, actual rewards, and routing performance.
          </p>
        </div>
        {logs.length > 0 && (
          <button
            type="button"
            onClick={() => {
              if (confirm('Clear payment log history?')) onClearLogs();
            }}
            className="text-xs text-slate-400 hover:text-rose-400 transition self-start"
          >
            Clear Log
          </button>
        )}
      </div>

      {/* Metrics Cards */}
      <div className="grid grid-cols-2 sm:grid-cols-4 gap-3">
        <div className="p-4 rounded-xl bg-slate-800/80 border border-slate-700/80">
          <p className="text-[11px] font-semibold text-slate-400 uppercase tracking-wider">Total Rewards</p>
          <p className="text-2xl font-black text-emerald-400 mt-1">+₹{totalRewards.toFixed(2)}</p>
          <p className="text-[10px] text-slate-500 mt-0.5">Optimized routing</p>
        </div>

        <div className="p-4 rounded-xl bg-slate-800/80 border border-slate-700/80">
          <p className="text-[11px] font-semibold text-slate-400 uppercase tracking-wider">Total Routed</p>
          <p className="text-2xl font-black text-white mt-1">₹{totalSpend.toFixed(0)}</p>
          <p className="text-[10px] text-slate-500 mt-0.5">{logs.length} transactions</p>
        </div>

        <div className="p-4 rounded-xl bg-slate-800/80 border border-slate-700/80">
          <p className="text-[11px] font-semibold text-slate-400 uppercase tracking-wider">Avg Return</p>
          <p className="text-2xl font-black text-indigo-400 mt-1">{avgReturn}%</p>
          <p className="text-[10px] text-slate-500 mt-0.5">Weighted cashback</p>
        </div>

        <div className="p-4 rounded-xl bg-slate-800/80 border border-slate-700/80">
          <p className="text-[11px] font-semibold text-slate-400 uppercase tracking-wider">Capture Source</p>
          <div className="flex items-center gap-2 mt-1.5 text-xs text-slate-300">
            <span className="flex items-center gap-1 font-bold text-white">
              <Scan className="w-3.5 h-3.5 text-indigo-400" /> {scannedCount}
            </span>
            <span className="text-slate-500">/</span>
            <span className="flex items-center gap-1 font-bold text-white">
              <Edit3 className="w-3.5 h-3.5 text-amber-400" /> {manualCount}
            </span>
          </div>
          <p className="text-[10px] text-slate-500 mt-0.5">QR Scans / Manual</p>
        </div>
      </div>

      {/* Transaction Log List */}
      {logs.length === 0 ? (
        <div className="p-10 text-center bg-slate-800/50 rounded-2xl border border-slate-800">
          <History className="w-10 h-10 text-slate-600 mx-auto mb-2" />
          <h4 className="text-sm font-bold text-slate-300">No Transactions Yet</h4>
          <p className="text-xs text-slate-500 max-w-xs mx-auto mt-1">
            Scan a QR code or enter a payment to route with the best card and start earning rewards.
          </p>
        </div>
      ) : (
        <div className="space-y-2.5">
          {logs.map((log) => {
            const dateStr = new Date(log.timestamp).toLocaleDateString('en-IN', {
              day: 'numeric',
              month: 'short',
              hour: '2-digit',
              minute: '2-digit'
            });

            return (
              <div
                key={log.id}
                className="p-4 rounded-xl bg-slate-800/80 border border-slate-700/80 flex flex-col sm:flex-row sm:items-center justify-between gap-3"
              >
                <div className="flex items-start gap-3">
                  <div className="w-9 h-9 rounded-xl bg-slate-700/80 text-slate-300 flex items-center justify-center shrink-0 mt-0.5">
                    {log.source === 'scan' ? <Scan className="w-4 h-4 text-indigo-400" /> : <Edit3 className="w-4 h-4 text-amber-400" />}
                  </div>

                  <div>
                    <div className="flex items-center gap-2">
                      <h5 className="text-sm font-bold text-white">{log.merchantName}</h5>
                      <span className="text-[10px] font-mono px-1.5 py-0.5 rounded bg-slate-700 text-slate-400 uppercase">
                        {log.source}
                      </span>
                    </div>

                    <p className="text-xs text-slate-400 mt-0.5">
                      Paid via <span className="text-slate-200 font-medium">{log.chosenMethodName}</span>
                    </p>

                    <div className="flex items-center gap-2 mt-1.5">
                      <CategoryBadge category={log.category} showSource={false} />
                      <span className="text-[10px] text-slate-500 flex items-center gap-1">
                        <Clock className="w-3 h-3" /> {dateStr}
                      </span>
                    </div>
                  </div>
                </div>

                <div className="sm:text-right pt-2 sm:pt-0 border-t sm:border-0 border-slate-700/50 flex sm:flex-col justify-between sm:justify-center items-center sm:items-end">
                  <span className="text-base font-bold text-white">₹{log.amount.toFixed(2)}</span>
                  <span className="text-xs font-bold text-emerald-400">
                    +₹{log.expectedReward.toFixed(2)} reward
                  </span>
                </div>
              </div>
            );
          })}
        </div>
      )}
    </div>
  );
};
