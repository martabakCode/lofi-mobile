package com.loanfinancial.lofi.ui.features.dashboard

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.Calculate
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.List
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.loanfinancial.lofi.R
import com.loanfinancial.lofi.ui.components.LofiTopBar
import com.loanfinancial.lofi.ui.features.home.HomeScreen
import com.loanfinancial.lofi.ui.features.loan.LoanHistoryScreen
import com.loanfinancial.lofi.ui.features.profile.ProfileScreen
import com.loanfinancial.lofi.ui.features.simulation.LoanSimulationScreen
import com.loanfinancial.lofi.ui.theme.LofiTheme

@Composable
fun DashboardScreen(
    isGuest: Boolean = false,
    onNavigateToLogin: () -> Unit = {},
    onNavigateToApplyLoan: () -> Unit = {},
    onNavigateToLoanDetail: (String) -> Unit = {},
    onNavigateToNotifications: () -> Unit = {},
    onNavigateToEditProfile: () -> Unit = {},
    onNavigateToChangePassword: () -> Unit = {},
    onNavigateToProfileDetail: () -> Unit = {},
) {
    var selectedTab by remember { mutableStateOf(0) }

    // Determine title based on tab
    val title =
        when (selectedTab) {
            0 -> stringResource(R.string.home_title)
            1 -> stringResource(R.string.simulation_title)
            2 -> stringResource(R.string.history_title)
            3 -> stringResource(R.string.profile_title)
            else -> stringResource(R.string.app_name)
        }

    val snackbarHostState = remember { SnackbarHostState() }

    Scaffold(
        topBar = {
            LofiTopBar(
                title = title,
                onNotificationClick = onNavigateToNotifications,
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f), // Translucent effect
                tonalElevation = 0.dp, // Flat Apple style
            ) {
                // Home
                NavigationBarItem(
                    icon = { Icon(if (selectedTab == 0) Icons.Filled.Home else Icons.Outlined.Home, null) },
                    label = { Text(stringResource(R.string.home_title), fontSize = 10.sp, fontWeight = if (selectedTab == 0) FontWeight.SemiBold else FontWeight.Normal) },
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    colors =
                        NavigationBarItemDefaults.colors(
                            indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                        ),
                )

                // Simulation
                NavigationBarItem(
                    icon = { Icon(if (selectedTab == 1) Icons.Filled.Calculate else Icons.Outlined.Calculate, null) },
                    label = { Text(stringResource(R.string.simulation_title), fontSize = 10.sp, fontWeight = if (selectedTab == 1) FontWeight.SemiBold else FontWeight.Normal) },
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    colors =
                        NavigationBarItemDefaults.colors(
                            indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                        ),
                )

                // History
                NavigationBarItem(
                    icon = { Icon(if (selectedTab == 2) Icons.Filled.List else Icons.Outlined.List, null) },
                    label = { Text(stringResource(R.string.history_title), fontSize = 10.sp, fontWeight = if (selectedTab == 2) FontWeight.SemiBold else FontWeight.Normal) },
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    colors =
                        NavigationBarItemDefaults.colors(
                            indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                        ),
                )

                // Profile
                NavigationBarItem(
                    icon = { Icon(if (selectedTab == 3) Icons.Filled.Person else Icons.Outlined.Person, null) },
                    label = { Text(stringResource(R.string.profile_title), fontSize = 10.sp, fontWeight = if (selectedTab == 3) FontWeight.SemiBold else FontWeight.Normal) },
                    selected = selectedTab == 3,
                    onClick = { selectedTab = 3 },
                    colors =
                        NavigationBarItemDefaults.colors(
                            indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                        ),
                )
            }
        },
    ) { padding ->
        Box(modifier = Modifier.padding(padding)) {
            when (selectedTab) {
                0 ->
                    HomeScreen(
                        onApplyLoanClick = onNavigateToApplyLoan,
                        onMyLoansClick = { selectedTab = 2 }, // Switch to History tab
                        onCompleteProfileClick = { selectedTab = 3 }, // Switch to Profile
                    )
                1 -> LoanSimulationScreen()
                2 ->
                    LoanHistoryScreen(
                        isGuest = isGuest,
                        onLoanClick = onNavigateToLoanDetail,
                    )
                3 ->
                    ProfileScreen(
                        isGuest = isGuest,
                        onLogoutClick = onNavigateToLogin,
                        onEditProfileClick = onNavigateToEditProfile,
                        onProfileDetailClick = onNavigateToProfileDetail,
                        onChangePasswordClick = onNavigateToChangePassword,
                    )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DashboardPreview() {
    LofiTheme {
        DashboardScreen()
    }
}
