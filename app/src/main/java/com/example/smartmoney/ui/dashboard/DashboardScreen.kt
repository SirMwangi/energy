package com.example.smartmoney.ui.dashboard

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.smartmoney.data.local.UserProfileManager
import com.example.smartmoney.ui.accounts.AccountScreen
import com.example.smartmoney.ui.accounts.AccountViewModel
import com.example.smartmoney.ui.budget.BudgetScreen
import com.example.smartmoney.ui.budget.BudgetViewModel
import com.example.smartmoney.ui.components.AppTopBar
import com.example.smartmoney.ui.components.MainResponsiveShell
import com.example.smartmoney.ui.home.HomeScreen
import com.example.smartmoney.ui.investment.InvestmentScreen
import com.example.smartmoney.ui.investment.InvestmentViewModel
import com.example.smartmoney.ui.menu.MenuScreen
import com.example.smartmoney.ui.more.MoreScreen
import com.example.smartmoney.ui.navigation.Screen
import com.example.smartmoney.ui.theme.SmartMoneyColors
import com.example.smartmoney.ui.transactions.TransactionScreen
import com.example.smartmoney.ui.transactions.TransactionViewModel
import kotlinx.coroutines.launch

@Composable
fun DashboardScreen(
    userName: String,
    userEmail: String? = null,
    accountViewModel: AccountViewModel,
    transactionViewModel: TransactionViewModel,
    budgetViewModel: BudgetViewModel,
    investmentViewModel: InvestmentViewModel,
    isDarkMode: Boolean = false,
    onToggleDarkMode: (Boolean) -> Unit = {},
    onLogout: () -> Unit
) {
    val navController = rememberNavController()
    val bottomBarPagerState = rememberPagerState(
        initialPage = 0,
        pageCount = { Screen.bottomBarScreens.size }
    )
    val coroutineScope = rememberCoroutineScope()
    val profileBitmap by UserProfileManager.profileBitmap.collectAsState()

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val navRoute = navBackStackEntry?.destination?.route ?: Screen.Dashboard.route

    // Handle system Back button: if on Accounts, Activity, or More tab, return to Overview tab
    BackHandler(enabled = navRoute == Screen.Dashboard.route && bottomBarPagerState.currentPage != 0) {
        coroutineScope.launch {
            bottomBarPagerState.animateScrollToPage(0)
        }
    }

    MainResponsiveShell(
        navController = navController,
        userName = userName,
        pagerState = bottomBarPagerState,
        onSignOut = onLogout
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = Screen.Dashboard.route,
            enterTransition = {
                slideInHorizontally(
                    initialOffsetX = { fullWidth -> fullWidth },
                    animationSpec = tween(durationMillis = 350, easing = FastOutSlowInEasing)
                ) + fadeIn(animationSpec = tween(durationMillis = 350))
            },
            exitTransition = {
                slideOutHorizontally(
                    targetOffsetX = { fullWidth -> -fullWidth / 4 },
                    animationSpec = tween(durationMillis = 350, easing = FastOutSlowInEasing)
                ) + fadeOut(animationSpec = tween(durationMillis = 350))
            },
            popEnterTransition = {
                slideInHorizontally(
                    initialOffsetX = { fullWidth -> -fullWidth / 4 },
                    animationSpec = tween(durationMillis = 350, easing = FastOutSlowInEasing)
                ) + fadeIn(animationSpec = tween(durationMillis = 350))
            },
            popExitTransition = {
                slideOutHorizontally(
                    targetOffsetX = { fullWidth -> fullWidth },
                    animationSpec = tween(durationMillis = 350, easing = FastOutSlowInEasing)
                ) + fadeOut(animationSpec = tween(durationMillis = 350))
            },
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            composable(Screen.Dashboard.route) {
                HorizontalPager(
                    state = bottomBarPagerState,
                    modifier = Modifier.fillMaxSize(),
                    beyondViewportPageCount = 1
                ) { page ->
                    when (page) {
                        0 -> HomeScreen(
                            userName = userName,
                            accountViewModel = accountViewModel,
                            transactionViewModel = transactionViewModel,
                            onNotificationsClick = { navController.navigate(Screen.Notifications.route) },
                            onSettingsClick = {
//                                navController.navigate(Screen.Settings.route)

                                              },
                            onProfileClick = { navController.navigate(Screen.Settings.route) }
                        )
                        1 -> Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(MaterialTheme.colorScheme.background)
                        ) {
                            AppTopBar(
                                title = Screen.Accounts.title,
                                userName = userName,
                                profileBitmap = profileBitmap,
                                isOverview = false,
                                canNavigateBack = false,
                                onNotificationsClick = { navController.navigate(Screen.Notifications.route) },
                                onSettingsClick = { navController.navigate(Screen.Settings.route) },
                                onProfileClick = { navController.navigate(Screen.Settings.route) },
                                modifier = Modifier.statusBarsPadding()
                            )
                            Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                                AccountScreen(viewModel = accountViewModel)
                            }
                        }
                        2 -> Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(MaterialTheme.colorScheme.background)
                        ) {
                            AppTopBar(
                                title = Screen.Transactions.title,
                                userName = userName,
                                profileBitmap = profileBitmap,
                                isOverview = false,
                                canNavigateBack = false,
                                onNotificationsClick = { navController.navigate(Screen.Notifications.route) },
                                onSettingsClick = { navController.navigate(Screen.Settings.route) },
                                onProfileClick = { navController.navigate(Screen.Settings.route) },
                                modifier = Modifier.statusBarsPadding()
                            )
                            Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                                TransactionScreen(viewModel = transactionViewModel)
                            }
                        }
                        3 -> Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(MaterialTheme.colorScheme.background)
                        ) {
                            AppTopBar(
                                title = Screen.Menu.title,
                                userName = userName,
                                profileBitmap = profileBitmap,
                                isOverview = false,
                                canNavigateBack = false,
                                onNotificationsClick = { navController.navigate(Screen.Notifications.route) },
                                onSettingsClick = { navController.navigate(Screen.Settings.route) },
                                onProfileClick = { navController.navigate(Screen.Settings.route) },
                                modifier = Modifier.statusBarsPadding()
                            )
                            Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                                MenuScreen(
                                    userName = userName,
                                    userEmail = userEmail,
                                    onNavigate = { screen ->
                                        if (screen in Screen.bottomBarScreens) {
                                            val targetIndex = Screen.bottomBarScreens.indexOf(screen)
                                            if (targetIndex >= 0) {
                                                coroutineScope.launch {
                                                    bottomBarPagerState.animateScrollToPage(targetIndex)
                                                }
                                            }
                                        } else {
                                            navController.navigate(screen.route) {
                                                launchSingleTop = true
                                            }
                                        }
                                    },
                                    onSignOut = onLogout
                                )
                            }
                        }
                    }
                }
            }
            composable(Screen.Accounts.route) {
                LaunchedEffect(Unit) {
                    navController.popBackStack(Screen.Dashboard.route, inclusive = false)
                    bottomBarPagerState.scrollToPage(1)
                }
            }
            composable(Screen.Transactions.route) {
                LaunchedEffect(Unit) {
                    navController.popBackStack(Screen.Dashboard.route, inclusive = false)
                    bottomBarPagerState.scrollToPage(2)
                }
            }
            composable(Screen.Menu.route) {
                LaunchedEffect(Unit) {
                    navController.popBackStack(Screen.Dashboard.route, inclusive = false)
                    bottomBarPagerState.scrollToPage(3)
                }
            }
            composable(Screen.CashFlow.route) {
                FeaturePlaceholderScreen(Screen.CashFlow, onBack = { navController.popBackStack() })
            }
            composable(Screen.Budgets.route) {
                BudgetScreen(
                    viewModel = budgetViewModel,
                    accountViewModel = accountViewModel
                )
            }
            composable(Screen.Investments.route) {
                InvestmentScreen(
                    viewModel = investmentViewModel
                )
            }
            composable(Screen.Analytics.route) {
                FeaturePlaceholderScreen(Screen.Analytics, onBack = { navController.popBackStack() })
            }
            composable(Screen.Reports.route) {
                FeaturePlaceholderScreen(Screen.Reports, onBack = { navController.popBackStack() })
            }
            composable(Screen.Notifications.route) {
                FeaturePlaceholderScreen(Screen.Notifications, onBack = { navController.popBackStack() })
            }
            composable(Screen.Settings.route) {
                MoreScreen(
                    userName = userName,
                    userEmail = userEmail,
                    isDarkMode = isDarkMode,
                    onToggleDarkMode = onToggleDarkMode,
                    onLogout = onLogout
                )
            }
        }
    }
}

@Composable
private fun FeaturePlaceholderScreen(
    screen: Screen,
    onBack: () -> Unit = {}
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = screen.icon,
                        contentDescription = screen.title,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(32.dp)
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = screen.title,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "${screen.title} module is ready for your financial workflows.",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(24.dp))
                OutlinedButton(
                    onClick = onBack,
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                        contentDescription = "Back",
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Back")
                }
            }
        }
    }
}
