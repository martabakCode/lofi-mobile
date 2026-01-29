package com.loanfinancial.lofi.ui.features.notification

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.loanfinancial.lofi.ui.components.LofiTopBar

@Composable
fun NotificationDetailScreen(
    notificationId: String,
    navigateUp: () -> Unit,
    viewModel: NotificationViewModel = hiltViewModel(),
) {
    val item = viewModel.getNotification(notificationId)

    Scaffold(
        topBar = {
            LofiTopBar(
                title = "Detail",
                canNavigateBack = true,
                navigateUp = navigateUp,
            )
        },
        containerColor = Color.White,
    ) { padding ->
        Column(
            modifier =
                Modifier
                    .padding(padding)
                    .fillMaxSize(),
        ) {
            item?.let {
                Column(
                    modifier =
                        Modifier
                            .padding(24.dp)
                            .fillMaxWidth(),
                ) {
                    Surface(
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                        shape = MaterialTheme.shapes.small,
                    ) {
                        Text(
                            text = it.type.name,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary,
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = it.title,
                        style =
                            MaterialTheme.typography.headlineMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                            ),
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = it.createdAt,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                    )

                    Spacer(modifier = Modifier.height(32.dp))

                    Divider(color = MaterialTheme.colorScheme.outlineVariant, thickness = 0.5.dp)

                    Spacer(modifier = Modifier.height(32.dp))

                    Text(
                        text = it.body,
                        style =
                            MaterialTheme.typography.bodyLarge.copy(
                                lineHeight = 28.sp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                            ),
                    )
                }
            } ?: Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Notification not found", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}
