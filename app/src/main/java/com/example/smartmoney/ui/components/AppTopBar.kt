package com.example.smartmoney.ui.components

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.smartmoney.ui.theme.LocalDarkTheme
import com.example.smartmoney.ui.theme.SmartMoneyColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppTopBar(
    title: String,
    userName: String = "User",
    profileBitmap: ImageBitmap? = null,
    isOverview: Boolean = false,
    canNavigateBack: Boolean = false,
    onBackClick: () -> Unit = {},
    onProfileClick: () -> Unit = {},
    onMenuClick: () -> Unit = {},
    onNotificationsClick: () -> Unit,
    onSettingsClick: () -> Unit,
    containerColor: Color = Color.Transparent,
    modifier: Modifier = Modifier
) {
    val isDark = LocalDarkTheme.current
    val titleColor = if (isOverview) Color(0xFF1A1A1A) else (if (isDark) SmartMoneyColors.DarkTextPrimary else SmartMoneyColors.TextPrimary)
    val borderCol = if (isOverview) Color(0xFF1A1A1A).copy(alpha = 0.35f) else (if (isDark) SmartMoneyColors.DarkBorderLine else SmartMoneyColors.BorderLine)
    val textMutedCol = if (isOverview) Color(0xFF1A1A1A).copy(alpha = 0.72f) else (if (isDark) SmartMoneyColors.DarkInactive else SmartMoneyColors.TextMuted)
    val actionIconColor = if (isOverview) Color(0xFF1A1A1A) else textMutedCol

    TopAppBar(
        title = {
            if (isOverview) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = onProfileClick
                    )
                ) {
                    UserProfileAvatar(
                        bitmap = profileBitmap,
                        userName = userName,
                        modifier = Modifier.size(38.dp),
                        fontSize = 13.sp
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Welcome back,",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Normal,
                            color = textMutedCol,
                            lineHeight = 13.sp
                        )
                        Text(
                            text = userName,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = titleColor,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            } else {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    UserProfileAvatar(
                        bitmap = profileBitmap,
                        userName = userName,
                        modifier = Modifier.size(28.dp),
                        fontSize = 11.sp
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = title,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = titleColor,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        },
        navigationIcon = {
            if (canNavigateBack) {
                IconButton(onClick = onBackClick) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                        contentDescription = "Back",
                        tint = titleColor
                    )
                }
            }
        },
        actions = {
            // Currency Indicator Badge
            Box(
                modifier = Modifier
                    .border(1.dp, borderCol, RoundedCornerShape(8.dp))
                    .padding(horizontal = 10.dp, vertical = 4.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "KES",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isOverview) Color(0xFF1A1A1A) else textMutedCol
                )
            }
            Spacer(modifier = Modifier.width(8.dp))

            // Notifications Icon
            IconButton(onClick = onNotificationsClick) {
                Icon(
                    imageVector = Icons.Outlined.Notifications,
                    contentDescription = "Notifications",
                    tint = actionIconColor
                )
            }

            // Settings Icon
            IconButton(onClick = onSettingsClick) {
                Icon(
                    imageVector = Icons.Outlined.Settings,
                    contentDescription = "Settings",
                    tint = actionIconColor
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = containerColor,
            scrolledContainerColor = containerColor,
            navigationIconContentColor = titleColor,
            titleContentColor = titleColor,
            actionIconContentColor = actionIconColor
        ),
        modifier = modifier
    )
}
