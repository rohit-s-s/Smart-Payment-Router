enum SpendCategory {
  dining('Dining & Food', 'Restaurants, cafes, food delivery (Swiggy, Zomato)'),
  groceries('Groceries', 'Supermarkets, Blinkit, Zepto, daily essentials'),
  fuel('Fuel', 'Petrol pumps, EV charging, HPCL, IOCL, Shell'),
  travel('Travel & Transport', 'Flights, trains (IRCTC), cabs (Uber, Ola), hotels'),
  utilities('Utilities & Bills', 'Electricity, water, gas, broadband, recharge'),
  shopping('Shopping', 'Apparel, electronics, Amazon, Flipkart, retail'),
  entertainment('Entertainment', 'Movies (PVR), BookMyShow, events, streaming'),
  other('Other', 'General stores, miscellaneous spend');

  final String displayName;
  final String description;

  const SpendCategory(this.displayName, this.description);

  static SpendCategory fromKey(String? key) {
    if (key == null) return SpendCategory.other;
    final normalized = key.trim().toLowerCase();
    for (final cat in SpendCategory.values) {
      if (cat.name.toLowerCase() == normalized) return cat;
    }
    return SpendCategory.other;
  }
}
