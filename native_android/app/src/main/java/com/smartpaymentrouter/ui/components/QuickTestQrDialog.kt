package com.smartpaymentrouter.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.smartpaymentrouter.ui.theme.*

data class QuickTestSample(
    val title: String,
    val tag: String,
    val upiString: String,
    val explanation: String
)

val ANDROID_SAMPLE_QRS = listOf(
    QuickTestSample(
        "Swiggy Dining (Amount ₹650 + MCC 5812)",
        "US-01 Fixed Amount",
        "upi://pay?pa=swiggy@icici&pn=Swiggy+Food&am=650.00&cu=INR&mc=5812&tn=Table+Dinner",
        "Pre-fills ₹650, maps MCC 5812 to Dining & Food, calculates 5% reward"
    ),
    QuickTestSample(
        "HPCL Petrol Pump (Missing Amount, MCC 5541)",
        "US-01 Missing Amount",
        "upi://pay?pa=hpclpump@upi&pn=HPCL+AutoCare&cu=INR&mc=5541&tn=Petrol+Bay+3",
        "Pre-fills Fuel category from MCC 5541, prompts user for payment amount"
    ),
    QuickTestSample(
        "Blinkit Quick Groceries (Amount ₹380 + MCC 5411)",
        "US-01 & US-03",
        "upi://pay?pa=blinkit@kotak&pn=Blinkit+Quick&am=380.00&cu=INR&mc=5411&tn=Daily+Groceries",
        "Pre-fills ₹380, maps MCC 5411 to Groceries, optimizes cashback card"
    ),
    QuickTestSample(
        "Croma Web Link (Non-UPI QR Fallback)",
        "US-01 Non-UPI",
        "https://www.croma.com/checkout/order-7890",
        "Detects web URL, switches to manual mode, prompts for amount and category"
    ),
    QuickTestSample(
        "BESCOM Power (Saved Merchant Memory, No MCC)",
        "US-03 Memory",
        "upi://pay?pa=bescom@billdesk&pn=BESCOM+Electricity+Bill&am=1250.00&cu=INR",
        "No MCC in QR, but auto-maps to Utilities from saved merchant history"
    )
)

@Composable
fun QuickTestQrDialog(
    onSelectSample: (String) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Quick Test Scenarios (Epic 1)",
                style = MaterialTheme.typography.titleLarge,
                color = TextPrimary
            )
        },
        text = {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(ANDROID_SAMPLE_QRS) { sample ->
                    Surface(
                        color = DarkSurfaceVariant,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                onSelectSample(sample.upiString)
                                onDismiss()
                            }
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = sample.title,
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                    color = TextPrimary,
                                    modifier = Modifier.weight(1f)
                                )
                                Text(
                                    text = sample.tag,
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                    color = AmberGold
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = sample.explanation,
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                color = TextSecondary
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close", color = TextSecondary)
            }
        },
        containerColor = DarkSurface
    )
}
