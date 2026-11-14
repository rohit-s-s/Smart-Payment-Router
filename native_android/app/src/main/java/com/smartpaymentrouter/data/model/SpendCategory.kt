package com.smartpaymentrouter.data.model

/**
 * The 8 fixed spend categories defined in Epic 1 (US-03)
 */
enum class SpendCategory(
    val displayName: String,
    val description: String
) {
    DINING("Dining & Food", "Restaurants, cafes, food delivery (Swiggy, Zomato)"),
    GROCERIES("Groceries", "Supermarkets, Blinkit, Zepto, daily essentials"),
    FUEL("Fuel", "Petrol pumps, EV charging, HPCL, IOCL, Shell"),
    TRAVEL("Travel & Transport", "Flights, trains (IRCTC), cabs (Uber, Ola), hotels"),
    UTILITIES("Utilities & Bills", "Electricity, water, gas, broadband, recharge"),
    SHOPPING("Shopping", "Apparel, electronics, Amazon, Flipkart, retail"),
    ENTERTAINMENT("Entertainment", "Movies (PVR), BookMyShow, events, streaming"),
    OTHER("Other", "General stores, miscellaneous spend");

    companion object {
        fun fromKey(key: String?): SpendCategory {
            if (key == null) return OTHER
            return try {
                valueOf(key.trim().uppercase())
            } catch (e: Exception) {
                OTHER
            }
        }
    }
}
