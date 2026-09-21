package com.example.smartmoney.ui.components

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.only
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScaffoldDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.foundation.pager.PagerState
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.smartmoney.data.local.UserProfileManager
import com.example.smartmoney.ui.navigation.Screen
import com.example.smartmoney.ui.theme.LocalDarkTheme
import com.example.smartmoney.ui.theme.SmartMoneyColors
import kotlinx.coroutines.launch

@Composable
fun MainResponsiveShell(
    navController: NavHostController = rememberNavController(),
    userName: String = "User",
    pagerState: PagerState? = null,
    onSignOut: () -> Unit = {},
    content: @Composable (PaddingValues) -> Unit
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val navRoute = navBackStackEntry?.destination?.route ?: Screen.Dashboard.route
    val previousRoute = navController.previousBackStackEntry?.destination?.route
    val profileBitmap by UserProfileManager.profileBitmap.collectAsState()

    val bottomBarScreens = remember { Screen.bottomBarScreens }
    val bottomBarRoutes = remember { bottomBarScreens.map { it.route }.toSet() }

    // When at the root pager destination, active route tracks the pager's current page
    val activeBottomScreen = if (pagerState != null && pagerState.currentPage in bottomBarScreens.indices) {
        bottomBarScreens[pagerState.currentPage]
    } else {
        Screen.Dashboard
    }

    val currentRoute = if (navRoute == Screen.Dashboard.route) {
        activeBottomScreen.route
    } else {
        navRoute
    }

    val isBottomBarScreen = currentRoute in bottomBarRoutes
    val isOverview = currentRoute == Screen.Dashboard.route
    val canNavigateBack = !isBottomBarScreen || previousRoute == Screen.Menu.route

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val isDark = LocalDarkTheme.current
    val drawerBg = if (isDark) SmartMoneyColors.DeepNavy else SmartMoneyColors.AzurePrimary

    fun navigateTo(targetRoute: String) {
        if (targetRoute in bottomBarRoutes) {
            val targetIndex = bottomBarScreens.indexOfFirst { it.route == targetRoute }
            if (targetIndex >= 0) {
                if (navRoute != Screen.Dashboard.route) {
                    navController.popBackStack(Screen.Dashboard.route, inclusive = false)
                }
                if (pagerState != null) {
                    scope.launch {
                        pagerState.animateScrollToPage(targetIndex)
                    }
                }
            }
        } else {
            if (currentRoute != targetRoute) {
                navController.navigate(targetRoute) {
                    launchSingleTop = true
                    restoreState = true
                }
            }
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(drawerContainerColor = drawerBg) {
                AppSidebar(
                    currentRoute = currentRoute,
                    onNavigate = { screen ->
                        scope.launch { drawerState.close() }
                        navigateTo(screen.route)
                    },
                    onSignOut = onSignOut
                )
            }
        }
    ) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = MaterialTheme.colorScheme.background,
            contentWindowInsets = if (isBottomBarScreen) {
                ScaffoldDefaults.contentWindowInsets.only(WindowInsetsSides.Bottom + WindowInsetsSides.Horizontal)
            } else {
                ScaffoldDefaults.contentWindowInsets
            },
            topBar = {
                if (!isBottomBarScreen) {
                    AppTopBar(
                        title = Screen.allScreens.find { it.route == currentRoute }?.title ?: "Overview",
                        userName = userName,
                        profileBitmap = profileBitmap,
                        isOverview = false,
                        canNavigateBack = canNavigateBack,
                        onBackClick = { navController.popBackStack() },
                        onProfileClick = { navigateTo(Screen.Settings.route) },
                        onNotificationsClick = { navigateTo(Screen.Notifications.route) },
                        onSettingsClick = { navigateTo(Screen.Settings.route) }
                    )
                }
            },
            bottomBar = {
                AppBottomNavigationBar(
                    currentRoute = currentRoute,
                    onNavigate = { screen ->
                        navigateTo(screen.route)
                    }
                )
            }
        ) { innerPadding ->
            content(innerPadding)
        }
    }
}
