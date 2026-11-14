package com.smartpaymentrouter.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    data object Scan : Screen("scan", "Scan QR", Icons.Default.QrCodeScanner)
    data object Manual : Screen("manual", "Manual Pay", Icons.Default.Edit)
    data object Cards : Screen("cards", "Cards", Icons.Default.CreditCard)
    data object Offers : Screen("offers", "Offers", Icons.Default.LocalOffer)
    data object History : Screen("history", "History", Icons.Default.History)

    companion object {
        val bottomNavItems: List<Screen>
            get() = listOf(Scan, Manual, Cards, Offers, History)
    }
}
