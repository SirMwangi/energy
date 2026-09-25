package com.example.smartmoney.ui.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import kotlinx.coroutines.launch
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.smartmoney.data.local.UserProfileManager
import com.example.smartmoney.domain.model.BankAccount
import com.example.smartmoney.ui.accounts.AccountViewModel
import com.example.smartmoney.ui.components.AppTopBar
import com.example.smartmoney.ui.components.BankLogo
import com.example.smartmoney.ui.components.UserProfileAvatar
import com.example.smartmoney.ui.transactions.TransactionViewModel
import com.example.smartmoney.ui.theme.SmartMoneyColors
import java.math.BigDecimal
import java.math.RoundingMode
import java.text.DecimalFormat

/**
 * Data model for daily/weekly income trend points.
 */
@Immutable
data class IncomePoint(
    val dayLabel: String,
    val amount: BigDecimal
)

private val DefaultIncomeTrend = listOf(
    IncomePoint("Mon", BigDecimal("4200.00")),
    IncomePoint("Tue", BigDecimal("7500.00")),
    IncomePoint("Wed", BigDecimal("3800.00")),
    IncomePoint("Thu", BigDecimal("9200.00")),
    IncomePoint("Fri", BigDecimal("6400.00")),
    IncomePoint("Sat", BigDecimal("11500.00")),
    IncomePoint("Sun", BigDecimal("2400.00"))
)

private val DefaultBankAccounts = listOf(
    BankAccount("sample_equity", "Equity Bank", "**** 4821", "Debit"),
    BankAccount("sample_kcb", "KCB", "**** 9104", "Credit"),
    BankAccount("sample_ncba", "NCBA", "**** 3350", "Debit"),
    BankAccount("sample_stanbic", "Stanbic Bank", "**** 7712", "Credit")
)

/**
 * UI State for the Home screen containing mock/live financial metrics.
 */
@Immutable
data class HomeFinancialSummary(
    val totalBalance: BigDecimal = BigDecimal("23590.73"),
    val totalCashIn: BigDecimal = BigDecimal("45000.00"),
    val totalCashOut: BigDecimal = BigDecimal("12500.00"),
    val incomeTrend: List<IncomePoint> = DefaultIncomeTrend
)

private val DefaultHomeFinancialSummary = HomeFinancialSummary()

private val MutedSageCard = SmartMoneyColors.PaleMintGreen      // #DAEBE3: Pale Mint Green
private val DarkContrastColor = SmartMoneyColors.DarkSlateGreen // #657166: Dark Slate Green
private val DarkGreenPillBadge = SmartMoneyColors.DarkSlateGreen // #657166: Dark Slate Green

