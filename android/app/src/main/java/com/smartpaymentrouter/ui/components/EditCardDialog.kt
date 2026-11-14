package com.smartpaymentrouter.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.smartpaymentrouter.data.model.*
import com.smartpaymentrouter.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

data class BonusRuleInput(
    val category: SpendCategory,
    val rate: String
)

@Composable
fun EditCardDialog(
    initialMethod: PaymentMethod? = null,
    onSave: (PaymentMethod) -> Unit,
    onDismiss: () -> Unit
) {
    val isEditing = initialMethod != null

    var name by remember { mutableStateOf(initialMethod?.name ?: "") }
    var selectedType by remember { mutableStateOf(initialMethod?.type ?: PaymentMethodType.CREDIT_CARD) }
    var issuer by remember { mutableStateOf(initialMethod?.issuer ?: "") }
    var network by remember { mutableStateOf(initialMethod?.network ?: "Visa") }
    var last4 by remember { mutableStateOf(initialMethod?.cardLast4 ?: "") }
    var upiVpa by remember { mutableStateOf(initialMethod?.upiVpa ?: "") }
    var baseRateStr by remember { mutableStateOf(initialMethod?.baseRate?.toString() ?: "1.0") }
    var monthlyCapStr by remember { mutableStateOf(initialMethod?.monthlyCap?.let { if (it > 0) it.toInt().toString() else "" } ?: "") }

    // Dynamic bonus rules: Category -> Rate%
    var bonusRules by remember {
        mutableStateOf<List<BonusRuleInput>>(
            initialMethod?.rules?.filter { it.category != null }?.map {
                BonusRuleInput(it.category!!, it.rate.toString())
            } ?: emptyList()
        )
    }

    // Excluded categories (US-04)
    var selectedExclusions by remember {
        mutableStateOf(initialMethod?.exclusions?.toSet() ?: emptySet())
    }

    var selectedNewCategory by remember { mutableStateOf(SpendCategory.DINING) }
    var newCategoryRateStr by remember { mutableStateOf("5.0") }
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
                    Column {
                        Text(
                            text = if (isEditing) "Edit Payment Method" else "Add Payment Method",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            color = TextPrimary
                        )
                        Text(
                            text = if (isEditing) "Update reward rules & limits" else "Enter card or UPI details (US-04)",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
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
                    // Privacy Guarantee Notice (US-04)
                    Surface(
                        color = Color(0xFF1E293B),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, BrandPrimary.copy(alpha = 0.4f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Security,
                                contentDescription = null,
                                tint = GreenReward,
                                modifier = Modifier.size(24.dp)
                            )
                            Text(
                                text = "Zero Credentials Stored: We never store or ask for full card numbers, CVVs, expiry dates, or UPI PINs.",
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                                color = TextPrimary
                            )
                        }
                    }

                    // Payment Method Type Selector
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = "Payment Method Type",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = TextPrimary
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            PaymentMethodType.entries.forEach { type ->
                                val isSelected = selectedType == type
                                FilterChip(
                                    selected = isSelected,
                                    onClick = {
                                        selectedType = type
                                        if (type == PaymentMethodType.UPI && network != "UPI") {
                                            network = "UPI"
                                        } else if (type != PaymentMethodType.UPI && network == "UPI") {
                                            network = "Visa"
                                        }
                                    },
                                    label = { Text(type.displayName, fontSize = 12.sp) },
                                    leadingIcon = {
                                        Icon(
                                            imageVector = if (type == PaymentMethodType.UPI) Icons.Default.Smartphone else Icons.Default.CreditCard,
                                            contentDescription = null,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = BrandPrimary,
                                        selectedLabelColor = Color.White
                                    )
                                )
                            }
                        }
                    }

                    // Display Name
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Display Name * (e.g. HDFC Millennia)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = BrandPrimary,
                            unfocusedBorderColor = DarkSurfaceVariant
                        )
                    )

                    // Issuer & Network Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedTextField(
                            value = issuer,
                            onValueChange = { issuer = it },
                            label = { Text("Issuer / Bank *") },
                            placeholder = { Text("e.g. HDFC, SBI") },
                            singleLine = true,
                            modifier = Modifier.weight(1f),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = BrandPrimary,
                                unfocusedBorderColor = DarkSurfaceVariant
                            )
                        )

                        OutlinedTextField(
                            value = network,
                            onValueChange = { network = it },
                            label = { Text("Network") },
                            placeholder = { Text("Visa, RuPay...") },
                            singleLine = true,
                            modifier = Modifier.weight(1f),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = BrandPrimary,
                                unfocusedBorderColor = DarkSurfaceVariant
                            )
                        )
                    }

                    // Last 4 / UPI VPA
                    if (selectedType == PaymentMethodType.UPI) {
                        OutlinedTextField(
                            value = upiVpa,
                            onValueChange = { upiVpa = it },
                            label = { Text("UPI VPA / ID (Optional)") },
                            placeholder = { Text("user@oksbi") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = BrandPrimary,
                                unfocusedBorderColor = DarkSurfaceVariant
                            )
                        )
                    } else {
                        OutlinedTextField(
                            value = last4,
                            onValueChange = { if (it.length <= 4 && it.all { char -> char.isDigit() }) last4 = it },
                            label = { Text("Card Last 4 Digits (Optional)") },
                            placeholder = { Text("e.g. 4192") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = BrandPrimary,
                                unfocusedBorderColor = DarkSurfaceVariant
                            )
                        )
                    }

                    // Base Rate & Monthly Cap Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedTextField(
                            value = baseRateStr,
                            onValueChange = { baseRateStr = it },
                            label = { Text("Base Rate (%) *") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.weight(1f),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = BrandPrimary,
                                unfocusedBorderColor = DarkSurfaceVariant
                            )
                        )

                        OutlinedTextField(
                            value = monthlyCapStr,
                            onValueChange = { monthlyCapStr = it },
                            label = { Text("Monthly Cap (₹)") },
                            placeholder = { Text("Blank = No Cap") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = BrandPrimary,
                                unfocusedBorderColor = DarkSurfaceVariant
                            )
                        )
                    }

                    // Category Bonus Rates Section
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "Accelerated Category Bonuses",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = TextPrimary
                        )

                        bonusRules.forEachIndexed { index, ruleInput ->
                            Surface(
                                color = DarkSurfaceVariant,
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier
                                        .padding(horizontal = 12.dp, vertical = 8.dp)
                                        .fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(
                                            text = ruleInput.category.displayName,
                                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                            color = TextPrimary
                                        )
                                        Text(
                                            text = "${ruleInput.rate}% Cashback",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = AmberGold
                                        )
                                    }
                                    IconButton(
                                        onClick = {
                                            val updated = bonusRules.toMutableList()
                                            updated.removeAt(index)
                                            bonusRules = updated
                                        }
                                    ) {
                                        Icon(
                                            Icons.Default.Delete,
                                            contentDescription = "Remove rule",
                                            tint = RedAlert,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                            }
                        }

                        // Add new rule sub-form
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            var expandedCategoryDropdown by remember { mutableStateOf(false) }

                            Box(modifier = Modifier.weight(1.3f)) {
                                OutlinedButton(
                                    onClick = { expandedCategoryDropdown = true },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = TextPrimary),
                                    border = BorderStroke(1.dp, DarkSurfaceVariant)
                                ) {
                                    Text(
                                        text = selectedNewCategory.displayName,
                                        fontSize = 12.sp,
                                        maxLines = 1
                                    )
                                }
                                DropdownMenu(
                                    expanded = expandedCategoryDropdown,
                                    onDismissRequest = { expandedCategoryDropdown = false }
                                ) {
                                    SpendCategory.entries.forEach { cat ->
                                        DropdownMenuItem(
                                            text = { Text(cat.displayName) },
                                            onClick = {
                                                selectedNewCategory = cat
                                                expandedCategoryDropdown = false
                                            }
                                        )
                                    }
                                }
                            }

                            OutlinedTextField(
                                value = newCategoryRateStr,
                                onValueChange = { newCategoryRateStr = it },
                                label = { Text("Rate %") },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                modifier = Modifier.weight(0.9f),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = BrandPrimary,
                                    unfocusedBorderColor = DarkSurfaceVariant
                                )
                            )

                            IconButton(
                                onClick = {
                                    val r = newCategoryRateStr.toDoubleOrNull()
                                    if (r != null && r > 0) {
                                        val updated = bonusRules.filter { it.category != selectedNewCategory }.toMutableList()
                                        updated.add(BonusRuleInput(selectedNewCategory, newCategoryRateStr))
                                        bonusRules = updated
                                    }
                                },
                                modifier = Modifier
                                    .background(BrandPrimary, RoundedCornerShape(10.dp))
                                    .size(44.dp)
                            ) {
                                Icon(Icons.Default.Add, contentDescription = "Add Rule", tint = Color.White)
                            }
                        }
                    }

                    // Exclusions Multi-Select (US-04)
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = "Reward Exclusions (0% Cashback)",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = TextPrimary
                        )
                        Text(
                            text = "Select any categories excluded by the bank (e.g. Fuel, Utilities, etc.)",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            SpendCategory.entries.take(4).forEach { cat ->
                                val isExcluded = selectedExclusions.contains(cat)
                                FilterChip(
                                    selected = isExcluded,
                                    onClick = {
                                        selectedExclusions = if (isExcluded) {
                                            selectedExclusions - cat
                                        } else {
                                            selectedExclusions + cat
                                        }
                                    },
                                    label = { Text(cat.displayName, fontSize = 11.sp) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = RedAlert.copy(alpha = 0.2f),
                                        selectedLabelColor = RedAlert
                                    )
                                )
                            }
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            SpendCategory.entries.drop(4).forEach { cat ->
                                val isExcluded = selectedExclusions.contains(cat)
                                FilterChip(
                                    selected = isExcluded,
                                    onClick = {
                                        selectedExclusions = if (isExcluded) {
                                            selectedExclusions - cat
                                        } else {
                                            selectedExclusions + cat
                                        }
                                    },
                                    label = { Text(cat.displayName, fontSize = 11.sp) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = RedAlert.copy(alpha = 0.2f),
                                        selectedLabelColor = RedAlert
                                    )
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
                            if (name.isBlank()) {
                                errorMessage = "Please enter a display name."
                                return@Button
                            }
                            if (issuer.isBlank()) {
                                errorMessage = "Please enter an issuer / bank name."
                                return@Button
                            }
                            val baseRate = baseRateStr.toDoubleOrNull()
                            if (baseRate == null || baseRate < 0) {
                                errorMessage = "Please enter a valid base reward rate."
                                return@Button
                            }

                            val cap = monthlyCapStr.toDoubleOrNull()
                            val today = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())

                            val rules = mutableListOf<RewardRule>()
                            bonusRules.forEach { ruleInput ->
                                ruleInput.rate.toDoubleOrNull()?.let { r ->
                                    rules.add(RewardRule(category = ruleInput.category, rate = r, capPeriod = "monthly"))
                                }
                            }
                            rules.add(RewardRule(category = null, rate = baseRate))

                            val id = initialMethod?.id ?: "card-${UUID.randomUUID().toString().take(8)}"

                            val method = PaymentMethod(
                                id = id,
                                name = name.trim(),
                                type = selectedType,
                                issuer = issuer.trim(),
                                network = network.trim(),
                                baseRate = baseRate,
                                rules = rules,
                                exclusions = selectedExclusions.toList(),
                                monthlyCap = cap,
                                currentMonthRewardUsed = initialMethod?.currentMonthRewardUsed ?: 0.0,
                                ruleLastConfirmed = today, // Confirmed on save (US-07)
                                isActive = initialMethod?.isActive ?: true,
                                cardLast4 = last4.ifBlank { null },
                                upiVpa = upiVpa.ifBlank { null }
                            )

                            onSave(method)
                        },
                        modifier = Modifier.weight(1.5f),
                        colors = ButtonDefaults.buttonColors(containerColor = BrandPrimary)
                    ) {
                        Text(if (isEditing) "Save Changes" else "Add Method", color = Color.White)
                    }
                }
            }
        }
    }
}
