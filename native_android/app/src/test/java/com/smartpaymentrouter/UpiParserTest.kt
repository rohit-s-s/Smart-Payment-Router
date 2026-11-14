package com.smartpaymentrouter

import com.smartpaymentrouter.data.model.CategorySource
import com.smartpaymentrouter.data.model.SavedMerchant
import com.smartpaymentrouter.data.model.SpendCategory
import com.smartpaymentrouter.domain.parser.UpiParser
import org.junit.Assert.*
import org.junit.Test

class UpiParserTest {

    private val sampleSavedMerchants = listOf(
        SavedMerchant(
            name = "Swiggy",
            normalizedName = "swiggy",
            vpa = "swiggy@icici",
            category = SpendCategory.DINING,
            mcc = "5812",
            lastUsedAt = "2026-11-10T10:00:00Z",
            usageCount = 10
        ),
        SavedMerchant(
            name = "HPCL Fuel Station",
            normalizedName = "hpcl fuel station",
            vpa = "hpcl@upi",
            category = SpendCategory.FUEL,
            mcc = "5541",
            lastUsedAt = "2026-11-09T10:00:00Z",
            usageCount = 5
        )
    )

    @Test
    fun testUpiParser_validUpiWithAmountAndMcc() {
        val qrString = "upi://pay?pa=restaurant@okaxis&pn=Green+Bistro&am=650.00&cu=INR&mc=5812&tn=Lunch+Table+4"
        val result = UpiParser.parse(qrString, sampleSavedMerchants)

        assertTrue(result.isUpi)
        assertEquals("restaurant@okaxis", result.payeeAddress)
        assertEquals("Green Bistro", result.payeeName)
        assertEquals(650.0, result.amount!!, 0.001)
        assertEquals("5812", result.mcc)
        assertEquals(SpendCategory.DINING, result.suggestedCategory)
        assertEquals(CategorySource.MCC, result.categorySource)
    }

    @Test
    fun testUpiParser_missingAmount_promptsUser() {
        val qrString = "upi://pay?pa=retailer@upi&pn=City+Shop&cu=INR&mc=5411"
        val result = UpiParser.parse(qrString, sampleSavedMerchants)

        assertTrue(result.isUpi)
        assertEquals("retailer@upi", result.payeeAddress)
        assertNull(result.amount) // signals app to ask user to enter amount (US-01 bullet 2)
        assertEquals(SpendCategory.GROCERIES, result.suggestedCategory)
        assertEquals(CategorySource.MCC, result.categorySource)
    }

    @Test
    fun testUpiParser_nonUpiWebsiteLink_switchesToManual() {
        val nonUpiUrl = "https://www.croma.com/checkout/order-9912"
        val result = UpiParser.parse(nonUpiUrl, sampleSavedMerchants)

        assertFalse(result.isUpi)
        assertTrue(result.isNonUpiUrl)
        assertNull(result.amount)
        assertEquals("croma.com", result.payeeName)
    }

    @Test
    fun testUpiParser_merchantHistoryMemoryPriority() {
        // QR without MCC, but matches Swiggy VPA in merchant memory (US-03)
        val qrWithoutMcc = "upi://pay?pa=swiggy@icici&pn=Swiggy+Delivery&am=450.00&cu=INR"
        val result = UpiParser.parse(qrWithoutMcc, sampleSavedMerchants)

        assertTrue(result.isUpi)
        assertEquals(SpendCategory.DINING, result.suggestedCategory)
        assertEquals(CategorySource.MERCHANT_HISTORY, result.categorySource)
    }

    @Test
    fun testMccMapping_allFixedCategories() {
        // Fuel
        val fuelQR = UpiParser.parse("upi://pay?pa=petrol@paytm&am=1000&mc=5541")
        assertEquals(SpendCategory.FUEL, fuelQR.suggestedCategory)

        // Groceries
        val groceryQR = UpiParser.parse("upi://pay?pa=mart@hdfc&am=500&mc=5411")
        assertEquals(SpendCategory.GROCERIES, groceryQR.suggestedCategory)

        // Utilities
        val utilityQR = UpiParser.parse("upi://pay?pa=power@axis&am=2000&mc=4900")
        assertEquals(SpendCategory.UTILITIES, utilityQR.suggestedCategory)

        // Entertainment
        val movieQR = UpiParser.parse("upi://pay?pa=cinemas@icici&am=800&mc=7832")
        assertEquals(SpendCategory.ENTERTAINMENT, movieQR.suggestedCategory)
    }
}
