package com.example.energy

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import kotlinx.coroutines.launch

@Composable
fun DashboardScreen(
    userName: String,
    onLogout: () -> Unit
) {

    val pagerState = rememberPagerState(
        initialPage = 0,
        pageCount = { 4 }
    )

    val scope = rememberCoroutineScope()

    Scaffold(
        bottomBar = {

            BottomMenu(
                currentScreen = pagerState.currentPage,
                onScreenSelected = { page ->

                    scope.launch {
                        pagerState.animateScrollToPage(page)
                    }
                }
            )
        }
    ) { paddingValues ->

        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) { page ->

            when (page) {

                0 -> HomeScreen(
                    userName = userName
                )

                1 -> AccountsScreen()

                2 -> TransactionsScreen()

                3 -> MoreScreen(
                    onLogout = onLogout
                )
            }
        }
    }
}