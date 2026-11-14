package com.smartpaymentrouter.ui.screens.recommendation

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.smartpaymentrouter.data.model.*
import com.smartpaymentrouter.domain.parser.UpiParser
import com.smartpaymentrouter.ui.components.OfferActivationDialog
import com.smartpaymentrouter.ui.theme.*
import com.smartpaymentrouter.util.IntentHelper
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun RecommendationScreen(
    amount: Double,
    category: SpendCategory,
    payeeName: String?,
    payeeAddress: String?,
    mcc: String?,
    source: String,
    recommendations: List<Recommendation>,
    onBack: () -> Unit,
    onPaymentConfirmed: (PaymentLog) -> Unit
) {
    val context = LocalContext.current
    var showActivationDialogForOffer by remember { mutableStateOf<Offer?>(null) }
    var selectedMethodForPayment by remember { mutableStateOf<Recommendation?>(null) }

    if (recommendations.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(DarkBackground)
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("No payment methods active", color = TextPrimary)
                Spacer(modifier = Modifier.height(12.dp))
                Button(onClick = onBack) { Text("Go Back") }
            }
        }
        return
    }

    val topPick = recommendations.first()
    val runnersUp = recommendations.drop(1)

    val initiatePayment = { rec: Recommendation ->
        val offer = rec.breakdown.appliedOffer
        if (offer != null && offer.requiresActivation) {
            selectedMethodForPayment = rec
            showActivationDialogForOffer = offer
        } else {
            launchPaymentIntent(context, rec, payeeAddress, payeeName, amount, category, mcc)
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Top Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Optimal Payment Route",
                        style = MaterialTheme.typography.titleLarge,
                        color = TextPrimary
                    )
                    Text(
                        text = "₹${String.format(Locale.US, "%.2f", amount)} for ${category.displayName}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary
                    )
                }
                TextButton(onClick = onBack) {
                    Text("Modify", color = BrandPrimary)
                }
            }
        }

        // TOP PICK CARD (Prominent Gold / Indigo Card)
        item {
            Surface(
                color = DarkSurface,
                shape = RoundedCornerShape(20.dp),
                border = BorderStroke(2.dp, BrandPrimary),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    // Highest Reward Badge
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = AmberGold
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.EmojiEvents,
                                    contentDescription = null,
                                    tint = Color.Black,
                                    modifier = Modifier.size(14.dp)
                                )
                                Text(
                                    text = "HIGHEST REWARD PICK",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Black),
                                    color = Color.Black
                                )
                            }
                        }

                        Text(
                            text = "${topPick.effectiveRate}% Return",
                            style = MaterialTheme.typography.labelSmall,
                            color = GreenReward
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Card Name & Reward
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = topPick.method.name,
                                style = MaterialTheme.typography.headlineMedium.copy(fontSize = 20.sp),
                                color = TextPrimary
                            )
                            Text(
                                text = "${topPick.method.issuer} • ${topPick.method.network}",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary
                            )
                        }

                        Text(
                            text = "+₹${String.format(Locale.US, "%.2f", topPick.effectiveReward)}",
                            style = MaterialTheme.typography.headlineLarge.copy(
                                color = GreenReward,
                                fontWeight = FontWeight.Black
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Transparent Reasoning
                    Surface(
                        color = DarkSurfaceVariant,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text(
                                text = "Why this card: ${topPick.reason}",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextPrimary
                            )
                            topPick.offerReason?.let {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = it,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = AmberGold
                                )
                            }
                        }
                    }

                    // Rule Warning (FR-11)
                    topPick.ruleWarning?.let { warning ->
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Notice: $warning",
                            style = MaterialTheme.typography.bodySmall,
                            color = AmberGold
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Pay Now Action Button
                    Button(
                        onClick = { initiatePayment(topPick) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = BrandAccent)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Launch,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Pay with ${topPick.method.name}",
                            style = MaterialTheme.typography.titleLarge.copy(fontSize = 15.sp),
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Fallback Copy Payee & Amount
                    OutlinedButton(
                        onClick = {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            val clip = ClipData.newPlainText("Payment Info", "Payee: ${payeeAddress ?: payeeName ?: ""}\nAmount: ₹$amount")
                            clipboard.setPrimaryClip(clip)
                            Toast.makeText(context, "Copied amount and payee details to clipboard", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.fillMaxWidth(),
                        border = BorderStroke(1.dp, DarkSurfaceVariant)
                    ) {
                        Icon(imageVector = Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Copy Payee & Amount to Clipboard", fontSize = 12.sp, color = TextSecondary)
                    }
                }
            }
        }

        // RUNNER-UP CARDS
        if (runnersUp.isNotEmpty()) {
            item {
                Text(
                    text = "Runner-Up Payment Cards",
                    style = MaterialTheme.typography.titleMedium,
                    color = TextPrimary
                )
            }

            itemsIndexed(runnersUp) { index, rec ->
                val diff = topPick.effectiveReward - rec.effectiveReward

                Surface(
                    color = DarkSurface,
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.dp, DarkSurfaceVariant),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "#${index + 2} ",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextMuted
                                )
                                Text(
                                    text = rec.method.name,
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                    color = TextPrimary
                                )
                            }
                            Text(
                                text = rec.reason,
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                color = TextSecondary,
                                maxLines = 1
                            )
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "+₹${String.format(Locale.US, "%.2f", rec.effectiveReward)}",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = TextPrimary
                            )
                            if (diff > 0) {
                                Text(
                                    text = "-₹${String.format(Locale.US, "%.2f", diff)} less",
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                    color = RedAlert
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        IconButton(
                            onClick = { initiatePayment(rec) }
                        ) {
                            Icon(
                                imageVector = Icons.Default.ArrowForward,
                                contentDescription = "Use ${rec.method.name}",
                                tint = BrandPrimary
                            )
                        }
                    }
                }
            }
        }

        // Confirm Payment in History Button (FR-9)
        item {
            Spacer(modifier = Modifier.height(8.dp))
            Button(
                onClick = {
                    val log = PaymentLog(
                        id = "log_${System.currentTimeMillis()}",
                        timestamp = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).format(Date()),
                        amount = amount,
                        category = category,
                        merchantName = payeeName ?: payeeAddress ?: "Merchant",
                        payeeAddress = payeeAddress ?: "",
                        source = source,
                        recommendedMethodId = topPick.method.id,
                        recommendedMethodName = topPick.method.name,
                        chosenMethodId = topPick.method.id,
                        chosenMethodName = topPick.method.name,
                        expectedReward = topPick.effectiveReward,
                        actualReward = topPick.effectiveReward
                    )
                    onPaymentConfirmed(log)
                    Toast.makeText(context, "Payment logged! +₹${topPick.effectiveReward} reward earned.", Toast.LENGTH_LONG).show()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = DarkSurfaceVariant)
            ) {
                Icon(imageVector = Icons.Default.Check, contentDescription = null)
                Spacer(modifier = Modifier.width(6.dp))
                Text("Confirm Payment Completed (Save to Log)")
            }

            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Disclaimer: Rewards shown are estimates based on user-maintained rules. Smart Payment Router does not process payments directly.",
                style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp),
                color = TextMuted
            )
        }
    }

    // Offer activation reminder prompt
    showActivationDialogForOffer?.let { offer ->
        OfferActivationDialog(
            offer = offer,
            issuerName = selectedMethodForPayment?.method?.issuer ?: "Card",
            onConfirmActivated = {
                showActivationDialogForOffer = null
                selectedMethodForPayment?.let { rec ->
                    launchPaymentIntent(context, rec, payeeAddress, payeeName, amount, category, mcc)
                }
            },
            onDismiss = {
                showActivationDialogForOffer = null
            }
        )
    }
}

private fun launchPaymentIntent(
    context: Context,
    rec: Recommendation,
    payeeAddress: String?,
    payeeName: String?,
    amount: Double,
    category: SpendCategory,
    mcc: String?
) {
    val upiUri = UpiParser.buildUpiUri(
        payeeAddress = payeeAddress ?: "merchant@upi",
        payeeName = payeeName,
        amount = amount,
        category = category,
        mcc = mcc
    )

    IntentHelper.launchUpiIntent(context, upiUri)
}
