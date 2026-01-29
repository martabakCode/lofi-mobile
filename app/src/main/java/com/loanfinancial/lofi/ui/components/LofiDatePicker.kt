package com.loanfinancial.lofi.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LofiDatePicker(
    label: String,
    selectedDate: String,
    onDateSelected: (String) -> Unit,
    modifier: Modifier = Modifier,
    isError: Boolean = false,
    errorMessage: String? = null,
) {
    var showDatePicker by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState()

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let {
                        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                        onDateSelected(sdf.format(Date(it)))
                    }
                    showDatePicker = false
                }) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancel")
                }
            },
        ) {
            DatePicker(state = datePickerState)
        }
    }

    Box(modifier = modifier) {
        LofiTextField(
            value = selectedDate,
            onValueChange = { },
            label = label,
            readOnly = true,
            isError = isError,
            errorMessage = errorMessage,
            trailingIcon = {
                Icon(
                    Icons.Default.DateRange,
                    contentDescription = "Select Date",
                    modifier = Modifier.clickable { showDatePicker = true },
                )
            },
            modifier =
                Modifier
                    .fillMaxWidth()
                    .clickable { showDatePicker = true },
        )
    }
}
