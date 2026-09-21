package com.example.smartmoney.ui.components

import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.smartmoney.ui.navigation.Screen
import com.example.smartmoney.ui.theme.LocalDarkTheme
import com.example.smartmoney.ui.theme.SmartMoneyColors

@Composable
fun AppBottomNavigationBar(
    currentRoute: String,
    onNavigate: (Screen) -> Unit,
    modifier: Modifier = Modifier
) {
    val isDark = LocalDarkTheme.current
    val navContainerColor = if (isDark) SmartMoneyColors.DarkBackground else Color.White
    val selectedCol = if (isDark) SmartMoneyColors.DarkActiveCyan else SmartMoneyColors.AzureDark
    val indicatorCol = if (isDark) SmartMoneyColors.DarkSurface else SmartMoneyColors.AzureLight
    val unselectedCol = if (isDark) SmartMoneyColors.DarkInactive else SmartMoneyColors.TextMuted

    NavigationBar(
        containerColor = navContainerColor,
        tonalElevation = 6.dp,
        modifier = modifier
    ) {
        Screen.bottomBarScreens.forEach { screen ->
            val isSelected = currentRoute == screen.route

            NavigationBarItem(
                selected = isSelected,
                onClick = { onNavigate(screen) },
                icon = {
                    Icon(
                        imageVector = screen.icon,
                        contentDescription = screen.title
                    )
                },
                label = {
                    Text(
                        text = screen.title,
                        fontSize = 10.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = selectedCol,
                    selectedTextColor = selectedCol,
                    indicatorColor = indicatorCol,
                    unselectedIconColor = unselectedCol,
                    unselectedTextColor = unselectedCol
                )
            )
        }
    }
}
