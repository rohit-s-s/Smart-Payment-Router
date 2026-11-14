package com.smartpaymentrouter

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.smartpaymentrouter.data.model.*
import com.smartpaymentrouter.domain.engine.RecommendationEngine
import com.smartpaymentrouter.domain.parser.UpiParser
import com.smartpaymentrouter.ui.navigation.Screen
import com.smartpaymentrouter.ui.screens.cards.CardsScreen
import com.smartpaymentrouter.ui.screens.history.HistoryScreen
import com.smartpaymentrouter.ui.screens.manual.ManualEntryScreen
import com.smartpaymentrouter.ui.screens.offers.OffersScreen
import com.smartpaymentrouter.ui.screens.recommendation.RecommendationScreen
import com.smartpaymentrouter.ui.screens.scan.ScanQrScreen
import com.smartpaymentrouter.ui.theme.DarkBackground
import com.smartpaymentrouter.ui.theme.DarkSurface
import com.smartpaymentrouter.ui.theme.SmartPaymentRouterTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val app = application as SmartPaymentRouterApplication
        val repository = app.repository

        setContent {
            SmartPaymentRouterTheme {
                val methods by repository.methods.collectAsState()
                val offers by repository.offers.collectAsState()
                val savedMerchants by repository.savedMerchants.collectAsState()
                val paymentLogs by repository.paymentLogs.collectAsState()

                var currentScreen by remember { mutableStateOf<Screen>(Screen.Scan) }

                var paymentDraft by remember {
                    mutableStateOf(
                        PaymentDraft(
                            source = "scan",
                            category = SpendCategory.DINING,
                            categorySource = CategorySource.DEFAULT
                        )
                    )
                }

                var activeRecommendations by remember {
                    mutableStateOf<List<Recommendation>?>(null)
                }

                // Handle QR Decoded event (US-01)
                val handleQrScanned = { rawQr: String ->
                    val parsed = UpiParser.parse(rawQr, savedMerchants)

                    paymentDraft = PaymentDraft(
                        source = if (parsed.isUpi) "scan" else "manual",
                        payeeName = parsed.payeeName ?: "",
                        payeeAddress = parsed.payeeAddress ?: "",
                        amount = parsed.amount,
                        category = parsed.suggestedCategory,
                        categorySource = parsed.categorySource,
                        mcc = parsed.mcc,
                        rawQr = parsed.rawString,
                        isNonUpiUrl = parsed.isNonUpiUrl
                    )

                    if (parsed.isUpi && parsed.amount != null && parsed.amount > 0) {
                        // Fixed amount is present! Automatically calculate recommendation
                        val recs = RecommendationEngine.calculate(
                            amount = parsed.amount,
                            category = parsed.suggestedCategory,
                            methods = methods,
                            offers = offers
                        )
                        activeRecommendations = recs
                        currentScreen = Screen.Manual // shows recommendation view
                        Toast.makeText(
                            this@MainActivity,
                            "Scanned ${parsed.payeeName ?: "Payee"} • ₹${parsed.amount}",
                            Toast.LENGTH_SHORT
                        ).show()
                    } else if (parsed.isUpi) {
                        // US-01 bullet 2: QR has no amount, app asks user to enter one
                        activeRecommendations = null
                        currentScreen = Screen.Manual
                        Toast.makeText(
                            this@MainActivity,
                            "Payee captured from QR. Please enter amount.",
                            Toast.LENGTH_SHORT
                        ).show()
                    } else {
                        // US-01 bullet 3: Non-UPI string, switches to manual entry
                        activeRecommendations = null
                        currentScreen = Screen.Manual
                        Toast.makeText(
                            this@MainActivity,
                            "Non-UPI code. Switched to manual mode.",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    containerColor = DarkBackground,
                    bottomBar = {
                        NavigationBar(
                            containerColor = DarkSurface
                        ) {
                            Screen.bottomNavItems.forEach { screen ->
                                NavigationBarItem(
                                    icon = { Icon(screen.icon, contentDescription = screen.title) },
                                    label = { Text(screen.title) },
                                    selected = currentScreen == screen,
                                    onClick = {
                                        currentScreen = screen
                                        if (screen == Screen.Manual && paymentDraft.amount == null) {
                                            activeRecommendations = null
                                        }
                                    }
                                )
                            }
                        }
                    }
                ) { innerPadding ->
                    Surface(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding),
                        color = DarkBackground
                    ) {
                        when (currentScreen) {
                            Screen.Scan -> {
                                ScanQrScreen(
                                    onQrDecoded = handleQrScanned
                                )
                            }
                            Screen.Manual -> {
                                if (activeRecommendations != null && paymentDraft.amount != null) {
                                    RecommendationScreen(
                                        amount = paymentDraft.amount!!,
                                        category = paymentDraft.category,
                                        payeeName = paymentDraft.payeeName,
                                        payeeAddress = paymentDraft.payeeAddress,
                                        mcc = paymentDraft.mcc,
                                        source = paymentDraft.source,
                                        recommendations = activeRecommendations!!,
                                        onBack = { activeRecommendations = null },
                                        onPaymentConfirmed = { log ->
                                            repository.addPaymentLog(log)
                                            activeRecommendations = null
                                            paymentDraft = PaymentDraft()
                                            currentScreen = Screen.History
                                        }
                                    )
                                } else {
                                    ManualEntryScreen(
                                        draft = paymentDraft,
                                        savedMerchants = savedMerchants,
                                        onDraftChange = { updated -> paymentDraft = updated },
                                        onSubmitCalculate = {
                                            paymentDraft.amount?.let { amt ->
                                                val recs = RecommendationEngine.calculate(
                                                    amount = amt,
                                                    category = paymentDraft.category,
                                                    methods = methods,
                                                    offers = offers
                                                )
                                                activeRecommendations = recs
                                            }
                                        }
                                    )
                                }
                            }
                            Screen.Cards -> {
                                CardsScreen(
                                    methods = methods,
                                    onToggleMethod = { methodId ->
                                        val updated = methods.map {
                                            if (it.id == methodId) it.copy(isActive = !it.isActive) else it
                                        }
                                        repository.updateMethods(updated)
                                    }
                                )
                            }
                            Screen.Offers -> {
                                OffersScreen(
                                    offers = offers,
                                    methods = methods,
                                    onToggleOffer = { offerId ->
                                        val updated = offers.map {
                                            if (it.id == offerId) it.copy(isActive = !it.isActive) else it
                                        }
                                        repository.updateOffers(updated)
                                    }
                                )
                            }
                            Screen.History -> {
                                HistoryScreen(
                                    logs = paymentLogs,
                                    onClearLogs = { repository.clearLogs() }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
