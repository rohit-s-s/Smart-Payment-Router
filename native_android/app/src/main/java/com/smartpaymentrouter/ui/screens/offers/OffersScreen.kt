package com.smartpaymentrouter.ui.screens.offers

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.smartpaymentrouter.data.model.Offer
import com.smartpaymentrouter.data.model.PaymentMethod
import com.smartpaymentrouter.ui.components.DeleteConfirmDialog
import com.smartpaymentrouter.ui.components.EditOfferDialog
import com.smartpaymentrouter.ui.theme.*

@Composable
fun OffersScreen(
    offers: List<Offer>,
    methods: List<PaymentMethod>,
    onToggleOffer: (String) -> Unit,
    onAddOffer: (Offer) -> Unit,
    onEditOffer: (Offer) -> Unit,
    onDeleteOffer: (String) -> Unit,
    onMarkOfferUsed: (String) -> Unit
) {
    var showAddDialog by remember { mutableStateOf(false) }
    var editingOffer by remember { mutableStateOf<Offer?>(null) }
    var deletingOffer by remember { mutableStateOf<Offer?>(null) }

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(DarkBackground)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Screen Header
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Active Offers & Boosters",
                            style = MaterialTheme.typography.headlineMedium,
                            color = TextPrimary
                        )
                        Text(
                            text = "Track bank promotions, instant category discounts, and merchant vouchers (US-06).",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary
                        )
                    }

                    Button(
                        onClick = { showAddDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = BrandPrimary),
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Add", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // Offers List
            items(offers, key = { it.id }) { offer ->
                val card = methods.find { it.id == offer.methodId }
                val isCapExhausted = offer.usageCap > 0 && offer.usageCount >= offer.usageCap

                Surface(
                    color = DarkSurface,
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(
                        1.dp,
                        if (isCapExhausted) RedAlert.copy(alpha = 0.5f) else if (offer.isActive) DarkSurfaceVariant else Color.Transparent
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        // Title Row: Icon, Title, Card badge, Active Switch
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = if (isCapExhausted) DarkSurfaceVariant else Color(0xFF312E81),
                                    modifier = Modifier.size(38.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.Default.LocalOffer,
                                            contentDescription = null,
                                            tint = if (isCapExhausted) TextMuted else AmberGold,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                                Column {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Text(
                                            text = offer.title,
                                            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                                            color = if (isCapExhausted) TextMuted else TextPrimary
                                        )
                                        if (isCapExhausted) {
                                            Surface(
                                                color = RedAlert.copy(alpha = 0.2f),
                                                shape = RoundedCornerShape(4.dp)
                                            ) {
                                                Text(
                                                    text = "Exhausted",
                                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                                                    color = RedAlert,
                                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                                )
                                            }
                                        }
                                    }

                                    Text(
                                        text = card?.name ?: "All Methods",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = TextSecondary
                                    )
                                }
                            }

                            Switch(
                                checked = offer.isActive && !isCapExhausted,
                                onCheckedChange = { onToggleOffer(offer.id) },
                                enabled = !isCapExhausted,
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = AmberGold,
                                    checkedTrackColor = BrandPrimary
                                )
                            )
                        }

                        if (offer.description.isNotBlank()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = offer.description,
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Benefit chips & rules
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Benefit amount
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = BrandPrimaryDark
                            ) {
                                val benefitText = when {
                                    offer.discountPercent != null -> "${offer.discountPercent}% Off" +
                                            if (offer.maxDiscount != null) " (up to ₹${offer.maxDiscount.toInt()})" else ""
                                    offer.discountFlat != null -> "Flat ₹${offer.discountFlat.toInt()} Off"
                                    else -> "Cashback Booster"
                                }
                                Text(
                                    text = benefitText,
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, fontWeight = FontWeight.Bold),
                                    color = Color.White,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                                )
                            }

                            if (offer.minAmount > 0) {
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = DarkSurfaceVariant
                                ) {
                                    Text(
                                        text = "Min ₹${offer.minAmount.toInt()}",
                                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                        color = TextPrimary,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                                    )
                                }
                            }

                            if (offer.stacks) {
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = Color(0xFF064E3B)
                                ) {
                                    Text(
                                        text = "Stacks",
                                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                        color = GreenReward,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                                    )
                                }
                            }

                            if (offer.requiresActivation) {
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = Color(0xFF451A03)
                                ) {
                                    Text(
                                        text = "Needs Activation",
                                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                        color = AmberGold,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                                    )
                                }
                            }
                        }

                        // Usage Cap Progress (US-06)
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (offer.usageCap > 0) {
                                    "Used ${offer.usageCount} of ${offer.usageCap} times"
                                } else {
                                    "Used ${offer.usageCount} times (Unlimited)"
                                },
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                color = if (isCapExhausted) RedAlert else TextSecondary
                            )

                            if (offer.endDate.isNotBlank()) {
                                Text(
                                    text = "Expires: ${offer.endDate}",
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                    color = TextMuted
                                )
                            }
                        }

                        HorizontalDivider(
                            color = DarkSurfaceVariant,
                            thickness = 1.dp,
                            modifier = Modifier.padding(vertical = 10.dp)
                        )

                        // Bottom Actions: Mark Used (US-06), Edit, Delete
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // "Mark as Used" Button
                            FilledTonalButton(
                                onClick = { onMarkOfferUsed(offer.id) },
                                enabled = !isCapExhausted,
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                                colors = ButtonDefaults.filledTonalButtonColors(
                                    containerColor = BrandPrimary.copy(alpha = 0.2f),
                                    contentColor = BrandAccent,
                                    disabledContainerColor = DarkSurfaceVariant,
                                    disabledContentColor = TextMuted
                                )
                            ) {
                                Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Mark as Used", fontSize = 11.sp)
                            }

                            // Edit & Delete
                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                IconButton(
                                    onClick = { editingOffer = offer },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(
                                        Icons.Default.Edit,
                                        contentDescription = "Edit Offer",
                                        tint = TextSecondary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }

                                IconButton(
                                    onClick = { deletingOffer = offer },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(
                                        Icons.Default.Delete,
                                        contentDescription = "Delete Offer",
                                        tint = RedAlert,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(48.dp))
            }
        }
    }

    // Add Offer Dialog (US-06)
    if (showAddDialog) {
        EditOfferDialog(
            initialOffer = null,
            availableMethods = methods,
            onSave = { newOffer ->
                onAddOffer(newOffer)
                showAddDialog = false
            },
            onDismiss = { showAddDialog = false }
        )
    }

    // Edit Offer Dialog (US-06)
    if (editingOffer != null) {
        EditOfferDialog(
            initialOffer = editingOffer,
            availableMethods = methods,
            onSave = { updated ->
                onEditOffer(updated)
                editingOffer = null
            },
            onDismiss = { editingOffer = null }
        )
    }

    // Delete Confirmation Dialog (US-06)
    if (deletingOffer != null) {
        DeleteConfirmDialog(
            title = "Delete Offer",
            message = "Are you sure you want to remove '${deletingOffer!!.title}'? It will no longer be considered for reward calculations.",
            onConfirmDelete = {
                onDeleteOffer(deletingOffer!!.id)
                deletingOffer = null
            },
            onDismiss = { deletingOffer = null }
        )
    }
}
