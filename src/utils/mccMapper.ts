import { SpendCategory } from '../types';

export interface MCCInfo {
  code: string;
  category: SpendCategory;
  name: string;
  examples: string[];
}

export const MCC_DIRECTORY: Record<string, MCCInfo> = {
  // Dining
  '5812': { code: '5812', category: 'dining', name: 'Restaurants & Eating Places', examples: ['Dine-in restaurants', 'Cafes', 'Bistros'] },
  '5813': { code: '5813', category: 'dining', name: 'Bars, Lounges & Nightclubs', examples: ['Pubs', 'Breweries', 'Cocktail lounges'] },
  '5814': { code: '5814', category: 'dining', name: 'Fast Food & Quick Service', examples: ["McDonald's", 'KFC', 'Burger King', 'Street Food'] },
  '5462': { code: '5462', category: 'dining', name: 'Bakeries', examples: ['Theobroma', 'Cake shops', 'Artisan bakeries'] },

  // Groceries
  '5411': { code: '5411', category: 'groceries', name: 'Grocery Stores & Supermarkets', examples: ['D-Mart', 'BigBasket', 'Blinkit', 'Zepto', 'Nature Basket'] },
  '5422': { code: '5422', category: 'groceries', name: 'Meat & Poultry Provisions', examples: ['Licious', 'FreshToHome', 'Local meat stalls'] },
  '5441': { code: '5441', category: 'groceries', name: 'Candy & Confectionery Stores', examples: ['Sweet shops', 'Haldirams', 'Chocolatiers'] },
  '5451': { code: '5451', category: 'groceries', name: 'Dairy Products Stores', examples: ['Amul parlours', 'Mother Dairy', 'Nandini'] },
  '5499': { code: '5499', category: 'groceries', name: 'Convenience Stores & Markets', examples: ['24/7 stores', 'Local Kirana shops'] },

  // Fuel
  '5541': { code: '5541', category: 'fuel', name: 'Service Stations / Petrol Pumps', examples: ['IndianOil', 'HPCL', 'BPCL', 'Shell'] },
  '5542': { code: '5542', category: 'fuel', name: 'Automated Fuel Dispensers', examples: ['Self-service fuel pumps'] },
  '5552': { code: '5552', category: 'fuel', name: 'EV Charging Stations', examples: ['Tata Power EV', 'Ather Grid', 'Jio-bp pulse'] },

  // Travel
  '4111': { code: '4111', category: 'travel', name: 'Commuter Passenger Transit', examples: ['Metro Rail', 'Local trains', 'Ferry'] },
  '4112': { code: '4112', category: 'travel', name: 'Passenger Railways', examples: ['IRCTC', 'Indian Railways'] },
  '4121': { code: '4121', category: 'travel', name: 'Taxicabs & Ride Sharing', examples: ['Uber', 'Ola', 'Rapido', 'BluSmart'] },
  '4131': { code: '4131', category: 'travel', name: 'Bus Lines', examples: ['KSRTC', 'RedBus', 'Chalo', 'Intercity bus'] },
  '4511': { code: '4511', category: 'travel', name: 'Airlines & Air Carriers', examples: ['IndiGo', 'Air India', 'Akasa Air', 'SpiceJet'] },
  '4722': { code: '4722', category: 'travel', name: 'Travel Agencies & Tour Operators', examples: ['MakeMyTrip', 'Cleartrip', 'Yatra', 'EaseMyTrip'] },
  '7011': { code: '7011', category: 'travel', name: 'Hotels, Motels & Resorts', examples: ['Taj Hotels', 'Marriott', 'OYO', 'Airbnb'] },

  // Utilities
  '4900': { code: '4900', category: 'utilities', name: 'Utilities - Electric, Gas, Water', examples: ['BESCOM', 'Tata Power', 'Mahanagar Gas', 'BWSSB'] },
  '4814': { code: '4814', category: 'utilities', name: 'Telecom Services & Recharge', examples: ['Jio', 'Airtel', 'Vi Mobile Recharge'] },
  '4899': { code: '4899', category: 'utilities', name: 'Cable, Broadband & Satellite', examples: ['ACT Fibernet', 'Tata Play', 'Airtel Xstream'] },

  // Shopping
  '5311': { code: '5311', category: 'shopping', name: 'Department Stores', examples: ['Shoppers Stop', 'Lifestyle', 'Westside'] },
  '5331': { code: '5331', category: 'shopping', name: 'Variety Stores', examples: ['Miniso', 'Market99'] },
  '5399': { code: '5399', category: 'shopping', name: 'General Merchandise', examples: ['Amazon India', 'Flipkart', 'Meesho'] },
  '5611': { code: '5611', category: 'shopping', name: "Men's Apparel & Accessories", examples: ['Raymond', 'Peter England', 'Rare Rabbit'] },
  '5621': { code: '5621', category: 'shopping', name: "Women's Ready-to-Wear", examples: ['Zara', 'H&M', 'Biba', 'FabIndia'] },
  '5651': { code: '5651', category: 'shopping', name: 'Family Clothing Stores', examples: ['Max Fashion', 'Pantaloons', 'Uniqlo'] },
  '5732': { code: '5732', category: 'shopping', name: 'Consumer Electronics & Gadgets', examples: ['Croma', 'Reliance Digital', 'Apple Store'] },
  '5942': { code: '5942', category: 'shopping', name: 'Book Stores', examples: ['Crossword', 'Sapna Book House'] },
  '5999': { code: '5999', category: 'shopping', name: 'Miscellaneous Retail', examples: ['Local shops', 'Gift shops', 'Stationery'] },

  // Entertainment
  '7832': { code: '7832', category: 'entertainment', name: 'Motion Picture Theaters', examples: ['PVR Cinemas', 'INOX', 'Cinepolis'] },
  '7922': { code: '7922', category: 'entertainment', name: 'Theatrical Producers & Ticket Agencies', examples: ['BookMyShow', 'Insider.in'] },
  '7991': { code: '7991', category: 'entertainment', name: 'Tourist Attractions & Museums', examples: ['Theme parks', 'Science museums', 'Zoos'] },
  '7996': { code: '7996', category: 'entertainment', name: 'Amusement Parks & Arcades', examples: ['Wonderla', 'Timezone', 'Smaaash'] },
  '7999': { code: '7999', category: 'entertainment', name: 'Recreation & Gaming Services', examples: ['Bowling alleys', 'Gaming arenas'] },
};