@Composable
fun HomeScreen(
    userName: String,
    accountViewModel: AccountViewModel? = null,
    transactionViewModel: TransactionViewModel? = null,
    summaryData: HomeFinancialSummary = remember { DefaultHomeFinancialSummary },
    onNotificationsClick: () -> Unit = {},
    onSettingsClick: () -> Unit = {},
    onProfileClick: () -> Unit = onSettingsClick
) {
    val scrollState = rememberScrollState()
    val profileBitmap by UserProfileManager.profileBitmap.collectAsState()

    val bankAccounts by (accountViewModel?.bankAccounts?.collectAsState()
        ?: remember { mutableStateOf(emptyList()) })

    val accounts by (accountViewModel?.accounts?.collectAsState()
        ?: remember { mutableStateOf(emptyList()) })

    val viewModelTotalBalance by (accountViewModel?.totalBalance?.collectAsState()
        ?: remember { mutableStateOf(BigDecimal.ZERO) })

    val activeTotalBalance = remember(accounts.isEmpty(), viewModelTotalBalance, summaryData.totalBalance) {
        if (accounts.isNotEmpty()) {
            viewModelTotalBalance
        } else {
            summaryData.totalBalance
        }
    }

    val activeSummaryData = remember(summaryData, activeTotalBalance) {
        summaryData.copy(totalBalance = activeTotalBalance)
    }

    val effectiveBankAccounts = remember(bankAccounts) {
        if (bankAccounts.isNotEmpty()) bankAccounts else DefaultBankAccounts
    }

    val pagerState = rememberPagerState(pageCount = { 2 })
    val coroutineScope = rememberCoroutineScope()

    var isExpanded by remember { mutableStateOf(false) }
    var isBalanceVisible by remember { mutableStateOf(true) }

    val chevronRotation by animateFloatAsState(
        targetValue = if (isExpanded) 180f else 0f,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "chevron_rotate"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
    ) {
        // =========================================================================
        // 1. THE FLUSH-TOP MAIN CARD WITH OVERLAPPING APPTOPBAR
        // Sits completely flush against the top edge without any top borders/margins.
        // Rounding applied ONLY to bottom corners (bottomStart = 32.dp, bottomEnd = 32.dp).
        // Transparent AppTopBar overlaps the card at the top, acting as its title bar.
        // =========================================================================
        Box(modifier = Modifier.fillMaxWidth()) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp),
                color = MutedSageCard,
                contentColor = DarkContrastColor,
                shadowElevation = 4.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(bottom = 22.dp)
                ) {
                    // Clearance for the overlapping transparent AppTopBar (standard TopAppBar height 64.dp + 8.dp)
                    Spacer(modifier = Modifier.height(72.dp))

                    // -----------------------------------------------------------------
                    // INNER BANNERS CAROUSEL:
                    // Smooth swipeable HorizontalPager between:
                    // Page 0 (Left): Total Balance banner card
                    // Page 1 (Right): Connected Apps banner card
                    // -----------------------------------------------------------------
                    HorizontalPager(
                        state = pagerState,
                        modifier = Modifier.fillMaxWidth(),
                        contentPadding = PaddingValues(horizontal = 20.dp),
                        pageSpacing = 16.dp,
                        verticalAlignment = Alignment.Top
                    ) { page ->
                        when (page) {
                            0 -> TotalBalanceBannerCard(
                                summaryData = activeSummaryData,
                                isBalanceVisible = isBalanceVisible,
                                onToggleBalanceVisibility = { isBalanceVisible = !isBalanceVisible }
                            )
                            1 -> ConnectedAppsBannerCard(
                                bankAccounts = effectiveBankAccounts
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // -----------------------------------------------------------------
                    // SCROLL DOT AND LINE INDICATOR:
                    // Animated indicator displaying an active line and inactive dot
                    // with smooth spring transitions and tap-to-scroll interaction.
                    // -----------------------------------------------------------------
                    CarouselPageIndicator(
                        pageCount = 2,
                        currentPage = pagerState.currentPage,
                        onDotClick = { targetPage ->
                            coroutineScope.launch {
                                pagerState.animateScrollToPage(targetPage)
                            }
                        },
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )
                }
            }

            // -----------------------------------------------------------------
            // APPTOPBAR (OVERLAPPING THE FLUSH-TOP CARD)
            // Transparent top bar seamlessly blending with the green card background.
            // Displays Profile Avatar, "Welcome back," + userName, Currency badge,
            // and Notifications & Settings action buttons.
            // -----------------------------------------------------------------
            AppTopBar(
                title = "Overview",
                userName = userName,
                profileBitmap = profileBitmap,
                isOverview = true,
                canNavigateBack = false,
                onProfileClick = onProfileClick,
                onNotificationsClick = onNotificationsClick,
                onSettingsClick = onSettingsClick,
                modifier = Modifier.fillMaxWidth()
            )
        }

        // =========================================================================
        // 2. THE NOTCH / DROPDOWN TOGGLE
        // At the absolute bottom center of the green container:
        // A small, overlapping notch containing Chevron Down with animated rotation.
        // =========================================================================
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .offset(y = (-14).dp),
            contentAlignment = Alignment.Center
        ) {
            Surface(
                onClick = { isExpanded = !isExpanded },
                shape = RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp),
                color = MutedSageCard,
                shadowElevation = 4.dp,
                modifier = Modifier.size(width = 56.dp, height = 28.dp)
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowDown,
                        contentDescription = if (isExpanded) "Collapse details" else "Expand details",
                        tint = DarkContrastColor,
                        modifier = Modifier
                            .size(22.dp)
                            .rotate(chevronRotation)
                    )
                }
            }
        }

        // =========================================================================
        // 3. EXPANDABLE DROPDOWN SECTION
        // Left empty per user request ("do not add anything to the drop down section right now leave it without anything")
        // =========================================================================
        AnimatedVisibility(
            visible = isExpanded,
            enter = expandVertically(animationSpec = spring(stiffness = Spring.StiffnessMediumLow)) + fadeIn(),
            exit = shrinkVertically(animationSpec = spring(stiffness = Spring.StiffnessMediumLow)) + fadeOut()
        ) {
            // Intentionally empty per user instruction
        }

        // =========================================================================
        // 4. DETACHED CASH IN TREND & LINKED ACCOUNTS SUMMARY
        // Completely detached from the banner, existing below the collapsed and expanded card.
        // =========================================================================
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
        ) {
            Spacer(modifier = Modifier.height(6.dp))

            // Cash In Trend (Income Trend Graph Card) - detached from banner
            IncomeTrendGraphCard(summaryData = activeSummaryData)

            Spacer(modifier = Modifier.height(20.dp))

            // Linked Accounts Summary Section
            LinkedAccountsSummarySection(bankAccounts = effectiveBankAccounts)

            Spacer(modifier = Modifier.navigationBarsPadding().height(96.dp))
        }
    }
}

