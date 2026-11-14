package com.smartpaymentrouter.data.local

import com.smartpaymentrouter.data.model.*

object DefaultDataProvider {

    fun getDefaultPaymentMethods(): List<PaymentMethod> = listOf(
        PaymentMethod(
            id = "hdfc-millennia",
            name = "HDFC Millennia Credit Card",
            type = PaymentMethodType.CREDIT_CARD,
            issuer = "HDFC Bank",
            network = "Mastercard",
            baseRate = 1.0,
            rules = listOf(
                RewardRule(SpendCategory.DINING, 5.0, "monthly", 1000.0),
                RewardRule(SpendCategory.SHOPPING, 5.0, "monthly", 1000.0),
                RewardRule(SpendCategory.ENTERTAINMENT, 5.0, "monthly", 1000.0),
                RewardRule(null, 1.0)
            ),
            monthlyCap = 1000.0,
            currentMonthRewardUsed = 200.0,
            ruleLastConfirmed = "2026-10-15",
            cardLast4 = "4192"
        ),
        PaymentMethod(
            id = "sbi-cashback",
            name = "SBI Cashback Credit Card",
            type = PaymentMethodType.CREDIT_CARD,
            issuer = "SBI Card",
            network = "Visa",
            baseRate = 1.0,
            rules = listOf(
                RewardRule(SpendCategory.SHOPPING, 5.0, "monthly", 5000.0),
                RewardRule(SpendCategory.GROCERIES, 5.0, "monthly", 5000.0),
                RewardRule(SpendCategory.UTILITIES, 5.0, "monthly", 5000.0),
                RewardRule(SpendCategory.DINING, 5.0, "monthly", 5000.0),
                RewardRule(null, 1.0)
            ),
            exclusions = listOf(SpendCategory.FUEL),
            monthlyCap = 5000.0,
            currentMonthRewardUsed = 850.0,
            ruleLastConfirmed = "2026-11-01",
            cardLast4 = "8831"
        ),
        PaymentMethod(
            id = "icici-amazon-pay",
            name = "Amazon Pay ICICI Card",
            type = PaymentMethodType.CREDIT_CARD,
            issuer = "ICICI Bank",
            network = "Visa",
            baseRate = 1.0,
            rules = listOf(
                RewardRule(SpendCategory.SHOPPING, 5.0),
                RewardRule(SpendCategory.UTILITIES, 2.0),
                RewardRule(SpendCategory.DINING, 2.0),
                RewardRule(null, 1.0)
            ),
            monthlyCap = null, // Unlimited!
            currentMonthRewardUsed = 340.0,
            ruleLastConfirmed = "2026-09-10",
            cardLast4 = "1049"
        ),
        PaymentMethod(
            id = "axis-atlas",
            name = "Axis Atlas Credit Card",
            type = PaymentMethodType.CREDIT_CARD,
            issuer = "Axis Bank",
            network = "Visa",
            baseRate = 2.0,
            rules = listOf(
                RewardRule(SpendCategory.TRAVEL, 10.0, "monthly", 2500.0),
                RewardRule(SpendCategory.DINING, 4.0, "monthly", 2500.0),
                RewardRule(null, 2.0)
            ),
            monthlyCap = 2500.0,
            currentMonthRewardUsed = 400.0,
            ruleLastConfirmed = "2026-10-20",
            cardLast4 = "9920"
        ),
        PaymentMethod(
            id = "iocl-rupay",
            name = "IndianOil RuPay Card",
            type = PaymentMethodType.CREDIT_CARD,
            issuer = "Axis Bank / IOCL",
            network = "RuPay",
            baseRate = 1.0,
            rules = listOf(
                RewardRule(SpendCategory.FUEL, 4.0, "monthly", 250.0),
                RewardRule(null, 1.0)
            ),
            monthlyCap = 250.0,
            currentMonthRewardUsed = 75.0,
            ruleLastConfirmed = "2026-08-01", // >90 days old to test FR-11 warning
            cardLast4 = "6312"
        ),
        PaymentMethod(
            id = "gpay-upi",
            name = "Google Pay UPI (Savings)",
            type = PaymentMethodType.UPI,
            issuer = "NPCI / Bank",
            network = "UPI",
            baseRate = 0.5,
            rules = listOf(
                RewardRule(null, 0.5, "monthly", 100.0)
            ),
            monthlyCap = 100.0,
            currentMonthRewardUsed = 15.0,
            ruleLastConfirmed = "2026-11-05",
            upiVpa = "user@okhdfcbank"
        )
    )

    fun getDefaultOffers(): List<Offer> = listOf(
        Offer(
            id = "offer-swiggy-zomato",
            methodId = "hdfc-millennia",
            title = "Weekend Dining Bonanza: 10% Extra",
            description = "Extra 10% cashback up to ₹150 on dining orders over ₹400 with HDFC cards",
            categories = listOf(SpendCategory.DINING),
            minAmount = 400.0,
            discountPercent = 10.0,
            maxDiscount = 150.0,
            startDate = "2026-11-01",
            endDate = "2026-11-30",
            usageCap = 4,
            usageCount = 1,
            stacks = true,
            requiresActivation = true // FR-8 prompt
        ),
        Offer(
            id = "offer-fuel-surcharge",
            methodId = "iocl-rupay",
            title = "Fuel Cashback Boost: Flat ₹30 Back",
            description = "Flat ₹30 additional reward on fuel transactions of ₹500 or more",
            categories = listOf(SpendCategory.FUEL),
            minAmount = 500.0,
            discountFlat = 30.0,
            startDate = "2026-11-01",
            endDate = "2026-12-31",
            usageCap = 2,
            usageCount = 0,
            stacks = true,
            requiresActivation = false
        ),
        Offer(
            id = "offer-bill-pay-sbi",
            methodId = "sbi-cashback",
            title = "SBI Smart Utility Booster: Extra ₹50",
            description = "Flat ₹50 cashback on Utility bill payments above ₹1,000",
            categories = listOf(SpendCategory.UTILITIES),
            minAmount = 1000.0,
            discountFlat = 50.0,
            startDate = "2026-11-01",
            endDate = "2026-11-25",
            usageCap = 2,
            usageCount = 0,
            stacks = false, // non-stackable
            requiresActivation = true
        )
    )

    fun getDefaultSavedMerchants(): List<SavedMerchant> = listOf(
        SavedMerchant(
            name = "Swiggy",
            normalizedName = "swiggy",
            vpa = "swiggy@icici",
            category = SpendCategory.DINING,
            mcc = "5812",
            lastUsedAt = "2026-11-12T14:30:00Z",
            usageCount = 14
        ),
        SavedMerchant(
            name = "Blinkit Quick Groceries",
            normalizedName = "blinkit quick groceries",
            vpa = "blinkit@kotak",
            category = SpendCategory.GROCERIES,
            mcc = "5411",
            lastUsedAt = "2026-11-13T10:15:00Z",
            usageCount = 8
        ),
        SavedMerchant(
            name = "HPCL Auto Fuel Care",
            normalizedName = "hpcl auto fuel care",
            vpa = "hpclpump@upi",
            category = SpendCategory.FUEL,
            mcc = "5541",
            lastUsedAt = "2026-11-10T18:45:00Z",
            usageCount = 5
        ),
        SavedMerchant(
            name = "BESCOM Electricity Bill",
            normalizedName = "bescom electricity bill",
            vpa = "bescom@billdesk",
            category = SpendCategory.UTILITIES,
            mcc = "4900",
            lastUsedAt = "2026-11-05T12:00:00Z",
            usageCount = 6
        )
    )
}
