package com.fitcast.cuacagaya.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Checkroom
import androidx.compose.material.icons.outlined.Cloud
import androidx.compose.material.icons.outlined.DryCleaning
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.LocalLaundryService
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.rounded.Checkroom
import androidx.compose.material.icons.rounded.DryCleaning
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.LocalLaundryService
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fitcast.cuacagaya.ui.theme.CanvasLight
import com.fitcast.cuacagaya.ui.theme.ElectricCobalt
import com.fitcast.cuacagaya.ui.theme.FitCastTheme
import com.fitcast.cuacagaya.ui.theme.FitCastWordmarkTextStyle
import com.fitcast.cuacagaya.ui.theme.LaundryRed
import com.fitcast.cuacagaya.ui.theme.ObsidianDark
import com.fitcast.cuacagaya.ui.theme.PlusJakartaSans
import com.fitcast.cuacagaya.ui.theme.PureWhite
import com.fitcast.cuacagaya.ui.theme.SurfaceCardDark
import com.fitcast.cuacagaya.ui.theme.VoltLime

enum class FitCastTab(
    val route: String,
    val label: String,
    val unselectedIcon: ImageVector,
    val selectedIcon: ImageVector
) {
    HOME("home", "Beranda", Icons.Outlined.Home, Icons.Rounded.Home),
    STUDIO("studio", "Studio", Icons.Outlined.DryCleaning, Icons.Rounded.DryCleaning),
    WARDROBE("wardrobe", "Lemari", Icons.Outlined.Checkroom, Icons.Rounded.Checkroom),
    LAUNDRY("laundry", "Laundry", Icons.Outlined.LocalLaundryService, Icons.Rounded.LocalLaundryService)
}

@Composable
fun FitCastTopBar(
    cityName: String,
    temperatureCelsius: Int,
    onLocationClick: () -> Unit,
    onNotificationClick: () -> Unit,
    onProfileClick: () -> Unit,
    modifier: Modifier = Modifier,
    hasUnreadNotification: Boolean = true,
    notificationBadgeColor: Color = ElectricCobalt,
    weatherIconTint: Color = VoltLime
) {
    val pillBackground = PureWhite.copy(alpha = 0.08f)
    val pillBorder = PureWhite.copy(alpha = 0.14f)

    Surface(
        modifier = modifier.fillMaxWidth(),
        color = ObsidianDark,
        contentColor = PureWhite
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "FITCAST",
                style = FitCastWordmarkTextStyle,
                color = PureWhite,
                maxLines = 1,
                softWrap = false
            )

            Box(
                modifier = Modifier.weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Surface(
                    shape = CircleShape,
                    color = pillBackground,
                    border = BorderStroke(1.dp, pillBorder),
                    modifier = Modifier
                        .defaultMinSize(minHeight = 40.dp)
                        .clip(CircleShape)
                        .clickable(
                            role = Role.Button,
                            onClickLabel = "Pilih kota atau perbarui GPS",
                            onClick = onLocationClick
                        )
                        .semantics {
                            contentDescription = "Lokasi cuaca $cityName $temperatureCelsius derajat Celsius. Ketuk untuk memilih kota atau GPS."
                        }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Cloud,
                            contentDescription = null,
                            tint = weatherIconTint,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "$cityName • $temperatureCelsius°C",
                            fontFamily = PlusJakartaSans,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 13.sp,
                            lineHeight = 16.sp,
                            color = PureWhite,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(2.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TopBarCircleIconButton(
                    icon = Icons.Outlined.Notifications,
                    contentDescription = if (hasUnreadNotification) {
                        "Notifikasi, ada pembaruan belum dibaca"
                    } else {
                        "Notifikasi"
                    },
                    onClick = onNotificationClick,
                    showBadgeDot = hasUnreadNotification,
                    badgeDotColor = notificationBadgeColor
                )

                TopBarCircleIconButton(
                    icon = Icons.Outlined.Person,
                    contentDescription = "Profil dan Tentang FitCast",
                    onClick = onProfileClick
                )
            }
        }
    }
}

@Composable
private fun TopBarCircleIconButton(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    showBadgeDot: Boolean = false,
    badgeDotColor: Color = ElectricCobalt
) {
    // 48dp touch area wrapping the 40dp visual circle meets Android accessibility targets.
    Box(
        modifier = Modifier
            .size(48.dp)
            .clip(CircleShape)
            .clickable(role = Role.Button, onClick = onClick)
            .semantics { this.contentDescription = contentDescription },
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(SurfaceCardDark)
                .border(1.dp, PureWhite.copy(alpha = 0.14f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = PureWhite,
                modifier = Modifier.size(20.dp)
            )
            if (showBadgeDot) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .offset(x = (-7).dp, y = 7.dp)
                        .size(9.dp)
                        .clip(CircleShape)
                        .background(badgeDotColor)
                        .border(1.5.dp, ObsidianDark, CircleShape)
                )
            }
        }
    }
}

