package com.loanfinancial.lofi.ui.features.profile

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.loanfinancial.lofi.ui.components.LofiButton
import com.loanfinancial.lofi.ui.components.LofiTextField
import com.loanfinancial.lofi.ui.components.LofiTopBar

@Composable
fun ChangePasswordScreen(
    navigateUp: () -> Unit,
    viewModel: ChangePasswordViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState.isSuccess) {
        if (uiState.isSuccess) {
            navigateUp()
        }
    }

    Scaffold(
        topBar = {
            LofiTopBar(
                title = "Change Password",
                canNavigateBack = true,
                navigateUp = navigateUp,
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
    ) { padding ->
        Column(
            modifier =
                Modifier
                    .padding(padding)
                    .padding(24.dp)
                    .fillMaxSize(),
        ) {
            Text(
                "Update your password",
                style =
                    MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                    ),
            )
            Text(
                "Ensure your account is secure by using a strong password.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
            )

            Spacer(modifier = Modifier.height(32.dp))

            LofiTextField(
                value = uiState.oldPassword,
                onValueChange = { viewModel.onOldPasswordChange(it) },
                label = "Current Password",
                visualTransformation = PasswordVisualTransformation(),
                isError = uiState.validationErrors["oldPassword"] != null,
                errorMessage = uiState.validationErrors["oldPassword"],
            )

            Spacer(modifier = Modifier.height(20.dp))

            LofiTextField(
                value = uiState.newPassword,
                onValueChange = { viewModel.onNewPasswordChange(it) },
                label = "New Password",
                visualTransformation = PasswordVisualTransformation(),
                isError = uiState.validationErrors["newPassword"] != null,
                errorMessage = uiState.validationErrors["newPassword"],
            )

            Spacer(modifier = Modifier.height(20.dp))

            LofiTextField(
                value = uiState.confirmPassword,
                onValueChange = { viewModel.onConfirmPasswordChange(it) },
                label = "Confirm New Password",
                visualTransformation = PasswordVisualTransformation(),
                isError = uiState.validationErrors["confirmPassword"] != null,
                errorMessage = uiState.validationErrors["confirmPassword"],
            )

            if (uiState.error != null) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = uiState.error!!,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            LofiButton(
                text = "Save Password",
                onClick = { viewModel.submit() },
                isLoading = uiState.isLoading,
                modifier = Modifier.fillMaxWidth().height(56.dp),
            )

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}
