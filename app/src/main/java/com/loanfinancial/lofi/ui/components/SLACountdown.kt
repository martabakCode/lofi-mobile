package com.loanfinancial.lofi.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.loanfinancial.lofi.core.util.SLACalculator
import com.loanfinancial.lofi.core.util.SLACountdownState
import kotlinx.coroutines.delay

/**
 * A composable that displays a real-time SLA countdown with color-coded urgency levels.
 *
 * @param submittedAt ISO 8601 formatted datetime string when the loan was submitted
 * @param slaDurationHours The SLA duration in hours
 * @param modifier Modifier for customizing the layout
 * @param showIcon Whether to show the timer icon
 * @param compact Whether to use compact layout (smaller text, less padding)
 */
@Composable
fun SLACountdown(
    submittedAt: String?,
    slaDurationHours: Int?,
    modifier: Modifier = Modifier,
    showIcon: Boolean = true,
    compact: Boolean = false,
) {
    var countdownState by remember {
        mutableStateOf(SLACalculator.calculateTimeRemaining(submittedAt, slaDurationHours))
    }

    // Update countdown every second
    LaunchedEffect(submittedAt, slaDurationHours) {
        while (true) {
            countdownState = SLACalculator.calculateTimeRemaining(submittedAt, slaDurationHours)
            delay(1000) // Update every second
        }
    }

    // Don't show anything if there's no SLA
    if (countdownState is SLACountdownState.NoSLA) {
        return
    }

    val (backgroundColor, contentColor, timeText, labelText) =
        when (val state = countdownState) {
            is SLACountdownState.Active -> {
                val bgColor = SLACalculator.getSLABackgroundColor(state.percentageRemaining)
                val fgColor = SLACalculator.getSLAColor(state.percentageRemaining)
                val label =
                    when {
                        state.isUrgent -> "SLA Expiring Soon"
                        state.isWarning -> "SLA Warning"
                        else -> "SLA Time Remaining"
                    }
                Quadruple(bgColor, fgColor, state.formattedTime, label)
            }
            is SLACountdownState.Expired -> {
                Quadruple(
                    Color(0xFFFFEBEE),
                    Color(0xFFF44336),
                    "Expired",
                    "SLA Deadline Passed",
                )
            }
            else -> return
        }

    val animatedBackgroundColor by animateColorAsState(
        targetValue = backgroundColor,
        label = "SLA Background Color",
    )

    val animatedContentColor by animateColorAsState(
        targetValue = contentColor,
        label = "SLA Content Color",
    )

    Card(
        modifier = modifier.fillMaxWidth(),
        colors =
            CardDefaults.cardColors(
                containerColor = animatedBackgroundColor,
            ),
        shape = MaterialTheme.shapes.medium,
    ) {
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(if (compact) 12.dp else 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                if (showIcon) {
                    val icon =
                        when (countdownState) {
                            is SLACountdownState.Active -> {
                                val state = countdownState as SLACountdownState.Active
                                when {
                                    state.isUrgent -> Icons.Filled.Warning
                                    state.isWarning -> Icons.Filled.Schedule
                                    else -> Icons.Filled.Timer
                                }
                            }
                            is SLACountdownState.Expired -> Icons.Filled.Error
                            else -> Icons.Filled.Timer
                        }
                    Icon(
                        imageVector = icon,
                        contentDescription = "SLA Timer",
                        tint = animatedContentColor,
                        modifier = Modifier.size(if (compact) 20.dp else 24.dp),
                    )
                }

                Column {
                    Text(
                        text = labelText,
                        style =
                            if (compact) {
                                MaterialTheme.typography.labelSmall
                            } else {
                                MaterialTheme.typography.labelMedium
                            },
                        color = animatedContentColor.copy(alpha = 0.8f),
                    )
                    Text(
                        text = timeText,
                        style =
                            if (compact) {
                                MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                            } else {
                                MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            },
                        color = animatedContentColor,
                    )
                }
            }

            // Progress indicator for active countdowns
            if (countdownState is SLACountdownState.Active) {
                val state = countdownState as SLACountdownState.Active
                val progress = state.percentageRemaining.coerceIn(0f, 1f)

                CircularProgressIndicator(
                    progress = { progress },
                    modifier = Modifier.size(if (compact) 28.dp else 36.dp),
                    color = animatedContentColor,
                    trackColor = animatedContentColor.copy(alpha = 0.2f),
                    strokeWidth = if (compact) 3.dp else 4.dp,
                )
            }
        }
    }
}

/**
 * A compact version of the SLA countdown for use in lists or cards
 */
@Composable
fun SLACountdownCompact(
    submittedAt: String?,
    slaDurationHours: Int?,
    modifier: Modifier = Modifier,
) {
    SLACountdown(
        submittedAt = submittedAt,
        slaDurationHours = slaDurationHours,
        modifier = modifier,
        showIcon = true,
        compact = true,
    )
}

/**
 * A simple text-only version of the SLA countdown
 */
@Composable
fun SLACountdownText(
    submittedAt: String?,
    slaDurationHours: Int?,
    modifier: Modifier = Modifier,
) {
    var countdownState by remember {
        mutableStateOf(SLACalculator.calculateTimeRemaining(submittedAt, slaDurationHours))
    }

    LaunchedEffect(submittedAt, slaDurationHours) {
        while (true) {
            countdownState = SLACalculator.calculateTimeRemaining(submittedAt, slaDurationHours)
            delay(1000)
        }
    }

    if (countdownState is SLACountdownState.NoSLA) {
        return
    }

    val (text, color) =
        when (val state = countdownState) {
            is SLACountdownState.Active -> {
                Pair(state.formattedTime, SLACalculator.getSLAColor(state.percentageRemaining))
            }
            is SLACountdownState.Expired -> {
                Pair("Expired", Color(0xFFF44336))
            }
            else -> return
        }

    val animatedColor by animateColorAsState(
        targetValue = color,
        label = "SLA Text Color",
    )

    Text(
        text = text,
        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
        color = animatedColor,
        modifier = modifier,
    )
}

// Helper data class for returning multiple values
private data class Quadruple<A, B, C, D>(
    val first: A,
    val second: B,
    val third: C,
    val fourth: D,
)
