import '../models/spend_category.dart';

class MccDirectory {
  static const Map<String, SpendCategory> _mccToCategory = {
    // Dining & Food (Restaurants, Caterers, Fast Food, Bakeries, Bars)
    '5811': SpendCategory.dining, // Caterers
    '5812': SpendCategory.dining, // Eating Places and Restaurants
    '5813': SpendCategory.dining, // Drinking Places (Alcoholic Beverages)
    '5814': SpendCategory.dining, // Fast Food Restaurants
    '5462': SpendCategory.dining, // Bakeries

    // Groceries & Supermarkets
    '5411': SpendCategory.groceries, // Grocery Stores, Supermarkets
    '5422': SpendCategory.groceries, // Meat Provisions
    '5441': SpendCategory.groceries, // Candy, Nut, Confectionery Stores
    '5451': SpendCategory.groceries, // Dairy Products Stores
    '5499': SpendCategory.groceries, // Misc Food Stores - Convenience Stores

    // Fuel & EV Charging
    '5541': SpendCategory.fuel, // Service Stations (with or without Ancillary Services)
    '5542': SpendCategory.fuel, // Automated Fuel Dispensers
    '5552': SpendCategory.fuel, // Electric Vehicle Charging
    '5983': SpendCategory.fuel, // Fuel Dealers (Coal, Fuel Oil, Wood)

    // Travel & Transport (Airlines, Railways, Taxis, Hotels, Tolls)
    '3000': SpendCategory.travel, // Airlines
    '4111': SpendCategory.travel, // Local Commuter Transport, Ferries
    '4112': SpendCategory.travel, // Passenger Railways (e.g. IRCTC)
    '4121': SpendCategory.travel, // Taxicabs, Limousines, Ride-share (Uber, Ola)
    '4131': SpendCategory.travel, // Bus Lines
    '4511': SpendCategory.travel, // Air Carriers, Airlines
    '4722': SpendCategory.travel, // Travel Agencies, Tour Operators
    '4784': SpendCategory.travel, // Tolls and Bridge Fees
    '4789': SpendCategory.travel, // Transportation Services Not Elsewhere Classified
    '7011': SpendCategory.travel, // Hotels, Motels, Resorts
    '7512': SpendCategory.travel, // Automobile Rental Agency

    // Utilities & Telecom Bills
    '4814': SpendCategory.utilities, // Telecommunication Services (Mobile recharge)
    '4899': SpendCategory.utilities, // Cable and other pay Television Services
    '4900': SpendCategory.utilities, // Utilities - Electric, Gas, Water, Sanitary

    // Shopping & Retail
    '5311': SpendCategory.shopping, // Department Stores
    '5331': SpendCategory.shopping, // Variety Stores
    '5399': SpendCategory.shopping, // Misc General Merchandise
    '5611': SpendCategory.shopping, // Men's and Boy's Clothing
    '5621': SpendCategory.shopping, // Women's Ready-to-Wear
    '5651': SpendCategory.shopping, // Family Clothing Stores
    '5661': SpendCategory.shopping, // Shoe Stores
    '5712': SpendCategory.shopping, // Furniture, Home Furnishings
    '5722': SpendCategory.shopping, // Household Appliance Stores
    '5732': SpendCategory.shopping, // Electronic Sales
    '5912': SpendCategory.shopping, // Drug Stores and Pharmacies
    '5942': SpendCategory.shopping, // Book Stores
    '5944': SpendCategory.shopping, // Jewelry Stores
    '5945': SpendCategory.shopping, // Hobby, Toy, and Game Shops
    '5977': SpendCategory.shopping, // Cosmetic Stores
    '5999': SpendCategory.shopping, // Miscellaneous and Specialty Retail Stores

    // Entertainment & Leisure
    '7832': SpendCategory.entertainment, // Motion Picture Theaters (PVR, INOX)
    '7911': SpendCategory.entertainment, // Dance Halls, Studios, Schools
    '7922': SpendCategory.entertainment, // Theatrical Producers, Ticket Agencies (BookMyShow)
    '7929': SpendCategory.entertainment, // Bands, Orchestras, Entertainers
    '7941': SpendCategory.entertainment, // Commercial Sports, Professional Sports Clubs
    '7991': SpendCategory.entertainment, // Tourist Attractions and Exhibits
    '7996': SpendCategory.entertainment, // Amusement Parks, Carnivals
    '7997': SpendCategory.entertainment, // Membership Clubs (Sports, Recreation, Athletic)
    '7999': SpendCategory.entertainment, // Recreation Services Not Elsewhere Classified

    // Other & Professional Services
    '6011': SpendCategory.other, // Financial Institutions - Automated Cash
    '6012': SpendCategory.other, // Financial Institutions - Merchandise & Services
    '6513': SpendCategory.other, // Real Estate Agents and Managers - Rentals
    '8011': SpendCategory.other, // Doctors
    '8021': SpendCategory.other, // Dentists
    '8062': SpendCategory.other, // Hospitals
    '8211': SpendCategory.other, // Elementary and Secondary Schools
    '8220': SpendCategory.other, // Colleges, Universities
    '8661': SpendCategory.other, // Religious Organizations
    '8999': SpendCategory.other, // Professional Services
  };

  static SpendCategory? lookup(String? mcc) {
    if (mcc == null) return null;
    final clean = mcc.trim();
    return _mccToCategory[clean];
  }
}