/**
 * Total Balance Banner Card (Page 0 of the top container carousel):
 * Houses Total Balance, Privacy Toggle, Performance Pill Badge,
 * and the Total Cash In & Total Cash Out metric row.
 */
@Composable
private fun TotalBalanceBannerCard(
    summaryData: HomeFinancialSummary,
    isBalanceVisible: Boolean,
    onToggleBalanceVisibility: () -> Unit,
    modifier: Modifier = Modifier
) {
    ElevatedCard(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(1.586f),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 18.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // 1. Top Row: Title + Performance Badge (top right, like a card chip/contactless badge)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Total Balance",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                // Dark green pill-shaped badge: "↑ 24% Last week"
                Surface(
                    shape = RoundedCornerShape(50),
                    color = DarkGreenPillBadge
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowUpward,
                            contentDescription = null,
                            tint = MutedSageCard,
                            modifier = Modifier.size(11.dp)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = "24%",
                            color = MutedSageCard,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Last week",
                            color = Color.White.copy(alpha = 0.92f),
                            fontWeight = FontWeight.Medium,
                            fontSize = 11.sp
                        )
                    }
                }
            }

            // 2. Middle Row: Big Balance amount + visibility toggle
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Start
            ) {
                val displayBalance = remember(isBalanceVisible, summaryData.totalBalance) {
                    if (isBalanceVisible) {
                        formatKesCurrency(summaryData.totalBalance)
                    } else {
                        "••••••••"
                    }
                }

                Text(
                    text = displayBalance,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.width(10.dp))

                Surface(
                    onClick = onToggleBalanceVisibility,
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.size(26.dp)
                ) {
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                        Icon(
                            imageVector = if (isBalanceVisible) Icons.Outlined.Visibility else Icons.Outlined.VisibilityOff,
                            contentDescription = if (isBalanceVisible) "Hide balance" else "Show balance",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(15.dp)
                        )
                    }
                }
            }

            // 3. Bottom Row: Total Cash In & Total Cash Out (inside the credit card)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                MetricItem(
                    modifier = Modifier.weight(1f),
                    label = "Total Cash In",
                    amount = summaryData.totalCashIn,
                    isCredit = true
                )

                MetricItem(
                    modifier = Modifier.weight(1f),
                    label = "Total Cash Out",
                    amount = summaryData.totalCashOut,
                    isCredit = false
                )
            }
        }
    }
}

