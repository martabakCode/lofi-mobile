package com.loanfinancial.lofi.ui.features.auth.biometric

import androidx.compose.foundation.layout.*
import androidx.compose.ui.res.stringResource
import com.loanfinancial.lofi.R
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.loanfinancial.lofi.core.di.BiometricAuthenticatorEntryPoint
import com.loanfinancial.lofi.ui.components.LofiLogoLarge
import dagger.hilt.android.EntryPointAccessors

@Composable
fun BiometricLoginScreen(
    onBiometricSuccess: () -> Unit,
    onBiometricFailed: (String) -> Unit,
    onUsePasswordClick: () -> Unit,
) {
    val context = LocalContext.current
    val activity = context as android.app.Activity

    // Get BiometricAuthenticator from Activity component
    val biometricAuthenticator =
        remember(activity) {
            EntryPointAccessors
                .fromActivity(
                    activity,
                    BiometricAuthenticatorEntryPoint::class.java,
                ).biometricAuthenticator()
        }

    // Create ViewModel using the factory
    val viewModel: BiometricLoginViewModel =
        viewModel(
            factory = BiometricLoginViewModel.provideFactory(biometricAuthenticator, context),
        )

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
            // 🎨 Logo
            LofiLogoLarge()

            Spacer(modifier = Modifier.height(24.dp))

            // Header
            Text(
                text = stringResource(R.string.label_welcome_back),
                style =
                    MaterialTheme.typography.displaySmall.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 32.sp,
                    ),
                color = MaterialTheme.colorScheme.onBackground,
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = stringResource(R.string.biometric_login_title),
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
                            text = uiState.error ?: stringResource(R.string.error_biometric_not_available),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            textAlign = TextAlign.Center,
                        )
                    }
                }
            } else {
                // Biometric button with Logo
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
                            contentDescription = stringResource(R.string.desc_biometric_login),
                            modifier = Modifier.size(64.dp),
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = stringResource(R.string.biometric_tap_to_auth),
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
                    Text(stringResource(R.string.biometric_use_password))
                }
            } else {
                TextButton(onClick = onUsePasswordClick) {
                    Text(stringResource(R.string.biometric_use_password))
                }
            }
        }
    }
}
