package com.example.energy

import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable

@Composable
fun BottomMenu(
    currentScreen: Int,
    onScreenSelected: (Int) -> Unit
) {
    NavigationBar {

        NavigationBarItem(
            selected = currentScreen == 0,
            onClick = {
                onScreenSelected(0)
            },
            icon = {
                Text("⌂")
            },
            label = {
                Text("Home")
            }
        )

        NavigationBarItem(
            selected = currentScreen == 1,
            onClick = {
                onScreenSelected(1)
            },
            icon = {
                Text("▣")
            },
            label = {
                Text("Accounts")
            }
        )

        NavigationBarItem(
            selected = currentScreen == 2,
            onClick = {
                onScreenSelected(2)
            },
            icon = {
                Text("↻")
            },
            label = {
                Text("Transactions")
            }
        )

        NavigationBarItem(
            selected = currentScreen == 3,
            onClick = {
                onScreenSelected(3)
            },
            icon = {
                Text("⋮")
            },
            label = {
                Text("More")
            }
        )
    }
}