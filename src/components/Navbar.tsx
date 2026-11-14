import React from 'react';
import { Scan, Edit3, CreditCard, Tag, History, WifiOff, Sparkles, RefreshCw } from 'lucide-react';

export type NavTab = 'scan' | 'manual' | 'cards' | 'offers' | 'history';

interface Props {
  activeTab: NavTab;
  onSelectTab: (tab: NavTab) => void;
  onResetData: () => void;
}

export const Navbar: React.FC<Props> = ({ activeTab, onSelectTab, onResetData }) => {
  return (
    <header className="sticky top-0 z-40 bg-slate-900/95 backdrop-blur-md border-b border-slate-800 shadow-xl">
      <div className="max-w-5xl mx-auto px-4 sm:px-6">
        <div className="flex items-center justify-between h-16">
          {/* Logo & Brand */}
          <div className="flex items-center gap-3">
            <div className="w-9 h-9 rounded-xl bg-gradient-to-tr from-indigo-600 via-indigo-500 to-emerald-400 flex items-center justify-center text-white shadow-lg shadow-indigo-600/30">
              <Sparkles className="w-5 h-5 text-amber-200 fill-amber-200/40" />
            </div>
            <div>
              <div className="flex items-center gap-2">
                <h1 className="text-base sm:text-lg font-black text-white tracking-tight">
                  Smart Payment Router
                </h1>
                <span className="text-[10px] font-mono px-1.5 py-0.5 rounded bg-indigo-950 text-indigo-300 border border-indigo-800/60 hidden sm:inline">
                  v1.0
                </span>
              </div>
              <p className="text-[10px] text-slate-400 hidden sm:block">
                Max cashback optimizer for UPI & cards
              </p>
            </div>
          </div>

          {/* Right Status & Tools */}
          <div className="flex items-center gap-2 sm:gap-3">
            {/* Offline Ready Badge */}
            <div 
              className="inline-flex items-center gap-1.5 px-2.5 py-1 rounded-full bg-slate-800 text-[11px] font-medium text-slate-300 border border-slate-700"
              title="Recommendations run 100% locally and work offline"
            >
              <span className="w-2 h-2 rounded-full bg-emerald-400 animate-pulse" />
              <span className="hidden sm:inline">Offline Ready</span>
            </div>

            {/* Reset Data Button */}
            <button
              type="button"
              onClick={onResetData}
              title="Reset default cards, offers & sample data"
              aria-label="Reset default demo data"
              className="p-2 rounded-xl text-slate-400 hover:text-white hover:bg-slate-800 transition text-xs flex items-center gap-1"
            >
              <RefreshCw className="w-3.5 h-3.5" />
              <span className="hidden md:inline">Reset Defaults</span>
            </button>
          </div>
        </div>

        {/* Tab Navigation Navigation Bar */}
        <nav 
          className="flex items-center space-x-1 overflow-x-auto no-scrollbar py-2 -mx-4 px-4 sm:mx-0 sm:px-0 border-t border-slate-800/80"
          role="tablist"
          aria-label="Main Navigation"
        >
          <button
            type="button"
            role="tab"
            aria-selected={activeTab === 'scan'}
            onClick={() => onSelectTab('scan')}
            className={`flex items-center gap-2 px-3.5 py-2 rounded-xl text-xs font-bold whitespace-nowrap transition-all ${
              activeTab === 'scan'
                ? 'bg-indigo-600 text-white shadow-md shadow-indigo-600/30'
                : 'text-slate-400 hover:text-slate-200 hover:bg-slate-800/60'
            }`}
          >
            <Scan className="w-4 h-4" />
            <span>Scan QR (US-01)</span>
          </button>

          <button
            type="button"
            role="tab"
            aria-selected={activeTab === 'manual'}
            onClick={() => onSelectTab('manual')}
            className={`flex items-center gap-2 px-3.5 py-2 rounded-xl text-xs font-bold whitespace-nowrap transition-all ${
              activeTab === 'manual'
                ? 'bg-indigo-600 text-white shadow-md shadow-indigo-600/30'
                : 'text-slate-400 hover:text-slate-200 hover:bg-slate-800/60'
            }`}
          >
            <Edit3 className="w-4 h-4" />
            <span>Manual Pay (US-02)</span>
          </button>

          <button
            type="button"
            role="tab"
            aria-selected={activeTab === 'cards'}
            onClick={() => onSelectTab('cards')}
            className={`flex items-center gap-2 px-3.5 py-2 rounded-xl text-xs font-bold whitespace-nowrap transition-all ${
              activeTab === 'cards'
                ? 'bg-indigo-600 text-white shadow-md shadow-indigo-600/30'
                : 'text-slate-400 hover:text-slate-200 hover:bg-slate-800/60'
            }`}
          >
            <CreditCard className="w-4 h-4" />
            <span>My Cards & Rules</span>
          </button>

          <button
            type="button"
            role="tab"
            aria-selected={activeTab === 'offers'}
            onClick={() => onSelectTab('offers')}
            className={`flex items-center gap-2 px-3.5 py-2 rounded-xl text-xs font-bold whitespace-nowrap transition-all ${
              activeTab === 'offers'
                ? 'bg-indigo-600 text-white shadow-md shadow-indigo-600/30'
                : 'text-slate-400 hover:text-slate-200 hover:bg-slate-800/60'
            }`}
          >
            <Tag className="w-4 h-4" />
            <span>Offers</span>
          </button>

          <button
            type="button"
            role="tab"
            aria-selected={activeTab === 'history'}
            onClick={() => onSelectTab('history')}
            className={`flex items-center gap-2 px-3.5 py-2 rounded-xl text-xs font-bold whitespace-nowrap transition-all ${
              activeTab === 'history'
                ? 'bg-indigo-600 text-white shadow-md shadow-indigo-600/30'
                : 'text-slate-400 hover:text-slate-200 hover:bg-slate-800/60'
            }`}
          >
            <History className="w-4 h-4" />
            <span>Payment Log</span>
          </button>
        </nav>
      </div>
    </header>
  );
};
