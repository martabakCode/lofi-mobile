package com.loanfinancial.lofi.core.util

import androidx.compose.ui.graphics.Color
import java.time.Duration
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

/**
 * Data class representing the state of an SLA countdown
 */
sealed class SLACountdownState {
    data class Active(
        val timeRemainingMs: Long,
        val percentageRemaining: Float,
        val formattedTime: String,
        val isUrgent: Boolean,
        val isWarning: Boolean,
    ) : SLACountdownState()

    data object Expired : SLACountdownState()

    data object NoSLA : SLACountdownState()
}

/**
 * Utility object for calculating SLA countdown times and formatting
 */
object SLACalculator {
    private val dateTimeFormatter = DateTimeFormatter.ISO_DATE_TIME

    /**
     * Calculates the remaining time for an SLA based on submission time and duration
     *
     * @param submittedAt ISO 8601 formatted datetime string when the loan was submitted
     * @param slaDurationHours The SLA duration in hours
     * @return SLACountdownState representing the current state of the countdown
     */
    fun calculateTimeRemaining(
        submittedAt: String?,
        slaDurationHours: Int?,
    ): SLACountdownState {
        if (submittedAt == null || slaDurationHours == null || slaDurationHours <= 0) {
            return SLACountdownState.NoSLA
        }

        return try {
            val submissionTime = LocalDateTime.parse(submittedAt, dateTimeFormatter)
            val deadline = submissionTime.plusHours(slaDurationHours.toLong())
            val now = LocalDateTime.now()

            if (now.isAfter(deadline)) {
                SLACountdownState.Expired
            } else {
                val remainingDuration = Duration.between(now, deadline)
                val totalDuration = Duration.between(submissionTime, deadline)
                val remainingMs = remainingDuration.toMillis()
                val totalMs = totalDuration.toMillis()

                val percentageRemaining =
                    if (totalMs > 0) {
                        remainingMs.toFloat() / totalMs.toFloat()
                    } else {
                        0f
                    }

                SLACountdownState.Active(
                    timeRemainingMs = remainingMs,
                    percentageRemaining = percentageRemaining,
                    formattedTime = formatDuration(remainingMs),
                    isUrgent = percentageRemaining < 0.1f,
                    isWarning = percentageRemaining < 0.25f,
                )
            }
        } catch (e: DateTimeParseException) {
            SLACountdownState.NoSLA
        }
    }

    /**
     * Formats a duration in milliseconds to a human-readable string
     *
     * @param millis Duration in milliseconds
     * @return Formatted string like "2d 14h 30m 15s" or "Expired"
     */
    fun formatDuration(millis: Long): String {
        if (millis <= 0) return "Expired"

        val seconds = millis / 1000
        val minutes = seconds / 60
        val hours = minutes / 60
        val days = hours / 24

        val remainingHours = hours % 24
        val remainingMinutes = minutes % 60
        val remainingSeconds = seconds % 60

        return buildString {
            if (days > 0) {
                append("${days}d ")
            }
            if (remainingHours > 0 || days > 0) {
                append("${remainingHours}h ")
            }
            if (remainingMinutes > 0 || remainingHours > 0 || days > 0) {
                append("${remainingMinutes}m ")
            }
            append("${remainingSeconds}s")
        }.trim()
    }

    /**
     * Gets the color to display based on the percentage of time remaining
     *
     * @param percentageRemaining Float between 0.0 and 1.0
     * @return Color appropriate for the urgency level
     */
    fun getSLAColor(percentageRemaining: Float): Color =
        when {
            percentageRemaining > 0.5f -> Color(0xFF4CAF50) // Green - Safe
            percentageRemaining > 0.25f -> Color(0xFFFFC107) // Yellow - Warning
            percentageRemaining > 0.1f -> Color(0xFFFF9800) // Orange - Urgent
            else -> Color(0xFFF44336) // Red - Critical/Expired
        }

    /**
     * Gets the container/background color for SLA display
     *
     * @param percentageRemaining Float between 0.0 and 1.0
     * @return Color with alpha for background
     */
    fun getSLABackgroundColor(percentageRemaining: Float): Color =
        when {
            percentageRemaining > 0.5f -> Color(0xFFE8F5E9) // Light Green
            percentageRemaining > 0.25f -> Color(0xFFFFF8E1) // Light Yellow
            percentageRemaining > 0.1f -> Color(0xFFFFF3E0) // Light Orange
            else -> Color(0xFFFFEBEE) // Light Red
        }

    /**
     * Alternative formatter that shows only the most significant time unit
     * e.g., "2 days", "14 hours", "30 minutes"
     *
     * @param millis Duration in milliseconds
     * @return Simplified formatted string
     */
    fun formatDurationSimple(millis: Long): String {
        if (millis <= 0) return "Expired"

        val seconds = millis / 1000
        val minutes = seconds / 60
        val hours = minutes / 60
        val days = hours / 24

        return when {
            days > 0 -> "$days day${if (days > 1) "s" else ""}"
            hours > 0 -> "$hours hour${if (hours > 1) "s" else ""}"
            minutes > 0 -> "$minutes minute${if (minutes > 1) "s" else ""}"
            else -> "$seconds second${if (seconds > 1) "s" else ""}"
        }
    }
}
