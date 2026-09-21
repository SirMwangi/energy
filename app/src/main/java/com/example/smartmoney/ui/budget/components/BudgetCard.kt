package com.example.smartmoney.ui.budget.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.smartmoney.domain.model.BudgetStatus
import com.example.smartmoney.domain.model.BudgetSummary
import com.example.smartmoney.ui.theme.LocalDarkTheme
import com.example.smartmoney.ui.theme.SmartMoneyColors
import java.text.NumberFormat
import java.util.Locale

@Composable
fun BudgetCard(
    summary: BudgetSummary,
    accountName: String? = null,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isDark = LocalDarkTheme.current
    val budget = summary.budget
    val currencyFormat = NumberFormat.getCurrencyInstance(Locale("en", "KE")).apply {
        currency = java.util.Currency.getInstance("KES")
    }

    val cardBg = if (isDark) SmartMoneyColors.DarkSurface else Color.White
    val textPrimary = if (isDark) SmartMoneyColors.DarkTextPrimary else SmartMoneyColors.TextPrimary
    val textMuted = if (isDark) SmartMoneyColors.DarkInactive else SmartMoneyColors.TextMuted

    val (statusText, statusBg, statusFg) = when (summary.status) {
        BudgetStatus.OVER_BUDGET -> Triple(
            "Over budget",
            if (isDark) Color(0xFF4A1818) else Color(0xFFFDE8E8),
            if (isDark) Color(0xFFFF6B6B) else Color(0xFFE02424)
        )
        BudgetStatus.APPROACHING_LIMIT -> Triple(
            "Approaching limit",
            if (isDark) Color(0xFF423512) else Color(0xFFFEF08A),
            if (isDark) Color(0xFFFDE047) else Color(0xFF854D0E)
        )
        BudgetStatus.WITHIN_BUDGET -> Triple(
            "Within budget",
            if (isDark) Color(0xFF133629) else Color(0xFFDEF7EC),
            if (isDark) Color(0xFF4ADE80) else Color(0xFF03543F)
        )
    }

    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = cardBg),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            // Header Row: Category & Status Badge
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = budget.category,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = textPrimary
                )

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(statusBg)
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = statusText,
                        color = statusFg,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))
            val accountScopeLabel = if (!budget.accountId.isNullOrBlank()) {
                " · ${accountName ?: "Account Specific"}"
            } else {
                " · All Accounts"
            }
            Text(
                text = "${budget.start} to ${budget.end}$accountScopeLabel",
                fontSize = 12.sp,
                color = textMuted
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Spend vs Allocated Figures
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column {
                    Text("Spent", fontSize = 11.sp, color = textMuted)
                    Text(
                        text = currencyFormat.format(summary.spentMajor),
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = if (summary.status == BudgetStatus.OVER_BUDGET) statusFg else textPrimary
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text("Limit", fontSize = 11.sp, color = textMuted)
                    Text(
                        text = currencyFormat.format(budget.allocatedMajor),
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = textPrimary
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Progress Bar with Threshold Color Coding
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(if (isDark) SmartMoneyColors.DarkBorderLine else Color(0xFFE2E8F0))
            ) {
                val fillFraction = (summary.percentUsed / 100f).coerceIn(0f, 1f)
                Box(
                    modifier = Modifier
                        .fillMaxWidth(fillFraction)
                        .fillMaxHeight()
                        .background(
                            when (summary.status) {
                                BudgetStatus.OVER_BUDGET -> Color(0xFFEF4444)
                                BudgetStatus.APPROACHING_LIMIT -> Color(0xFFEAB308)
                                BudgetStatus.WITHIN_BUDGET -> Color(0xFF10B981)
                            }
                        )
                )
            }

            Spacer(modifier = Modifier.height(6.dp))
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "${summary.percentUsed.toInt()}% spent (${budget.threshold}% alert threshold)",
                    fontSize = 11.sp,
                    color = textMuted
                )
                Text(
                    text = "${currencyFormat.format(summary.remainingMajor)} left",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    color = textMuted
                )
            }

            Spacer(modifier = Modifier.height(14.dp))
            HorizontalDivider(
                color = if (isDark) SmartMoneyColors.DarkBorderLine else Color(0xFFF1F5F9)
            )
            Spacer(modifier = Modifier.height(6.dp))

            // Action Buttons
            Row(
                horizontalArrangement = Arrangement.End,
                modifier = Modifier.fillMaxWidth()
            ) {
                TextButton(onClick = onEdit) {
                    Icon(
                        imageVector = Icons.Outlined.Edit,
                        contentDescription = "Edit budget",
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Edit", fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
                }
                Spacer(modifier = Modifier.width(8.dp))
                TextButton(
                    onClick = onDelete,
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Delete,
                        contentDescription = "Delete budget",
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Delete", fontSize = 12.sp)
                }
            }
        }
    }
}
