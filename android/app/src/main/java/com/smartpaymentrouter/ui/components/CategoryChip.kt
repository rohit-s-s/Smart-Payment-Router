package com.smartpaymentrouter.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.smartpaymentrouter.data.model.CategorySource
import com.smartpaymentrouter.data.model.SpendCategory
import com.smartpaymentrouter.ui.theme.*

fun getCategoryIcon(category: SpendCategory): ImageVector = when (category) {
    SpendCategory.DINING -> Icons.Default.Restaurant
    SpendCategory.GROCERIES -> Icons.Default.ShoppingCart
    SpendCategory.FUEL -> Icons.Default.LocalGasStation
    SpendCategory.TRAVEL -> Icons.Default.Flight
    SpendCategory.UTILITIES -> Icons.Default.Bolt
    SpendCategory.SHOPPING -> Icons.Default.ShoppingBag
    SpendCategory.ENTERTAINMENT -> Icons.Default.Movie
    SpendCategory.OTHER -> Icons.Default.MoreHoriz
}

@Composable
fun CategorySelectorCard(
    category: SpendCategory,
    isSelected: Boolean,
    onSelect: () -> Unit,
    modifier: Modifier = Modifier
) {
    val borderColor = if (isSelected) BrandPrimary else DarkSurfaceVariant
    val bgColor = if (isSelected) Color(0xFF1E1B4B) else DarkSurface

    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable { onSelect() }
            .semantics { contentDescription = "Select ${category.displayName}. ${category.description}" },
        shape = RoundedCornerShape(12.dp),
        color = bgColor,
        border = BorderStroke(if (isSelected) 2.dp else 1.dp, borderColor)
    ) {
        Column(
            modifier = Modifier.padding(10.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = getCategoryIcon(category),
                    contentDescription = null,
                    tint = if (isSelected) AmberGold else BrandPrimary,
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    text = category.displayName,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    ),
                    color = TextPrimary,
                    maxLines = 1
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = category.description,
                style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp),
                color = TextSecondary,
                maxLines = 1
            )
        }
    }
}

@Composable
fun CategorySourceBadge(
    category: SpendCategory,
    source: CategorySource,
    mcc: String? = null,
    modifier: Modifier = Modifier
) {
    val sourceLabel = when (source) {
        CategorySource.MCC -> "From QR MCC ${mcc ?: ""}".trim()
        CategorySource.MERCHANT_HISTORY -> "From Past Merchant"
        CategorySource.MANUAL -> "Manually Chosen"
        CategorySource.DEFAULT -> "Default"
    }

    Surface(
        shape = RoundedCornerShape(20.dp),
        color = DarkSurfaceVariant,
        border = BorderStroke(1.dp, Color(0xFF475569)),
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                imageVector = getCategoryIcon(category),
                contentDescription = null,
                tint = AmberGold,
                modifier = Modifier.size(14.dp)
            )
            Text(
                text = category.displayName,
                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                color = TextPrimary
            )
            Text(
                text = "• $sourceLabel",
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                color = TextSecondary
            )
        }
    }
}
