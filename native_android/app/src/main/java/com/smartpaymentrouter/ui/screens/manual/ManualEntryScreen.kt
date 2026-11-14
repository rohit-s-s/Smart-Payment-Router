package com.smartpaymentrouter.ui.screens.manual

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.smartpaymentrouter.data.model.*
import com.smartpaymentrouter.ui.components.CategorySelectorCard
import com.smartpaymentrouter.ui.components.CategorySourceBadge
import com.smartpaymentrouter.ui.theme.*

@Composable
fun ManualEntryScreen(
    draft: PaymentDraft,
    savedMerchants: List<SavedMerchant>,
    onDraftChange: (PaymentDraft) -> Unit,
    onSubmitCalculate: () -> Unit
) {
    var amountInput by remember(draft.amount) {
        mutableStateOf(draft.amount?.toString() ?: "")
    }

    var merchantNameInput by remember(draft.payeeName) {
        mutableStateOf(draft.payeeName)
    }

    val isValid = (draft.amount != null && draft.amount > 0.0)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Screen Header
        Text(
            text = if (draft.source == "scan") "Confirm Payment Details" else "Enter Payment Manually",
            style = MaterialTheme.typography.headlineMedium,
            color = TextPrimary
        )
        Text(
            text = if (draft.source == "scan")
                "Review scanned details and confirm or override the spend category."
            else
                "Enter amount and category for online orders, bills, and card payments.",
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondary
        )

        // Non-UPI Notice Banner (US-01 bullet 3)
        if (draft.isNonUpiUrl) {
            Surface(
                color = Color(0xFF451A03),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, AmberGold)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = AmberGold
                    )
                    Text(
                        text = "Scanned QR was a website link. Switched to manual mode. Please confirm amount and category.",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextPrimary
                    )
                }
            }
        }

        // Amount Field (US-01 & US-02: Required before recommendation is produced)
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Payment Amount *",
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                    color = TextPrimary
                )
                if (draft.source == "scan" && draft.amount != null && draft.amount > 0) {
                    Text(
                        text = "Pre-filled from QR",
                        style = MaterialTheme.typography.labelSmall,
                        color = BrandAccent
                    )
                }
            }

            OutlinedTextField(
                value = amountInput,
                onValueChange = { input ->
                    amountInput = input
                    val parsed = input.toDoubleOrNull()
                    onDraftChange(draft.copy(amount = parsed))
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .semantics { contentDescription = "Payment amount in Indian Rupees (required)" },
                leadingIcon = {
                    Text(
                        text = "₹",
                        style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Black),
                        color = TextSecondary,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                },
                placeholder = {
                    Text("0.00", style = MaterialTheme.typography.headlineMedium, color = TextMuted)
                },
                textStyle = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Black,
                    color = TextPrimary
                ),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = BrandPrimary,
                    unfocusedBorderColor = DarkSurfaceVariant,
                    focusedContainerColor = DarkSurface,
                    unfocusedContainerColor = DarkSurface
                )
            )

            // Quick Add Amount Buttons
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.padding(top = 4.dp)
            ) {
                listOf(100.0, 250.0, 500.0, 1000.0, 2000.0).forEach { delta ->
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = DarkSurfaceVariant,
                        modifier = Modifier.weight(1f)
                    ) {
                        TextButton(
                            onClick = {
                                val current = draft.amount ?: 0.0
                                val updated = current + delta
                                amountInput = updated.toInt().toString()
                                onDraftChange(draft.copy(amount = updated))
                            },
                            contentPadding = PaddingValues(horizontal = 2.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "+₹${delta.toInt()}",
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                color = TextPrimary
                            )
                        }
                    }
                }
            }
        }

        // Merchant Name Field (Optional, with history matching for US-03)
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(
                text = "Merchant / Store Name (Optional)",
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                color = TextPrimary
            )
            OutlinedTextField(
                value = merchantNameInput,
                onValueChange = { name ->
                    merchantNameInput = name

                    // Check if name matches saved merchant (US-03)
                    val normalized = name.lowercase().replace(Regex("[^a-z0-9]"), "").trim()
                    val match = savedMerchants.find { it.normalizedName.contains(normalized) && normalized.length > 2 }

                    if (match != null && draft.categorySource != CategorySource.MANUAL) {
                        onDraftChange(
                            draft.copy(
                                payeeName = name,
                                category = match.category,
                                categorySource = CategorySource.MERCHANT_HISTORY
                            )
                        )
                    } else {
                        onDraftChange(draft.copy(payeeName = name))
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("e.g. Swiggy, HPCL, Blinkit, Amazon...", color = TextMuted) },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = BrandPrimary,
                    unfocusedBorderColor = DarkSurfaceVariant,
                    focusedContainerColor = DarkSurface,
                    unfocusedContainerColor = DarkSurface
                )
            )
        }

        // Spend Category Selection (US-03: Fixed 8 Categories & Override)
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Spend Category *",
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                    color = TextPrimary
                )
                CategorySourceBadge(
                    category = draft.category,
                    source = draft.categorySource,
                    mcc = draft.mcc
                )
            }

            Text(
                text = "Tap any category below to confirm or override:",
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary
            )

            // 8 Fixed Categories Grid
            val categories = SpendCategory.entries
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                for (row in categories.toList().chunked(2)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        for (cat in row) {
                            CategorySelectorCard(
                                category = cat,
                                isSelected = draft.category == cat,
                                onSelect = {
                                    onDraftChange(
                                        draft.copy(
                                            category = cat,
                                            categorySource = CategorySource.MANUAL // override
                                        )
                                    )
                                },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Calculate Recommendation Button
        Button(
            onClick = onSubmitCalculate,
            enabled = isValid,
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = BrandPrimary,
                disabledContainerColor = DarkSurfaceVariant
            )
        ) {
            Icon(
                imageVector = Icons.Default.ElectricBolt,
                contentDescription = null,
                tint = if (isValid) AmberGold else TextMuted
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Find Highest Reward Card",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                ),
                color = if (isValid) Color.White else TextMuted
            )
        }

        if (!isValid) {
            Text(
                text = "Please enter an amount greater than ₹0 to receive recommendations.",
                style = MaterialTheme.typography.bodySmall,
                color = TextMuted,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
        }
    }
}
