package com.smartpaymentrouter.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    object Scan : Screen("scan", "Scan QR", Icons.Default.QrCodeScanner)
    object Manual : Screen("manual", "Manual Pay", Icons.Default.Edit)
    object Cards : Screen("cards", "Cards", Icons.Default.CreditCard)
    object Offers : Screen("offers", "Offers", Icons.Default.LocalOffer)
    object History : Screen("history", "History", Icons.Default.History)

    companion object {
        val bottomNavItems = listOf(Scan, Manual, Cards, Offers, History)
    }
}
