package com.smartpaymentrouter

import android.app.Application
import com.smartpaymentrouter.data.local.PaymentRepository

class SmartPaymentRouterApplication : Application() {

    lateinit var repository: PaymentRepository
        private set

    override fun onCreate() {
        super.onCreate()
        repository = PaymentRepository(this)
    }
}
