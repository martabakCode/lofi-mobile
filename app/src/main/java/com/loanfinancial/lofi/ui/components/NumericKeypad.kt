package com.loanfinancial.lofi.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun PinDot(isFilled: Boolean) {
    Box(
        modifier =
            Modifier
                .size(16.dp)
                .clip(CircleShape)
                .background(
                    if (isFilled) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        Color.Transparent
                    },
                ).border(
                    width = 2.dp,
                    color = if (isFilled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                    shape = CircleShape,
                ),
    )
}

@Composable
fun NumericKeypad(
    onInput: (String) -> Unit,
    onDeleteClick: () -> Unit,
) {
    val numbers =
        listOf(
            listOf("1", "2", "3"),
            listOf("4", "5", "6"),
            listOf("7", "8", "9"),
            listOf("", "0", "Delete"),
        )

    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth(),
    ) {
        numbers.forEach { row ->
            Row(
                horizontalArrangement = Arrangement.spacedBy(24.dp),
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                row.forEach { item ->
                    Box(
                        modifier =
                            Modifier
                                .weight(1f)
                                .aspectRatio(2f)
                                .clip(RoundedCornerShape(12.dp))
                                .clickable(enabled = item.isNotEmpty()) {
                                    if (item == "Delete") onDeleteClick() else onInput(item)
                                },
                        contentAlignment = Alignment.Center,
                    ) {
                        if (item.isNotEmpty()) {
                            Text(
                                text = item,
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Medium,
                                color = if (item == "Delete") MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface,
                            )
                        }
                    }
                }
            }
        }
    }
}
