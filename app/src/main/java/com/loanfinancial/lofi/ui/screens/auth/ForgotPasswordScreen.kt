package com.loanfinancial.lofi.ui.screens.auth

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.loanfinancial.lofi.ui.components.LofiButton
import com.loanfinancial.lofi.ui.components.LofiTextField
import com.loanfinancial.lofi.ui.theme.LofiTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun ForgotPasswordScreen(
    onBackClick: () -> Unit = {},
) {
    var email by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var emailError by remember { mutableStateOf<String?>(null) }
    var isSubmitted by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()

    fun attemptSubmit() {
        if (email.isBlank()) {
            emailError = "Email cannot be empty"
            return
        }
        if (!android.util.Patterns.EMAIL_ADDRESS
                .matcher(email)
                .matches()
        ) {
            emailError = "Invalid email format"
            return
        }

        isLoading = true
        scope.launch {
            delay(1500)
            isLoading = false
            isSubmitted = true
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
    ) { padding ->
        Column(
            modifier =
                Modifier
                    .padding(padding)
                    .padding(24.dp)
                    .fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.Start,
        ) {
            if (isSubmitted) {
                Text(
                    text = "Check your email",
                    style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.primary,
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "We have sent password recovery instructions to $email",
                    style = MaterialTheme.typography.bodyLarge,
                )
                Spacer(modifier = Modifier.height(32.dp))
                LofiButton(
                    text = "Back to Login",
                    onClick = onBackClick,
                )
            } else {
                Text(
                    text = "Forgot Password",
                    style = MaterialTheme.typography.displaySmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onBackground,
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Enter your email to reset your password.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )

                Spacer(modifier = Modifier.height(32.dp))

                LofiTextField(
                    value = email,
                    onValueChange = {
                        email = it
                        if (emailError != null) emailError = null
                    },
                    label = "Email Address",
                    isError = emailError != null,
                    errorMessage = emailError,
                    keyboardOptions =
                        androidx.compose.foundation.text.KeyboardOptions(
                            keyboardType = KeyboardType.Email,
                        ),
                )

                Spacer(modifier = Modifier.height(32.dp))

                LofiButton(
                    text = "Send Instructions",
                    onClick = { attemptSubmit() },
                    isLoading = isLoading,
                )

                Spacer(modifier = Modifier.height(16.dp))

                TextButton(
                    onClick = onBackClick,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text("Cancel", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ForgotPasswordPreview() {
    LofiTheme {
        ForgotPasswordScreen()
    }
}
