package com.example.energy.ui.budget

import androidx.compose.foundation.background
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.Savings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.energy.domain.model.Budget
import com.example.energy.ui.accounts.AccountViewModel
import com.example.energy.ui.budget.components.BudgetCard
import com.example.energy.ui.budget.components.BudgetFormBottomSheet
import com.example.energy.ui.theme.LocalDarkTheme
import com.example.energy.ui.theme.SmartMoneyColors
import java.text.NumberFormat
import java.util.Locale

@Composable
fun BudgetScreen(
    viewModel: BudgetViewModel,
    accountViewModel: AccountViewModel? = null,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val bankAccounts by accountViewModel?.bankAccounts?.collectAsState() ?: remember {
        mutableStateOf(emptyList())
    }

    val isDark = LocalDarkTheme.current
    val currencyFormat = NumberFormat.getCurrencyInstance(Locale("en", "KE")).apply {
        currency = java.util.Currency.getInstance("KES")
    }

    var isBottomSheetOpen by remember { mutableStateOf(false) }
    var budgetToEdit by remember { mutableStateOf<Budget?>(null) }
    var budgetToDeleteId by remember { mutableStateOf<String?>(null) }

    val accountOptions = bankAccounts.map {
        it.id to "${it.bankName} · ${it.maskedAccountNumber}"
    }

    Scaffold(
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = {
                    budgetToEdit = null
                    isBottomSheetOpen = true
                },
                icon = { Icon(Icons.Default.Add, contentDescription = "Add Budget") },
                text = { Text("New Budget", fontWeight = FontWeight.Bold) },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 88.dp)
            ) {
                // 1. Metric Summary Banner
                item {
                    BudgetSummaryBanner(
                        totalAllocatedMajor = uiState.totalAllocatedMinor / 100.0,
                        totalSpentMajor = uiState.totalSpentMinor / 100.0,
                        activeCount = uiState.activeBudgetCount,
                        currencyFormat = currencyFormat,
                        isDark = isDark
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                }

                // 2. Section Header
                item {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Active Budget Limits",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Text(
                            text = "${uiState.budgets.size} categories",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                }

                // 3. Budgets List or Empty State
                if (uiState.budgets.isEmpty()) {
                    item {
                        BudgetEmptyState(
                            onAddBudget = {
                                budgetToEdit = null
                                isBottomSheetOpen = true
                            },
                            isDark = isDark
                        )
                    }
                } else {
                    items(uiState.budgets, key = { it.budget.id }) { summary ->
                        val matchedAccount = bankAccounts.find { it.id == summary.budget.accountId }
                        val accountName = matchedAccount?.let { "${it.bankName} · ${it.maskedAccountNumber}" }

                        BudgetCard(
                            summary = summary,
                            accountName = accountName,
                            onEdit = {
                                budgetToEdit = summary.budget
                                isBottomSheetOpen = true
                            },
                            onDelete = {
                                budgetToDeleteId = summary.budget.id
                            }
                        )
                    }
                }
            }
        }

        // Add / Edit Bottom Sheet Form
        if (isBottomSheetOpen) {
            BudgetFormBottomSheet(
                initialBudget = budgetToEdit,
                accountOptions = accountOptions,
                onDismiss = {
                    isBottomSheetOpen = false
                    budgetToEdit = null
                },
                onSave = { id, category, allocatedMinor, start, end, threshold, accountId ->
                    viewModel.saveBudget(
                        id = id,
                        category = category,
                        allocatedMinor = allocatedMinor,
                        start = start,
                        end = end,
                        threshold = threshold,
                        accountId = accountId
                    )
                }
            )
        }

        // Delete Confirmation Dialog
        if (budgetToDeleteId != null) {
            AlertDialog(
                onDismissRequest = { budgetToDeleteId = null },
                title = { Text("Delete Budget Allocation?") },
                text = { Text("Are you sure you want to remove this budget allocation? Historical transactions will remain preserved.") },
                confirmButton = {
                    Button(
                        onClick = {
                            budgetToDeleteId?.let { viewModel.deleteBudget(it) }
                            budgetToDeleteId = null
                        }
                    ) {
                        Text("Delete")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { budgetToDeleteId = null }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}

@Composable
private fun BudgetSummaryBanner(
    totalAllocatedMajor: Double,
    totalSpentMajor: Double,
    activeCount: Int,
    currencyFormat: NumberFormat,
    isDark: Boolean
) {
    val cardBg = if (isDark) SmartMoneyColors.DarkSurface else SmartMoneyColors.SlateBackground
    val cardBorder = if (isDark) SmartMoneyColors.DarkBorderLine else SmartMoneyColors.BorderLine
    val textPrimary = if (isDark) SmartMoneyColors.DarkTextPrimary else SmartMoneyColors.TextPrimary
    val textMuted = if (isDark) SmartMoneyColors.DarkInactive else SmartMoneyColors.TextMuted

    Row(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        // Total Allocated
        Card(
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = cardBg),
            modifier = Modifier.weight(1f)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text("Total Allocated", fontSize = 11.sp, color = textMuted)
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = currencyFormat.format(totalAllocatedMajor),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = textPrimary
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text("$activeCount active cycles", fontSize = 10.sp, color = textMuted)
            }
        }

        // Total Spent
        Card(
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = cardBg),
            modifier = Modifier.weight(1f)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text("Total Spent", fontSize = 11.sp, color = textMuted)
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = currencyFormat.format(totalSpentMajor),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(2.dp))
                val remaining = (totalAllocatedMajor - totalSpentMajor).coerceAtLeast(0.0)
                Text("${currencyFormat.format(remaining)} remaining", fontSize = 10.sp, color = textMuted)
            }
        }
    }
}

@Composable
private fun BudgetEmptyState(
    onAddBudget: () -> Unit,
    isDark: Boolean
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isDark) SmartMoneyColors.DarkSurface else Color.White
        ),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.Savings,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(32.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "No Budgets Configured",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Set financial allocations for groceries, utilities, shopping, and monitor your monthly expenditure.",
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
            Spacer(modifier = Modifier.height(20.dp))
            Button(
                onClick = onAddBudget,
                shape = RoundedCornerShape(10.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Create First Budget")
            }
        }
    }
}
