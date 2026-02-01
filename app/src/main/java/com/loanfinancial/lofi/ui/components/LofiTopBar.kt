package com.loanfinancial.lofi.ui.components

import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.loanfinancial.lofi.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LofiTopBar(
    title: String,
    canNavigateBack: Boolean = false,
    navigateUp: () -> Unit = {},
    onNotificationClick: () -> Unit = {},
    showLogo: Boolean = false,
    actions: @Composable RowScope.() -> Unit = {},
) {
    CenterAlignedTopAppBar(
        title = {
            if (showLogo) {
                LofiLogoSmall()
            } else {
                Text(
                    text = title,
                    style =
                        MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 17.sp, // Apple standard
                        ),
                )
            }
        },
        navigationIcon = {
            if (canNavigateBack) {
                IconButton(onClick = navigateUp) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = stringResource(R.string.back),
                    )
                }
            } else if (showLogo) {
                // Show small logo on the left when not navigating back
                LofiLogoSmall(
                    modifier = Modifier.padding(start = 16.dp)
                )
            }
        },
        actions = {
            // Pre-pended actions from call site
            actions()

            // Notification Icon with Badge
            IconButton(onClick = onNotificationClick) {
                BadgedBox(
                    badge = {
                        Badge { Text("3") } // Mock 3 notifications
                    },
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Notifications,
                        contentDescription = stringResource(R.string.notifications),
                    )
                }
            }
        },
        colors =
            TopAppBarDefaults.centerAlignedTopAppBarColors(
                containerColor = MaterialTheme.colorScheme.background,
                titleContentColor = MaterialTheme.colorScheme.onBackground,
                navigationIconContentColor = MaterialTheme.colorScheme.primary,
                actionIconContentColor = MaterialTheme.colorScheme.primary,
            ),
    )
}
