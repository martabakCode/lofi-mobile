package com.loanfinancial.lofi.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LofiDropdown(
    label: String,
    options: List<String>,
    selectedOption: String,
    onOptionSelected: (String) -> Unit,
    modifier: Modifier = Modifier,
    isSearchable: Boolean = false,
    errorMessage: String? = null,
    isError: Boolean = false,
) {
    var expanded by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }

    val filteredOptions =
        if (isSearchable && searchQuery.isNotEmpty()) {
            options.filter { it.contains(searchQuery, ignoreCase = true) }
        } else {
            options
        }

    Column(modifier = modifier) {
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded },
        ) {
            LofiTextField(
                value = if (expanded && isSearchable) searchQuery else selectedOption,
                onValueChange = { if (expanded && isSearchable) searchQuery = it },
                label = label,
                readOnly = !isSearchable || !expanded,
                isError = isError,
                errorMessage = errorMessage,
                trailingIcon = {
                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                },
                modifier =
                    Modifier
                        .menuAnchor(
                            type = if (isSearchable) MenuAnchorType.PrimaryEditable else MenuAnchorType.PrimaryNotEditable,
                            enabled = true,
                        ).fillMaxWidth(),
            )

            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = {
                    expanded = false
                    searchQuery = ""
                },
            ) {
                if (filteredOptions.isEmpty()) {
                    DropdownMenuItem(
                        text = { Text("No results found") },
                        onClick = { },
                        enabled = false,
                    )
                } else {
                    filteredOptions.forEach { option ->
                        DropdownMenuItem(
                            text = { Text(option) },
                            onClick = {
                                onOptionSelected(option)
                                expanded = false
                                searchQuery = ""
                            },
                        )
                    }
                }
            }
        }
    }
}
