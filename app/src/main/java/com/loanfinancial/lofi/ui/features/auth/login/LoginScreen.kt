package com.loanfinancial.lofi.ui.features.auth.login

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialException
import androidx.credentials.exceptions.GetCredentialCancellationException
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.loanfinancial.lofi.R
import com.loanfinancial.lofi.ui.components.LofiButton
import com.loanfinancial.lofi.ui.components.LofiLogoMedium
import com.loanfinancial.lofi.ui.components.LofiTextField
import com.loanfinancial.lofi.ui.components.SocialAuthButton
import com.loanfinancial.lofi.ui.theme.LofiTheme
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(
    onLoginClick: (profileCompleted: Boolean, pinSet: Boolean, isGoogleLogin: Boolean) -> Unit = { _, _, _ -> },
    onSkipLoginClick: () -> Unit = {},
    onRegisterClick: () -> Unit = {},
    onForgotPasswordClick: () -> Unit = {},
    onBiometricLoginClick: () -> Unit = {},
    viewModel: LoginViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    // Credential Manager
    val credentialManager = remember { CredentialManager.create(context) }
    
    fun handleGoogleSignIn() {
        val googleIdOption = GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(false)
            .setServerClientId(context.getString(R.string.default_web_client_id))
            .build()

        val request = GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build()

        coroutineScope.launch {
            try {
                val result = credentialManager.getCredential(
                    request = request,
                    context = context
                )
                val credential = result.credential
                if (credential is GoogleIdTokenCredential) {
                    viewModel.onGoogleLogin(credential.idToken)
                } else if (credential is androidx.credentials.CustomCredential && credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                    try {
                        val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                        viewModel.onGoogleLogin(googleIdTokenCredential.idToken)
                    } catch (e: Exception) {
                        snackbarHostState.showSnackbar("Failed to parse Google Credential: ${e.message}")
                    }
                } else {
                    snackbarHostState.showSnackbar(context.getString(R.string.error_google_signin_no_token) + " (Type: ${credential.javaClass.simpleName})")
                }
            } catch (e: GetCredentialException) {
                val errorMessage = when (e) {
                    is GetCredentialCancellationException -> context.getString(R.string.error_google_signin_cancelled)
                    else -> context.getString(R.string.error_google_signin_generic, 0, e.message)
                }
                snackbarHostState.showSnackbar(errorMessage)
            }
        }
    }

    LaunchedEffect(uiState.isLoginSuccessful) {
        if (uiState.isLoginSuccessful) {
            viewModel.onLoginSuccessHandled()
            // Check if biometric is already enabled
            if (uiState.isBiometricEnabled) {
                onBiometricLoginClick()
            } else {
                // Show enable biometric dialog
                viewModel.shouldShowEnableBiometricDialog()
            }
        }
    }

    LaunchedEffect(uiState.loginError) {
        uiState.loginError?.let { error ->
            val actionDismiss = context.getString(R.string.action_dismiss)
            snackbarHostState.showSnackbar(
                message = error,
                actionLabel = actionDismiss,
                duration = SnackbarDuration.Short,
            )
            viewModel.onErrorShown()
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
    ) { padding ->
        // Enable Biometric Dialog
        if (uiState.showEnableBiometricDialog) {
            EnableBiometricDialog(
                onDismiss = { viewModel.onSkipBiometric() },
                onEnable = {
                    viewModel.onEnableBiometric()
                    onLoginClick(uiState.profileCompleted, uiState.pinSet, uiState.isGoogleLogin)
                },
                onSkip = {
                    viewModel.onSkipBiometric()
                    onLoginClick(uiState.profileCompleted, uiState.pinSet, uiState.isGoogleLogin)
                },
            )
        }

        Column(
            modifier =
                Modifier
                    .padding(padding)
                    .padding(24.dp)
                    .fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.Start,
        ) {
            // 🎨 Logo
            LofiLogoMedium(
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )

            Spacer(modifier = Modifier.height(24.dp))

            // 🏷️ Header
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
                text = stringResource(R.string.login_subtitle),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Spacer(modifier = Modifier.height(40.dp))

            // 📧 Email
            LofiTextField(
                value = uiState.email,
                onValueChange = { viewModel.onEmailChange(it) },
                label = stringResource(R.string.label_email),
                keyboardOptions =
                    androidx.compose.foundation.text.KeyboardOptions(
                        keyboardType = KeyboardType.Email,
                    ),
                isError = uiState.emailError != null,
                errorMessage = uiState.emailError,
            )

            Spacer(modifier = Modifier.height(20.dp))

            // 🔒 Password
            LofiTextField(
                value = uiState.password,
                onValueChange = { viewModel.onPasswordChange(it) },
                label = stringResource(R.string.label_password),
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions =
                    androidx.compose.foundation.text.KeyboardOptions(
                        keyboardType = KeyboardType.Password,
                    ),
                isError = uiState.passwordError != null,
                errorMessage = uiState.passwordError,
            )

            // 🔑 Forgot Password
            Box(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp),
                contentAlignment = Alignment.CenterEnd,
            ) {
                Text(
                    text = stringResource(R.string.label_forgot_password),
                    style =
                        MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                        ),
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.clickable { onForgotPasswordClick() },
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // 🚀 Login Button
            LofiButton(
                text = stringResource(R.string.label_sign_in),
                onClick = { viewModel.onLoginClick() },
                isLoading = uiState.isLoading,
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Or Divider
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
            ) {
                HorizontalDivider(modifier = Modifier.weight(1f))
                Text(
                    text = stringResource(R.string.or_sign_in_with),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                HorizontalDivider(modifier = Modifier.weight(1f))
            }

            // Only Google Sign-In for now (Facebook requires Play Store verification)
            SocialAuthButton(
                text = stringResource(R.string.label_sign_in_google),
                onClick = { handleGoogleSignIn() },
                modifier = Modifier.fillMaxWidth(),
            )

            // Biometric Login Button (shown when biometric is enabled)
            if (uiState.isBiometricEnabled) {
                Spacer(modifier = Modifier.height(12.dp))
                SocialAuthButton(
                    text = stringResource(R.string.biometric_login_title),
                    onClick = onBiometricLoginClick,
                    modifier = Modifier.fillMaxWidth(),
                )
            }

            // TODO: Enable Facebook login after app is published to Play Store
            // SocialAuthButton(
            //     text = "Facebook",
            //     onClick = { /* Facebook Login */ },
            //     modifier = Modifier.weight(1f)
            // )

            Spacer(modifier = Modifier.height(24.dp))

            // 📝 Register Link
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
            ) {
                Text(
                    text = stringResource(R.string.no_account),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    text = stringResource(R.string.label_register),
                    style =
                        MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Bold,
                        ),
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.clickable { onRegisterClick() },
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // ⏩ Skip Login
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center,
        ) {
            TextButton(onClick = onSkipLoginClick) {
                Text(
                    text = stringResource(R.string.skip_login),
                    style =
                        MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Medium,
                        ),
                    color = MaterialTheme.colorScheme.secondary,
                )
            }
        }
    }
}

@Composable
fun EnableBiometricDialog(
    onDismiss: () -> Unit,
    onEnable: () -> Unit,
    onSkip: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(stringResource(R.string.enable_biometric_title))
        },
        text = {
            Text(stringResource(R.string.enable_biometric_desc))
        },
        confirmButton = {
            TextButton(onClick = onEnable) {
                Text(stringResource(R.string.btn_enable))
            }
        },
        dismissButton = {
            TextButton(onClick = onSkip) {
                Text(stringResource(R.string.btn_maybe_later))
            }
        },
    )
}

@Preview(showBackground = true)
@Composable
fun LoginScreenPreview() {
    LofiTheme {
        LoginScreen()
    }
}