/**
 * Connected Apps Banner Card (Page 1 of the top container carousel):
 * Proportioned strictly to the dimensions of a physical credit card (aspect ratio 1.586 : 1).
 * Features an internal scrollable list allowing the user to scroll through and view all connected
 * banks and accounts beyond the initial field of view (FOV).
 */
@Composable
private fun ConnectedAppsBannerCard(
    bankAccounts: List<BankAccount>,
    modifier: Modifier = Modifier
) {
    val themeGreen = SmartMoneyColors.DarkSlateGreen
    val themeRed = MaterialTheme.colorScheme.error
    val bankScrollState = rememberScrollState()

    ElevatedCard(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(1.586f),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 18.dp, vertical = 14.dp)
        ) {
            // Header Row: "Connected Apps" title and real-time status pill
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Connected Apps",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant
                    ) {
                        Text(
                            text = "${bankAccounts.size}",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(50),
                    color = SmartMoneyColors.PaleMintGreen
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(themeGreen)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Real-time",
                            style = MaterialTheme.typography.labelSmall,
                            color = themeGreen,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (bankAccounts.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No connected bank or payment apps yet",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                // Scrollable container: allows the user to scroll through to view all other banks not initially in FOV
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .verticalScroll(bankScrollState),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    bankAccounts.forEach { bank ->
                        BankFlowMiniCard(bank = bank, themeGreen = themeGreen, themeRed = themeRed)
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Subtle footer hint showing sync status and scroll affordance
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (bankAccounts.size > 2) "↕ Scroll to view all ${bankAccounts.size} accounts" else "All accounts connected",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                        fontWeight = FontWeight.Medium,
                        fontSize = 10.5.sp
                    )
                    Text(
                        text = "Auto-sync",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = themeGreen,
                        fontSize = 10.5.sp
                    )
                }
            }
        }
    }
}

/**
 * Animated Dot and Line Carousel Page Indicator:
 * Active page transforms into a sleek elongated line/pill, while inactive pages are dots.
 * Supports smooth spring animation and tap-to-scroll interactivity.
 */
@Composable
private fun CarouselPageIndicator(
    pageCount: Int,
    currentPage: Int,
    onDotClick: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(pageCount) { index ->
            val isSelected = currentPage == index
            val width by animateDpAsState(
                targetValue = if (isSelected) 24.dp else 7.dp,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessMediumLow
                ),
                label = "indicator_width_$index"
            )
            val color by animateColorAsState(
                targetValue = if (isSelected) DarkContrastColor else DarkContrastColor.copy(alpha = 0.28f),
                animationSpec = tween(durationMillis = 250),
                label = "indicator_color_$index"
            )

            Box(
                modifier = Modifier
                    .size(width = width, height = 7.dp)
                    .clip(RoundedCornerShape(3.5.dp))
                    .background(color)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) {
                        onDotClick(index)
                    }
            )
        }
    }
}

/**
 * Compact mini-card for a connected bank within the dropdown overview.
 */
@Composable
private fun BankFlowMiniCard(
    bank: BankAccount,
    themeGreen: Color,
    themeRed: Color
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 7.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                BankLogo(
                    bankName = bank.bankName,
                    modifier = Modifier.size(26.dp),
                    shape = RoundedCornerShape(6.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = bank.bankName,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = bank.maskedAccountNumber,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.ArrowUpward,
                        contentDescription = "Cash In",
                        tint = themeGreen,
                        modifier = Modifier.size(11.dp)
                    )
                    Spacer(modifier = Modifier.width(2.dp))
                    Text(
                        text = "+15K",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = themeGreen
                    )
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.ArrowDownward,
                        contentDescription = "Cash Out",
                        tint = themeRed,
                        modifier = Modifier.size(11.dp)
                    )
                    Spacer(modifier = Modifier.width(2.dp))
                    Text(
                        text = "-4.2K",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = themeRed
                    )
                }
            }
        }
    }
}

