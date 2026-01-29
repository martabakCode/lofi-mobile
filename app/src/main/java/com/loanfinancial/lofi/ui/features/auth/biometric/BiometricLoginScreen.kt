package com.loanfinancial.lofi.ui.features.auth.biometric

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun BiometricLoginScreen(
    onBiometricSuccess: () -> Unit,
    onBiometricFailed: (String) -> Unit,
    onUsePasswordClick: () -> Unit,
    viewModel: BiometricLoginViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    // Handle biometric success
    LaunchedEffect(uiState.biometricSuccess) {
        if (uiState.biometricSuccess) {
            viewModel.onBiometricSuccessHandled()
            onBiometricSuccess()
        }
    }

    // Handle biometric errors
    LaunchedEffect(uiState.error) {
        uiState.error?.let { error ->
            onBiometricFailed(error)
        }
    }

    // Auto-trigger biometric on screen load
    LaunchedEffect(Unit) {
        viewModel.authenticate()
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
    ) { padding ->
        Column(
            modifier =
                Modifier
                    .padding(padding)
                    .padding(24.dp)
                    .fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // Header
            Text(
                text = "Welcome Back",
                style =
                    MaterialTheme.typography.displaySmall.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 32.sp,
                    ),
                color = MaterialTheme.colorScheme.onBackground,
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Login with Biometric",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )

            Spacer(modifier = Modifier.height(48.dp))

            // Biometric Icon Button
            if (!uiState.isBiometricAvailable) {
                // Show error state
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors =
                        CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer,
                        ),
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Icon(
                            imageVector = Icons.Default.Fingerprint,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.error,
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = uiState.error ?: "Biometric not available",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            textAlign = TextAlign.Center,
                        )
                    }
                }
            } else {
                // Biometric button
                FilledIconButton(
                    onClick = { viewModel.authenticate() },
                    modifier = Modifier.size(100.dp),
                    enabled = !uiState.isLoading,
                ) {
                    if (uiState.isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(48.dp),
                            color = MaterialTheme.colorScheme.onPrimary,
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Fingerprint,
                            contentDescription = "Biometric Login",
                            modifier = Modifier.size(64.dp),
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Tap to authenticate",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            Spacer(modifier = Modifier.height(48.dp))

            // Use Password Button
            if (!uiState.isBiometricAvailable) {
                Button(
                    onClick = onUsePasswordClick,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text("Use Password Instead")
                }
            } else {
                TextButton(onClick = onUsePasswordClick) {
                    Text("Use Password Instead")
                }
            }
        }
    }
}
