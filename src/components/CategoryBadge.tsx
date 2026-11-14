import React from 'react';
import { SpendCategory, SPEND_CATEGORIES } from '../types';
import { Utensils, ShoppingCart, Fuel, Plane, Zap, ShoppingBag, Film, HelpCircle } from 'lucide-react';

interface Props {
  category: SpendCategory;
  source?: 'mcc' | 'merchant_history' | 'manual' | 'default';
  mccCode?: string;
  className?: string;
  showSource?: boolean;
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

export const CategoryBadge: React.FC<Props> = ({
  category,
  source,
  mccCode,
  className = '',
  showSource = true,
}) => {
  const catInfo = SPEND_CATEGORIES.find(c => c.id === category) || SPEND_CATEGORIES[7];
  const IconComponent = ICON_MAP[category] || HelpCircle;

  return (
    <div className={`inline-flex items-center gap-2 px-3 py-1.5 rounded-full text-xs font-medium border bg-slate-800/80 border-slate-700 shadow-sm ${className}`}>
      <span className={`w-5 h-5 rounded-full bg-gradient-to-r ${catInfo.color} flex items-center justify-center text-white`}>
        <IconComponent className="w-3 h-3" />
      </span>
      <span className="text-slate-200 font-semibold">{catInfo.label}</span>

      {showSource && source && (
        <span 
          className="text-[10px] uppercase tracking-wider px-1.5 py-0.5 rounded bg-slate-700 text-slate-300"
          title={`Category derived via: ${source}`}
        >
          {source === 'mcc' ? `MCC ${mccCode || 'Auto'}` :
           source === 'merchant_history' ? 'Past Merchant' :
           source === 'manual' ? 'Manual' : 'Default'}
        </span>
      )}
    </div>
  );
};