/**
 * Metric pill displaying Cash In / Cash Out with directional badge and color coding.
 */
@Composable
private fun MetricItem(
    modifier: Modifier = Modifier,
    label: String,
    amount: BigDecimal,
    isCredit: Boolean
) {
    val themeGreen = SmartMoneyColors.DarkSlateGreen
    val themeRed = Color(0xFF992B1C)

    val badgeBg = if (isCredit) SmartMoneyColors.PaleMintGreen else SmartMoneyColors.LightSalmon
    val textColor = if (isCredit) themeGreen else themeRed
    val icon = if (isCredit) Icons.Default.ArrowUpward else Icons.Default.ArrowDownward
    val formattedAmount = remember(amount) { formatKesCurrency(amount) }

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = if (isCredit) SmartMoneyColors.PaleMintGreen.copy(alpha = 0.4f) else SmartMoneyColors.LightPeach.copy(alpha = 0.5f)
    ) {
        Column(modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(20.dp)
                        .clip(CircleShape)
                        .background(badgeBg),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = textColor,
                        modifier = Modifier.size(11.dp)
                    )
                }
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.height(3.dp))

            Text(
                text = formattedAmount,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = textColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

/**
 * Income Trend Graph Card built strictly using Jetpack Compose's native Canvas API.
 * No external charting libraries required.
 */
@Composable
private fun IncomeTrendGraphCard(summaryData: HomeFinancialSummary) {
    val trendPoints = summaryData.incomeTrend
    val graphColor = SmartMoneyColors.DarkSlateGreen // #657166: Dark Slate Green

    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Cash In Trend",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "7-Day Inflow Activity",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = SmartMoneyColors.PaleMintGreen // #DAEBE3
                ) {
                    Text(
                        text = "Weekly",
                        style = MaterialTheme.typography.labelSmall,
                        color = graphColor,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Canvas Line Chart
            IncomeLineGraphCanvas(
                points = trendPoints,
                lineColor = graphColor,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            // X-Axis Day Labels
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                trendPoints.forEach { point ->
                    Text(
                        text = point.dayLabel,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

/**
 * Native Jetpack Compose Canvas rendering a smooth cubic-bezier line graph
 * with soft vertical gradient fill beneath it. Optimized to eliminate per-frame allocations.
 */
@Composable
private fun IncomeLineGraphCanvas(
    points: List<IncomePoint>,
    lineColor: Color,
    modifier: Modifier = Modifier
) {
    if (points.size < 2) return

    val strokePath = remember { Path() }
    val fillPath = remember { Path() }

    val floatAmounts = remember(points) { points.map { it.amount.toFloat() } }
    val maxVal = remember(floatAmounts) { floatAmounts.maxOrNull() ?: 1f }
    val minVal = remember(floatAmounts) { (floatAmounts.minOrNull() ?: 0f).coerceAtLeast(0f) }
    val valueRange = remember(maxVal, minVal) { (maxVal - minVal).let { if (it <= 0f) 1f else it } * 1.2f }

    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height
        if (width <= 0f || height <= 0f) return@Canvas

        val topPadding = 16.dp.toPx()
        val bottomPadding = 16.dp.toPx()
        val usableHeight = height - topPadding - bottomPadding

        val stepX = width / (points.size - 1)

        // 1. Subtle horizontal grid reference lines
        val gridLines = 3
        val gridStep = usableHeight / gridLines
        val gridColor = Color.LightGray.copy(alpha = 0.35f)
        val gridStrokeWidth = 1.dp.toPx()
        for (i in 0..gridLines) {
            val gridY = topPadding + (i * gridStep)
            drawLine(
                color = gridColor,
                start = Offset(0f, gridY),
                end = Offset(width, gridY),
                strokeWidth = gridStrokeWidth
            )
        }

        // 2. Build Bezier paths by resetting the remembered instances (0 allocations per draw)
        strokePath.reset()
        fillPath.reset()

        var prevX = 0f
        var prevY = 0f

        floatAmounts.forEachIndexed { index, amount ->
            val x = index * stepX
            val normalizedY = (amount - minVal) / valueRange
            val y = height - bottomPadding - (normalizedY * usableHeight)

            if (index == 0) {
                strokePath.moveTo(x, y)
            } else {
                val controlX = prevX + (x - prevX) / 2f
                strokePath.cubicTo(
                    x1 = controlX,
                    y1 = prevY,
                    x2 = controlX,
                    y2 = y,
                    x3 = x,
                    y3 = y
                )
            }
            prevX = x
            prevY = y
        }

        fillPath.addPath(strokePath)
        fillPath.lineTo(prevX, height)
        fillPath.lineTo(0f, height)
        fillPath.close()

        // 3. Draw gradient fill
        drawPath(
            path = fillPath,
            brush = Brush.verticalGradient(
                colors = listOf(
                    lineColor.copy(alpha = 0.30f),
                    lineColor.copy(alpha = 0.05f),
                    Color.Transparent
                ),
                startY = topPadding,
                endY = height
            )
        )

        // 4. Draw line stroke
        drawPath(
            path = strokePath,
            color = lineColor,
            style = Stroke(
                width = 3.dp.toPx(),
                cap = StrokeCap.Round,
                join = StrokeJoin.Round
            )
        )

        // 5. Draw anchor points
        val outerRadius = 5.dp.toPx()
        val innerRadius = 3.dp.toPx()
        floatAmounts.forEachIndexed { index, amount ->
            val x = index * stepX
            val normalizedY = (amount - minVal) / valueRange
            val y = height - bottomPadding - (normalizedY * usableHeight)
            val center = Offset(x, y)

            drawCircle(
                color = Color.White,
                radius = outerRadius,
                center = center
            )
            drawCircle(
                color = lineColor,
                radius = innerRadius,
                center = center
            )
        }
    }
}

/**
 * Summary section at the bottom of the Home screen displaying linked bank accounts.
 * Shows a compact horizontal scroll row (LazyRow) of bank names, card types, and masked numbers.
 */
@Composable
private fun LinkedAccountsSummarySection(bankAccounts: List<BankAccount>) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Linked Accounts Summary",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            if (bankAccounts.isNotEmpty()) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Text(
                        text = "${bankAccounts.size} Linked",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (bankAccounts.isEmpty()) {
            ElevatedCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.elevatedCardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                ),
                elevation = CardDefaults.elevatedCardElevation(defaultElevation = 1.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.AccountBalance,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "No linked bank accounts yet. Link an account from the Accounts tab.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(
                    items = bankAccounts,
                    key = { it.id }
                ) { bankAccount ->
                    LinkedAccountMiniCard(bankAccount = bankAccount)
                }
            }
        }
    }
}

/**
 * Compact horizontal preview card for each linked bank account.
 */
@Composable
private fun LinkedAccountMiniCard(bankAccount: BankAccount) {
    ElevatedCard(
        modifier = Modifier.width(170.dp),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                BankLogo(
                    bankName = bankAccount.bankName,
                    modifier = Modifier.size(34.dp),
                    shape = RoundedCornerShape(8.dp)
                )

                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = MaterialTheme.colorScheme.secondaryContainer
                ) {
                    Text(
                        text = bankAccount.cardType,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = bankAccount.bankName,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = bankAccount.maskedAccountNumber,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * Thread-safe cached DecimalFormat instance avoiding allocations on every recomposition.
 */
private val CurrencyFormatter = ThreadLocal.withInitial {
    DecimalFormat("#,##0.00").apply {
        roundingMode = RoundingMode.HALF_EVEN
    }
}

/**
 * Formats monetary amounts with commas and 2 decimal places:
 * e.g., BigDecimal("23590.73") -> "$23,590.73"
 */
private fun formatKesCurrency(amount: BigDecimal): String {
    val formatter = CurrencyFormatter.get() ?: DecimalFormat("#,##0.00")
    return "$" + formatter.format(amount)
}
