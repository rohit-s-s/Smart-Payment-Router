package com.smartpaymentrouter.data.local

import com.smartpaymentrouter.data.model.SpendCategory

data class MccInfo(
    val code: String,
    val category: SpendCategory,
    val name: String
)

object MccDirectory {
    private val directory = mapOf(
        // Dining
        "5812" to MccInfo("5812", SpendCategory.DINING, "Restaurants & Eating Places"),
        "5813" to MccInfo("5813", SpendCategory.DINING, "Bars & Lounges"),
        "5814" to MccInfo("5814", SpendCategory.DINING, "Fast Food & Quick Service"),
        "5462" to MccInfo("5462", SpendCategory.DINING, "Bakeries"),

        // Groceries
        "5411" to MccInfo("5411", SpendCategory.GROCERIES, "Grocery Stores & Supermarkets"),
        "5422" to MccInfo("5422", SpendCategory.GROCERIES, "Meat Provisions"),
        "5441" to MccInfo("5441", SpendCategory.GROCERIES, "Confectionery"),
        "5451" to MccInfo("5451", SpendCategory.GROCERIES, "Dairy Products"),
        "5499" to MccInfo("5499", SpendCategory.GROCERIES, "Convenience Stores"),

        // Fuel
        "5541" to MccInfo("5541", SpendCategory.FUEL, "Service Stations / Petrol Pumps"),
        "5542" to MccInfo("5542", SpendCategory.FUEL, "Automated Fuel Dispensers"),
        "5552" to MccInfo("5552", SpendCategory.FUEL, "EV Charging Stations"),

        // Travel
        "4111" to MccInfo("4111", SpendCategory.TRAVEL, "Commuter Passenger Transit"),
        "4112" to MccInfo("4112", SpendCategory.TRAVEL, "Passenger Railways (IRCTC)"),
        "4121" to MccInfo("4121", SpendCategory.TRAVEL, "Taxicabs & Ride Sharing (Uber, Ola)"),
        "4131" to MccInfo("4131", SpendCategory.TRAVEL, "Bus Lines"),
        "4511" to MccInfo("4511", SpendCategory.TRAVEL, "Airlines & Air Carriers"),
        "4722" to MccInfo("4722", SpendCategory.TRAVEL, "Travel Agencies"),
        "7011" to MccInfo("7011", SpendCategory.TRAVEL, "Hotels & Resorts"),

        // Utilities
        "4900" to MccInfo("4900", SpendCategory.UTILITIES, "Utilities - Electric, Gas, Water"),
        "4814" to MccInfo("4814", SpendCategory.UTILITIES, "Telecom Services & Recharge"),
        "4899" to MccInfo("4899", SpendCategory.UTILITIES, "Cable & Broadband Services"),

        // Shopping
        "5311" to MccInfo("5311", SpendCategory.SHOPPING, "Department Stores"),
        "5399" to MccInfo("5399", SpendCategory.SHOPPING, "General Merchandise"),
        "5611" to MccInfo("5611", SpendCategory.SHOPPING, "Men's Apparel"),
        "5621" to MccInfo("5621", SpendCategory.SHOPPING, "Women's Apparel"),
        "5651" to MccInfo("5651", SpendCategory.SHOPPING, "Family Clothing Stores"),
        "5732" to MccInfo("5732", SpendCategory.SHOPPING, "Electronics Sales"),
        "5942" to MccInfo("5942", SpendCategory.SHOPPING, "Book Stores"),
        "5999" to MccInfo("5999", SpendCategory.SHOPPING, "Miscellaneous Retail"),

        // Entertainment
        "7832" to MccInfo("7832", SpendCategory.ENTERTAINMENT, "Motion Picture Theaters (PVR)"),
        "7922" to MccInfo("7922", SpendCategory.ENTERTAINMENT, "Theatrical Tickets (BookMyShow)"),
        "7991" to MccInfo("7991", SpendCategory.ENTERTAINMENT, "Tourist Attractions"),
        "7996" to MccInfo("7996", SpendCategory.ENTERTAINMENT, "Amusement Parks")
    )

    fun mapMccToCategory(mcc: String?): Pair<SpendCategory, String?> {
        if (mcc.isNullOrBlank()) return Pair(SpendCategory.OTHER, null)
        val clean = mcc.trim()
        val found = directory[clean]
        if (found != null) {
            return Pair(found.category, found.name)
        }

        // Numeric range fallback
        val num = clean.toIntOrNull() ?: return Pair(SpendCategory.OTHER, null)
        return when {
            (num in 5811..5814) || num == 5462 -> Pair(SpendCategory.DINING, "Dining & Food")
            num in 5400..5499 -> Pair(SpendCategory.GROCERIES, "Groceries")
            num in 5541..5552 -> Pair(SpendCategory.FUEL, "Fuel")
            (num in 4000..4799) || (num in 7000..7099) -> Pair(SpendCategory.TRAVEL, "Travel & Transport")
            num in 4800..4999 -> Pair(SpendCategory.UTILITIES, "Utilities & Bills")
            (num in 5000..5399) || (num in 5600..5799) || (num in 5900..5999) -> Pair(SpendCategory.SHOPPING, "Shopping")
            num in 7800..7999 -> Pair(SpendCategory.ENTERTAINMENT, "Entertainment")
            else -> Pair(SpendCategory.OTHER, null)
        }
    }
}
