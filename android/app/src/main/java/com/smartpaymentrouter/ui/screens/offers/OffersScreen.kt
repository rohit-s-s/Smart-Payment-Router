package com.smartpaymentrouter.ui.screens.offers

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocalOffer
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.smartpaymentrouter.data.model.Offer
import com.smartpaymentrouter.data.model.PaymentMethod
import com.smartpaymentrouter.ui.theme.*

@Composable
fun OffersScreen(
    offers: List<Offer>,
    methods: List<PaymentMethod>,
    onToggleOffer: (String) -> Unit
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
                text = "Active Offers & Boosters",
                style = MaterialTheme.typography.headlineMedium,
                color = TextPrimary
            )
            Text(
                text = "Track bank promotions, instant category discounts, and fuel surcharge waivers.",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary
            )
        }

        items(offers) { offer ->
            val card = methods.find { it.id == offer.methodId }

            Surface(
                color = DarkSurface,
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, if (offer.isActive) DarkSurfaceVariant else Color.Transparent),
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
                                color = Color(0xFF312E81),
                                modifier = Modifier.size(36.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.LocalOffer,
                                        contentDescription = null,
                                        tint = AmberGold,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                            Column {
                                Text(
                                    text = offer.title,
                                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                                    color = TextPrimary
                                )
                                Text(
                                    text = card?.name ?: "All Cards",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextSecondary
                                )
                            }
                        }

                        Switch(
                            checked = offer.isActive,
                            onCheckedChange = { onToggleOffer(offer.id) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = AmberGold,
                                checkedTrackColor = BrandPrimary
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = offer.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = DarkSurfaceVariant
                        ) {
                            Text(
                                text = "Min spend: ₹${offer.minAmount.toInt()}",
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                color = TextPrimary,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                            )
                        }

                        if (offer.stacks) {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = Color(0xFF064E3B)
                            ) {
                                Text(
                                    text = "Stacks with card",
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
                                    text = "Needs activation",
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                    color = AmberGold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
