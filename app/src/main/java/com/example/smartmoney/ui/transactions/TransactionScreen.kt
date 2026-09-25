package com.example.smartmoney.ui.transactions

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import com.example.smartmoney.ui.components.BankLogo
import com.example.smartmoney.ui.components.findBankInText
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.smartmoney.domain.model.Transaction
import com.example.smartmoney.ui.theme.LocalDarkTheme
import com.example.smartmoney.ui.theme.SmartMoneyColors
import java.math.BigDecimal
import java.math.RoundingMode
import java.text.DecimalFormat
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Composable
fun TransactionScreen(
    viewModel: TransactionViewModel
) {
    val uiState by viewModel.uiState.collectAsState()
    val filteredTransactions by viewModel.filteredTransactions.collectAsState()
    val selectedFilter by viewModel.selectedFilter.collectAsState()

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Text(
                text = "Transactions",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 8.dp, bottom = 12.dp)
            )

            when (val state = uiState) {
                is TransactionUiState.Loading -> {
                    LoadingTransactionsView()
                }
                is TransactionUiState.Success -> {
                    if (state.transactions.isEmpty()) {
                        EmptyTransactionsView()
                    } else {
                        TransactionsListView(
                            filteredTransactions = filteredTransactions,
                            selectedFilter = selectedFilter,
                            onFilterChange = { viewModel.setFilter(it) }
                        )
                    }
                }
                is TransactionUiState.Error -> {
                    ErrorTransactionsView(
                        message = state.message,
                        onRetry = { viewModel.refreshTransactions() }
                    )
                }
            }
        }
    }
}

@Composable
fun TransactionsListView(
    filteredTransactions: List<Transaction>,
    selectedFilter: String = "ALL",
    onFilterChange: (String) -> Unit = {}
) {
    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf("ALL" to "All", "CREDIT" to "Inflow", "DEBIT" to "Outflow").forEach { (filterKey, label) ->
                val isSelected = selectedFilter == filterKey
                val containerColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
                val contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = containerColor,
                    modifier = Modifier.clickable { onFilterChange(filterKey) }
                ) {
                    Text(
                        text = label,
                        color = contentColor,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                    )
                }
            }
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 100.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(
                items = filteredTransactions,
                key = { it.id }
            ) { transaction ->
                TransactionCard(transaction = transaction)
            }
        }
    }
}

@Composable
fun TransactionCard(transaction: Transaction) {
    val isCredit = remember(transaction.type) { transaction.type.equals("CREDIT", ignoreCase = true) }
    val isDark = LocalDarkTheme.current
    val amountColor = if (isCredit) {
        if (isDark) Color(0xFF4ADE80) else SmartMoneyColors.DarkSlateGreen
    } else {
        if (isDark) MaterialTheme.colorScheme.error else Color(0xFF992B1C)
    }
    val badgeBgColor = if (isCredit) {
        if (isDark) Color(0xFF133629) else SmartMoneyColors.PaleMintGreen
    } else {
        if (isDark) Color(0xFF4A1818) else SmartMoneyColors.LightSalmon.copy(alpha = 0.45f)
    }
    val badgeTextColor = if (isCredit) {
        if (isDark) Color(0xFF4ADE80) else SmartMoneyColors.DarkSlateGreen
    } else {
        if (isDark) Color(0xFFFF6B6B) else Color(0xFF992B1C)
    }
    val badgeSymbol = if (isCredit) "↓" else "↑"
    val detectedBank = remember(transaction.description) { findBankInText(transaction.description) }
    val formattedAmount = remember(transaction.amount, isCredit) { formatKesAmount(transaction.amount, isCredit) }
    val formattedTime = remember(transaction.timestamp) { formatTimestamp(transaction.timestamp) }

    ElevatedCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Credit/Debit Direction Badge or Bank Logo with indicator
            if (detectedBank != null) {
                Box(modifier = Modifier.size(40.dp)) {
                    BankLogo(
                        bankName = detectedBank,
                        modifier = Modifier.fillMaxSize(),
                        shape = CircleShape
                    )
                    Box(
                        modifier = Modifier
                            .size(16.dp)
                            .align(Alignment.BottomEnd)
                            .clip(CircleShape)
                            .background(badgeBgColor),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = badgeSymbol,
                            color = badgeTextColor,
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp
                        )
                    }
                }
            } else {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(badgeBgColor),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = badgeSymbol,
                        color = badgeTextColor,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Description and Timestamp
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = transaction.description ?: "Energy Transaction",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(4.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (detectedBank != null) {
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                BankLogo(
                                    bankName = detectedBank,
                                    modifier = Modifier.size(14.dp),
                                    shape = RoundedCornerShape(2.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = detectedBank,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                    }

                    Text(
                        text = formattedTime,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Formatted Amount (+ KES X,XXX.XX / - KES X,XXX.XX)
            Text(
                text = formattedAmount,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = amountColor
            )
        }
    }
}

@Composable
fun LoadingTransactionsView() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            CircularProgressIndicator()
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Loading transactions...",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun EmptyTransactionsView() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(24.dp)
        ) {
            Text(
                text = "No Transactions Found",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Your prepaid tokens, meter charges, and utility payments will appear here.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun ErrorTransactionsView(
    message: String,
    onRetry: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(24.dp)
        ) {
            Text(
                text = "Unable to load transactions",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.error,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = message,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = onRetry) {
                Text("Retry")
            }
        }
    }
}

private val KesDecimalFormat = ThreadLocal.withInitial {
    DecimalFormat("#,##0.00").apply {
        roundingMode = RoundingMode.HALF_EVEN
    }
}

private val TimestampFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("MMM dd, yyyy • hh:mm a")
    .withZone(ZoneId.systemDefault())

/**
 * Formats monetary amounts with commas and 2 decimals using cached DecimalFormat.
 */
private fun formatKesAmount(amount: BigDecimal, isCredit: Boolean): String {
    val formatter = KesDecimalFormat.get() ?: DecimalFormat("#,##0.00")
    val prefix = if (isCredit) "+ KES " else "- KES "
    return prefix + formatter.format(amount)
}

/**
 * Formats ISO 8601 timestamp string using thread-safe DateTimeFormatter.
 */
private fun formatTimestamp(isoString: String): String {
    return try {
        val instant = Instant.parse(isoString)
        TimestampFormatter.format(instant)
    } catch (e: Exception) {
        isoString.replace("T", " ").replace("Z", "")
    }
}
