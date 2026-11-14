package com.smartpaymentrouter.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.LocalOffer
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.smartpaymentrouter.data.model.Offer
import com.smartpaymentrouter.data.model.PaymentMethod
import com.smartpaymentrouter.data.model.SpendCategory
import com.smartpaymentrouter.ui.theme.*
import java.util.UUID

@Composable
fun EditOfferDialog(
    initialOffer: Offer? = null,
    availableMethods: List<PaymentMethod>,
    onSave: (Offer) -> Unit,
    onDismiss: () -> Unit
) {
    val isEditing = initialOffer != null

    var title by remember { mutableStateOf(initialOffer?.title ?: "") }
    var description by remember { mutableStateOf(initialOffer?.description ?: "") }
    var selectedMethodId by remember { mutableStateOf(initialOffer?.methodId ?: (availableMethods.firstOrNull()?.id ?: "")) }
    var isPercentageDiscount by remember { mutableStateOf(initialOffer?.discountPercent != null || initialOffer?.discountFlat == null) }
    var discountPercentStr by remember { mutableStateOf(initialOffer?.discountPercent?.toString() ?: "10.0") }
    var discountFlatStr by remember { mutableStateOf(initialOffer?.discountFlat?.let { if (it > 0) it.toInt().toString() else "" } ?: "100") }
    var maxDiscountStr by remember { mutableStateOf(initialOffer?.maxDiscount?.let { if (it > 0) it.toInt().toString() else "" } ?: "") }
    var minAmountStr by remember { mutableStateOf(initialOffer?.minAmount?.let { if (it > 0) it.toInt().toString() else "" } ?: "0") }
    var usageCapStr by remember { mutableStateOf(initialOffer?.usageCap?.toString() ?: "5") }
    var endDateStr by remember { mutableStateOf(initialOffer?.endDate ?: "2026-12-31") }
    var stacks by remember { mutableStateOf(initialOffer?.stacks ?: true) }
    var requiresActivation by remember { mutableStateOf(initialOffer?.requiresActivation ?: false) }
    var selectedCategories by remember { mutableStateOf(initialOffer?.categories?.toSet() ?: emptySet()) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.9f),
            shape = RoundedCornerShape(24.dp),
            color = DarkSurface,
            border = BorderStroke(1.dp, DarkSurfaceVariant)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.LocalOffer, contentDescription = null, tint = AmberGold)
                        Column {
                            Text(
                                text = if (isEditing) "Edit Offer / Discount" else "Add New Offer",
                                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                                color = TextPrimary
                            )
                            Text(
                                text = "Define benefit, caps & stacking rules (US-06)",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary
                            )
                        }
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = TextMuted)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Scrollable Form
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Title
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("Offer Title * (e.g. Swiggy 15% Off)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = BrandPrimary,
                            unfocusedBorderColor = DarkSurfaceVariant
                        )
                    )

                    // Description
                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text("Description / Coupon Code (Optional)") },
                        placeholder = { Text("Use promo code HDFC200 at checkout") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = BrandPrimary,
                            unfocusedBorderColor = DarkSurfaceVariant
                        )
                    )

                    // Associated Payment Method Dropdown
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = "Eligible Payment Method *",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = TextPrimary
                        )

                        var expandedMethodDropdown by remember { mutableStateOf(false) }
                        val selectedMethodName = availableMethods.find { it.id == selectedMethodId }?.name ?: "Select Card"

                        Box(modifier = Modifier.fillMaxWidth()) {
                            OutlinedButton(
                                onClick = { expandedMethodDropdown = true },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = TextPrimary),
                                border = BorderStroke(1.dp, DarkSurfaceVariant)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(selectedMethodName, maxLines = 1)
                                    Text("▼", fontSize = 10.sp, color = TextMuted)
                                }
                            }
                            DropdownMenu(
                                expanded = expandedMethodDropdown,
                                onDismissRequest = { expandedMethodDropdown = false }
                            ) {
                                availableMethods.forEach { method ->
                                    DropdownMenuItem(
                                        text = { Text("${method.name} (${method.issuer})") },
                                        onClick = {
                                            selectedMethodId = method.id
                                            expandedMethodDropdown = false
                                        }
                                    )
                                }
                            }
                        }
                    }

                    // Benefit Type (Percentage vs Flat)
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = "Benefit Type",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = TextPrimary
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            FilterChip(
                                selected = isPercentageDiscount,
                                onClick = { isPercentageDiscount = true },
                                label = { Text("Percentage Discount (%)") }
                            )
                            FilterChip(
                                selected = !isPercentageDiscount,
                                onClick = { isPercentageDiscount = false },
                                label = { Text("Flat Discount (₹)") }
                            )
                        }
                    }

                    // Discount Amount & Max Cap
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        if (isPercentageDiscount) {
                            OutlinedTextField(
                                value = discountPercentStr,
                                onValueChange = { discountPercentStr = it },
                                label = { Text("Discount % *") },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                modifier = Modifier.weight(1f),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = BrandPrimary,
                                    unfocusedBorderColor = DarkSurfaceVariant
                                )
                            )
                            OutlinedTextField(
                                value = maxDiscountStr,
                                onValueChange = { maxDiscountStr = it },
                                label = { Text("Max Cap (₹)") },
                                placeholder = { Text("e.g. 150") },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.weight(1f),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = BrandPrimary,
                                    unfocusedBorderColor = DarkSurfaceVariant
                                )
                            )
                        } else {
                            OutlinedTextField(
                                value = discountFlatStr,
                                onValueChange = { discountFlatStr = it },
                                label = { Text("Flat Discount (₹) *") },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.weight(1f),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = BrandPrimary,
                                    unfocusedBorderColor = DarkSurfaceVariant
                                )
                            )
                        }
                    }

                    // Minimum Spend & Usage Cap Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedTextField(
                            value = minAmountStr,
                            onValueChange = { minAmountStr = it },
                            label = { Text("Min Spend (₹)") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = BrandPrimary,
                                unfocusedBorderColor = DarkSurfaceVariant
                            )
                        )
                        OutlinedTextField(
                            value = usageCapStr,
                            onValueChange = { usageCapStr = it },
                            label = { Text("Usage Cap (Times)") },
                            placeholder = { Text("0 = Unlimited") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = BrandPrimary,
                                unfocusedBorderColor = DarkSurfaceVariant
                            )
                        )
                    }

                    // Validity End Date
                    OutlinedTextField(
                        value = endDateStr,
                        onValueChange = { endDateStr = it },
                        label = { Text("Validity End Date (YYYY-MM-DD)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = BrandPrimary,
                            unfocusedBorderColor = DarkSurfaceVariant
                        )
                    )

                    // Stacking & Activation Switches
                    Surface(
                        color = DarkSurfaceVariant,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Stacks with Card Rewards",
                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                        color = TextPrimary
                                    )
                                    Text(
                                        text = "If enabled, offer benefit is added on top of base card rewards.",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = TextSecondary
                                    )
                                }
                                Switch(
                                    checked = stacks,
                                    onCheckedChange = { stacks = it },
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = AmberGold,
                                        checkedTrackColor = BrandPrimary
                                    )
                                )
                            }

                            HorizontalDivider(color = DarkSurface, thickness = 1.dp)

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Requires App/Portal Activation",
                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                        color = TextPrimary
                                    )
                                    Text(
                                        text = "Prompts you to confirm activation before redirecting (FR-8).",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = TextSecondary
                                    )
                                }
                                Switch(
                                    checked = requiresActivation,
                                    onCheckedChange = { requiresActivation = it },
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = AmberGold,
                                        checkedTrackColor = BrandPrimary
                                    )
                                )
                            }
                        }
                    }

                    // Applicable Categories Filter Chips
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = "Eligible Categories (Leave unselected for All Categories)",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = TextPrimary
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            SpendCategory.entries.take(4).forEach { cat ->
                                val isSelected = selectedCategories.contains(cat)
                                FilterChip(
                                    selected = isSelected,
                                    onClick = {
                                        selectedCategories = if (isSelected) selectedCategories - cat else selectedCategories + cat
                                    },
                                    label = { Text(cat.displayName, fontSize = 11.sp) }
                                )
                            }
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            SpendCategory.entries.drop(4).forEach { cat ->
                                val isSelected = selectedCategories.contains(cat)
                                FilterChip(
                                    selected = isSelected,
                                    onClick = {
                                        selectedCategories = if (isSelected) selectedCategories - cat else selectedCategories + cat
                                    },
                                    label = { Text(cat.displayName, fontSize = 11.sp) }
                                )
                            }
                        }
                    }

                    if (errorMessage != null) {
                        Text(
                            text = errorMessage!!,
                            color = RedAlert,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = TextSecondary),
                        border = BorderStroke(1.dp, DarkSurfaceVariant)
                    ) {
                        Text("Cancel")
                    }

                    Button(
                        onClick = {
                            if (title.isBlank()) {
                                errorMessage = "Please enter an offer title."
                                return@Button
                            }
                            if (selectedMethodId.isBlank()) {
                                errorMessage = "Please select an eligible payment method."
                                return@Button
                            }

                            val discountPct = if (isPercentageDiscount) discountPercentStr.toDoubleOrNull() else null
                            val discountFlat = if (!isPercentageDiscount) discountFlatStr.toDoubleOrNull() else null

                            if (isPercentageDiscount && (discountPct == null || discountPct <= 0)) {
                                errorMessage = "Please enter a valid discount percentage."
                                return@Button
                            }
                            if (!isPercentageDiscount && (discountFlat == null || discountFlat <= 0)) {
                                errorMessage = "Please enter a valid flat discount."
                                return@Button
                            }

                            val maxDiscount = maxDiscountStr.toDoubleOrNull()
                            val minAmount = minAmountStr.toDoubleOrNull() ?: 0.0
                            val usageCap = usageCapStr.toIntOrNull() ?: 5

                            val id = initialOffer?.id ?: "offer-${UUID.randomUUID().toString().take(8)}"

                            val offer = Offer(
                                id = id,
                                methodId = selectedMethodId,
                                title = title.trim(),
                                description = description.trim(),
                                categories = selectedCategories.toList(),
                                minAmount = minAmount,
                                discountPercent = discountPct,
                                discountFlat = discountFlat,
                                maxDiscount = maxDiscount,
                                startDate = initialOffer?.startDate ?: "",
                                endDate = endDateStr.trim(),
                                usageCap = usageCap,
                                usageCount = initialOffer?.usageCount ?: 0,
                                stacks = stacks,
                                requiresActivation = requiresActivation,
                                isActive = initialOffer?.isActive ?: true
                            )

                            onSave(offer)
                        },
                        modifier = Modifier.weight(1.5f),
                        colors = ButtonDefaults.buttonColors(containerColor = BrandPrimary)
                    ) {
                        Text(if (isEditing) "Save Changes" else "Add Offer", color = Color.White)
                    }
                }
            }
        }
    }
}
