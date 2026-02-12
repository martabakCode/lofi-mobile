package com.loanfinancial.lofi.ui.features.auth.google

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.loanfinancial.lofi.ui.components.NumericKeypad
import com.loanfinancial.lofi.ui.components.PinDot

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SetGooglePinScreen(
    onBackClick: () -> Unit,
    onSuccess: () -> Unit,
    onNavigateToChangePin: () -> Unit = {},
    viewModel: SetGooglePinViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(uiState.isSuccess) {
        if (uiState.isSuccess) {
            onSuccess()
        }
    }

    // Handle case when PIN is already set on server
    if (uiState.shouldNavigateToChangePin) {
        AlertDialog(
            onDismissRequest = { viewModel.onNavigateToChangePinHandled() },
            title = { Text("PIN Already Set") },
            text = { Text("You already have a PIN set. Please use the Change PIN feature instead.") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.onNavigateToChangePinHandled()
                    onNavigateToChangePin()
                }) {
                    Text("Go to Change PIN")
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.onNavigateToChangePinHandled() }) {
                    Text("Stay Here")
                }
            },
        )
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(text = "Set Security PIN") },
                navigationIcon = {
                    IconButton(onClick = {
                        if (uiState.step == 1) viewModel.onBackClick() else onBackClick()
                    }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
            )
        },
    ) { paddingValues ->
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center,
            ) {
                Text(
                    text = if (uiState.step == 0) "Create a 6-digit PIN" else "Confirm your PIN",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                )

                Spacer(modifier = Modifier.height(32.dp))

                // Pin Display
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    val currentPinLength = if (uiState.step == 0) uiState.pin.length else uiState.confirmPin.length
                    repeat(6) { index ->
                        PinDot(isFilled = index < currentPinLength)
                    }
                }

                if (uiState.error != null) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = uiState.error!!,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center,
                    )
                }
            }

            NumericKeypad(
                onInput = { key -> viewModel.updatePin((if (uiState.step == 0) uiState.pin else uiState.confirmPin) + key) },
                onDeleteClick = {
                    val currentPin = if (uiState.step == 0) uiState.pin else uiState.confirmPin
                    if (currentPin.isNotEmpty()) {
                        viewModel.updatePin(currentPin.dropLast(1))
                    }
                },
            )
        }
    }

    if (uiState.isLoading) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            CircularProgressIndicator()
        }
    }
}
