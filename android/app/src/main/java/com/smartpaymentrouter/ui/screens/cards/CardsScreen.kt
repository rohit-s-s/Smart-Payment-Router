package com.smartpaymentrouter.ui.screens.cards

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.smartpaymentrouter.data.model.PaymentMethod
import com.smartpaymentrouter.ui.theme.*
import kotlin.math.min

@Composable
fun CardsScreen(
    methods: List<PaymentMethod>,
    onToggleMethod: (String) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
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

        items(methods) { method ->
            val capPercent = if (method.monthlyCap != null && method.monthlyCap > 0) {
                min(1.0f, (method.currentMonthRewardUsed / method.monthlyCap).toFloat())
            } else 0f

            Surface(
                color = DarkSurface,
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, if (method.isActive) DarkSurfaceVariant else Color.Transparent),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = BrandPrimaryDark,
                                modifier = Modifier.size(40.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.CreditCard,
                                        contentDescription = null,
                                        tint = Color.White
                                    )
                                }
                            }
                            Column {
                                Text(
                                    text = method.name,
                                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                                    color = TextPrimary
                                )
                                Text(
                                    text = "${method.issuer} • ${method.network} ${method.cardLast4?.let { "(•• $it)" } ?: ""}",
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

                    // Base & Category rates
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
                            text = "${bonuses.size} bonus categories",
                            style = MaterialTheme.typography.bodySmall,
                            color = BrandPrimary
                        )
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
                                    color = TextSecondary
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
                }
            }
        }
    }
}
