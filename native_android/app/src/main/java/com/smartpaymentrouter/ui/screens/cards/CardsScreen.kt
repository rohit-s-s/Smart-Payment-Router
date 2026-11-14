package com.smartpaymentrouter.ui.screens.cards

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
import com.smartpaymentrouter.data.model.PaymentMethod
import com.smartpaymentrouter.data.model.PaymentMethodType
import com.smartpaymentrouter.ui.components.DeleteConfirmDialog
import com.smartpaymentrouter.ui.components.EditCardDialog
import com.smartpaymentrouter.ui.theme.*
import kotlin.math.min

@Composable
fun CardsScreen(
    methods: List<PaymentMethod>,
    onToggleMethod: (String) -> Unit,
    onAddMethod: (PaymentMethod) -> Unit,
    onEditMethod: (PaymentMethod) -> Unit,
    onDeleteMethod: (String) -> Unit,
    onConfirmRules: (String) -> Unit
) {
    var showAddDialog by remember { mutableStateOf(false) }
    var editingMethod by remember { mutableStateOf<PaymentMethod?>(null) }
    var deletingMethod by remember { mutableStateOf<PaymentMethod?>(null) }

    val outdatedMethods = methods.filter { it.isOutdated(90) }

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(DarkBackground)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Header
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Cards & UPI Inventory",
                            style = MaterialTheme.typography.headlineMedium,
                            color = TextPrimary
                        )
                        Text(
                            text = "Manage your credit cards, monthly reward caps, and accelerated bonus rules.",
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

            // 90-Day Review Warning Banner (US-07)
            if (outdatedMethods.isNotEmpty()) {
                item {
                    Surface(
                        color = Color(0xFF451A03),
                        shape = RoundedCornerShape(14.dp),
                        border = BorderStroke(1.dp, AmberGold.copy(alpha = 0.5f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(Icons.Default.Schedule, contentDescription = null, tint = AmberGold)
                                Text(
                                    text = "Rules Review Required (US-07)",
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                    color = AmberGold
                                )
                            }
                            Text(
                                text = "${outdatedMethods.size} payment method(s) have reward rules older than 90 days. Bank terms change frequently—verify your terms below.",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextPrimary
                            )
                        }
                    }
                }
            }

            // Privacy Reassurance Banner (US-04)
            item {
                Surface(
                    color = DarkSurface,
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, DarkSurfaceVariant),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = null,
                            tint = GreenReward,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "Privacy Protected: Only issuer, names, and reward rules are kept. No card numbers or CVVs.",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                            color = TextSecondary
                        )
                    }
                }
            }

            // Method Items List
            items(methods, key = { it.id }) { method ->
                val capPercent = if (method.monthlyCap != null && method.monthlyCap > 0) {
                    min(1.0f, (method.currentMonthRewardUsed / method.monthlyCap).toFloat())
                } else 0f

                val isOutdated = method.isOutdated(90)
                val daysAgo = method.daysSinceConfirmed()

                Surface(
                    color = DarkSurface,
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(
                        1.dp,
                        if (isOutdated) AmberGold.copy(alpha = 0.6f) else if (method.isActive) DarkSurfaceVariant else Color.Transparent
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        // Title Row with Icon, Name, Details, Active Switch
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
                                    color = if (method.type == PaymentMethodType.UPI) Color(0xFF1E3A8A) else BrandPrimaryDark,
                                    modifier = Modifier.size(40.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = if (method.type == PaymentMethodType.UPI) Icons.Default.Smartphone else Icons.Default.CreditCard,
                                            contentDescription = null,
                                            tint = Color.White
                                        )
                                    }
                                }
                                Column {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Text(
                                            text = method.name,
                                            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                                            color = TextPrimary
                                        )
                                        Surface(
                                            color = DarkSurfaceVariant,
                                            shape = RoundedCornerShape(4.dp)
                                        ) {
                                            Text(
                                                text = method.type.displayName,
                                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                                                color = TextSecondary,
                                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                            )
                                        }
                                    }

                                    Text(
                                        text = "${method.issuer} • ${method.network}" +
                                                (method.cardLast4?.let { " (•• $it)" } ?: "") +
                                                (method.upiVpa?.let { " ($it)" } ?: ""),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = TextSecondary
                                    )
                                }
                            }

                            Switch(
                                checked = method.isActive,
                                onCheckedChange = { onToggleMethod(method.id) },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = AmberGold,
                                    checkedTrackColor = BrandPrimary
                                )
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Rates & Bonuses
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Base Rate: ${method.baseRate}%",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary
                            )
                            val bonuses = method.rules.filter { it.category != null }
                            Text(
                                text = if (bonuses.isNotEmpty()) "${bonuses.size} bonus categories" else "Flat rate only",
                                style = MaterialTheme.typography.bodySmall,
                                color = if (bonuses.isNotEmpty()) BrandPrimary else TextMuted
                            )
                        }

                        // Exclusions (US-04)
                        if (method.exclusions.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    text = "Excluded (0%):",
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                    color = RedAlert
                                )
                                method.exclusions.forEach { excl ->
                                    Surface(
                                        color = RedAlert.copy(alpha = 0.15f),
                                        shape = RoundedCornerShape(4.dp)
                                    ) {
                                        Text(
                                            text = excl.displayName,
                                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                                            color = RedAlert,
                                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Monthly Cap Progress
                        if (method.monthlyCap != null) {
                            Column {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "Monthly Cap Used",
                                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                        color = TextMuted
                                    )
                                    Text(
                                        text = "₹${method.currentMonthRewardUsed.toInt()} / ₹${method.monthlyCap.toInt()}",
                                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                        color = if (capPercent >= 0.9f) RedAlert else TextSecondary
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                LinearProgressIndicator(
                                    progress = { capPercent },
                                    modifier = Modifier.fillMaxWidth().height(6.dp),
                                    color = if (capPercent >= 0.9f) RedAlert else BrandPrimary,
                                    trackColor = DarkSurfaceVariant
                                )
                            }
                        } else {
                            Text(
                                text = "Reward Cap: Unlimited",
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                                color = GreenReward
                            )
                        }

                        HorizontalDivider(
                            color = DarkSurfaceVariant,
                            thickness = 1.dp,
                            modifier = Modifier.padding(vertical = 10.dp)
                        )

                        // Bottom Actions: Rule Confirmation Status (US-07) + Edit + Delete (US-05)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Rule last confirmed status (US-07)
                            Column {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        imageVector = if (isOutdated) Icons.Default.Warning else Icons.Default.CheckCircle,
                                        contentDescription = null,
                                        tint = if (isOutdated) AmberGold else GreenReward,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Text(
                                        text = if (isOutdated) {
                                            "Review Needed (>90d)"
                                        } else if (daysAgo >= 0) {
                                            "Verified ${daysAgo}d ago"
                                        } else {
                                            "Rules current"
                                        },
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontSize = 10.sp,
                                            fontWeight = if (isOutdated) FontWeight.Bold else FontWeight.Normal
                                        ),
                                        color = if (isOutdated) AmberGold else TextSecondary
                                    )
                                }
                            }

                            // Action buttons: Confirm / Edit / Delete
                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                if (isOutdated) {
                                    FilledTonalButton(
                                        onClick = { onConfirmRules(method.id) },
                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                        colors = ButtonDefaults.filledTonalButtonColors(
                                            containerColor = AmberGold.copy(alpha = 0.2f),
                                            contentColor = AmberGold
                                        )
                                    ) {
                                        Text("Confirm Current", fontSize = 11.sp)
                                    }
                                }

                                IconButton(
                                    onClick = { editingMethod = method },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(
                                        Icons.Default.Edit,
                                        contentDescription = "Edit Card",
                                        tint = TextSecondary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }

                                IconButton(
                                    onClick = { deletingMethod = method },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(
                                        Icons.Default.Delete,
                                        contentDescription = "Delete Card",
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

    // Add Card Dialog (US-04)
    if (showAddDialog) {
        EditCardDialog(
            initialMethod = null,
            onSave = { newMethod ->
                onAddMethod(newMethod)
                showAddDialog = false
            },
            onDismiss = { showAddDialog = false }
        )
    }

    // Edit Card Dialog (US-05)
    if (editingMethod != null) {
        EditCardDialog(
            initialMethod = editingMethod,
            onSave = { updated ->
                onEditMethod(updated)
                editingMethod = null
            },
            onDismiss = { editingMethod = null }
        )
    }

    // Delete Confirmation Dialog (US-05)
    if (deletingMethod != null) {
        DeleteConfirmDialog(
            title = "Delete Payment Method",
            message = "Are you sure you want to remove '${deletingMethod!!.name}'? It will no longer be considered for reward recommendations.",
            onConfirmDelete = {
                onDeleteMethod(deletingMethod!!.id)
                deletingMethod = null
            },
            onDismiss = { deletingMethod = null }
        )
    }
}