@Composable
fun FitCastFloatingBottomNav(
    selectedTab: FitCastTab,
    onTabSelected: (FitCastTab) -> Unit,
    laundryCount: Int,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(68.dp),
            shape = RoundedCornerShape(999.dp),
            color = ObsidianDark,
            shadowElevation = 14.dp,
            border = BorderStroke(1.dp, PureWhite.copy(alpha = 0.08f))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp)
                    .selectableGroup(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                FitCastTab.entries.forEach { tab ->
                    val isSelected = tab == selectedTab
                    val badgeCount = if (tab == FitCastTab.LAUNDRY) laundryCount else 0

                    FloatingNavTabItem(
                        tab = tab,
                        isSelected = isSelected,
                        badgeCount = badgeCount,
                        onClick = { onTabSelected(tab) }
                    )
                }
            }
        }
    }
}

@Composable
private fun FloatingNavTabItem(
    tab: FitCastTab,
    isSelected: Boolean,
    badgeCount: Int,
    onClick: () -> Unit
) {
    val containerColor by animateColorAsState(
        targetValue = if (isSelected) VoltLime else Color.Transparent,
        animationSpec = tween(durationMillis = 220),
        label = "navTabContainerColor"
    )
    val contentColor by animateColorAsState(
        targetValue = if (isSelected) ObsidianDark else PureWhite.copy(alpha = 0.68f),
        animationSpec = tween(durationMillis = 200),
        label = "navTabContentColor"
    )
    val horizontalPadding by animateDpAsState(
        targetValue = if (isSelected) 18.dp else 12.dp,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "navTabHorizontalPadding"
    )

    val a11yLabel = if (badgeCount > 0) {
        "${tab.label}, $badgeCount pakaian perlu dicuci"
    } else {
        tab.label
    }

    Box(
        modifier = Modifier
            .defaultMinSize(minWidth = 48.dp, minHeight = 50.dp)
            .clip(CircleShape)
            .background(containerColor)
            .selectable(
                selected = isSelected,
                onClick = onClick,
                role = Role.Tab
            )
            .semantics { contentDescription = a11yLabel }
            .animateContentSize(
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioNoBouncy,
                    stiffness = Spring.StiffnessMediumLow
                )
            )
            .padding(horizontal = horizontalPadding, vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        AnimatedContent(
            targetState = isSelected,
            transitionSpec = {
                fadeIn(animationSpec = tween(180)) togetherWith fadeOut(animationSpec = tween(120))
            },
            label = "navTabContent"
        ) { active ->
            if (active) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = tab.selectedIcon,
                        contentDescription = null,
                        tint = contentColor,
                        modifier = Modifier.size(21.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = tab.label,
                        fontFamily = PlusJakartaSans,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        lineHeight = 16.sp,
                        color = contentColor,
                        maxLines = 1,
                        softWrap = false
                    )
                    if (badgeCount > 0) {
                        Spacer(modifier = Modifier.width(8.dp))
                        LaundryCountBadge(count = badgeCount)
                    }
                }
            } else {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = tab.unselectedIcon,
                        contentDescription = null,
                        tint = contentColor,
                        modifier = Modifier.size(22.dp)
                    )
                    if (badgeCount > 0) {
                        LaundryCountBadge(
                            count = badgeCount,
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .offset(x = 8.dp, y = (-6).dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun LaundryCountBadge(
    count: Int,
    modifier: Modifier = Modifier
) {
    val label = if (count > 9) "9+" else count.toString()
    Box(
        modifier = modifier
            .defaultMinSize(minWidth = 18.dp, minHeight = 18.dp)
            .clip(CircleShape)
            .background(LaundryRed)
            .padding(horizontal = 5.dp, vertical = 1.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            fontFamily = PlusJakartaSans,
            fontWeight = FontWeight.Bold,
            fontSize = 10.sp,
            lineHeight = 12.sp,
            color = PureWhite,
            maxLines = 1,
            softWrap = false
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFF5F7F9)
@Composable
private fun FitCastChromeBerandaPreview() {
    FitCastTheme(darkTheme = false) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(CanvasLight),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            FitCastTopBar(
                cityName = "Bandung",
                temperatureCelsius = 24,
                onLocationClick = {},
                onNotificationClick = {},
                onProfileClick = {}
            )
            FitCastFloatingBottomNav(
                selectedTab = FitCastTab.HOME,
                onTabSelected = {},
                laundryCount = 2
            )
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF131816)
@Composable
private fun FitCastChromeLaundryActiveDarkPreview() {
    FitCastTheme(darkTheme = true) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(ObsidianDark),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            FitCastTopBar(
                cityName = "Bandung",
                temperatureCelsius = 24,
                onLocationClick = {},
                onNotificationClick = {},
                onProfileClick = {},
                notificationBadgeColor = LaundryRed
            )
            FitCastFloatingBottomNav(
                selectedTab = FitCastTab.LAUNDRY,
                onTabSelected = {},
                laundryCount = 2
            )
        }
    }
}