/**
 * Maps a 4-digit MCC string to the 8 fixed SpendCategories
 */
export function mapMccToCategory(mcc?: string): { category: SpendCategory; name?: string; examples?: string[] } {
  if (!mcc) {
    return { category: 'other' };
  }

  const cleanMcc = mcc.trim();
  const info = MCC_DIRECTORY[cleanMcc];

  if (info) {
    return {
      category: info.category,
      name: info.name,
      examples: info.examples
    };
  }

  // Range-based fallback for standard ISO 18245 MCC allocations
  const numeric = parseInt(cleanMcc, 10);
  if (!isNaN(numeric)) {
    if ((numeric >= 5811 && numeric <= 5814) || numeric === 5462) {
      return { category: 'dining', name: 'Dining & Food' };
    }
    if (numeric >= 5400 && numeric <= 5499) {
      return { category: 'groceries', name: 'Groceries' };
    }
    if (numeric >= 5541 && numeric <= 5552) {
      return { category: 'fuel', name: 'Fuel' };
    }
    if ((numeric >= 4000 && numeric <= 4799) || (numeric >= 7000 && numeric <= 7099)) {
      return { category: 'travel', name: 'Travel & Transport' };
    }
    if (numeric >= 4800 && numeric <= 4999) {
      return { category: 'utilities', name: 'Utilities & Bills' };
    }
    if ((numeric >= 5000 && numeric <= 5399) || (numeric >= 5600 && numeric <= 5799) || (numeric >= 5900 && numeric <= 5999)) {
      return { category: 'shopping', name: 'Shopping' };
    }
    if (numeric >= 7800 && numeric <= 7999) {
      return { category: 'entertainment', name: 'Entertainment' };
    }
  }

  return { category: 'other' };
}
