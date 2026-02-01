package com.loanfinancial.lofi.ui.features.auth.register

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.facebook.CallbackManager
import com.facebook.login.LoginManager
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.loanfinancial.lofi.R
import com.loanfinancial.lofi.ui.components.LofiButton
import com.loanfinancial.lofi.ui.components.LofiLogoMedium
import com.loanfinancial.lofi.ui.components.LofiTextField
import com.loanfinancial.lofi.ui.components.SocialAuthButton
import com.loanfinancial.lofi.ui.theme.LofiTheme
import kotlinx.coroutines.launch

@Composable
fun RegisterScreen(
    onRegisterSuccess: () -> Unit = {},
    onLoginClick: () -> Unit = {},
    viewModel: RegisterViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    // Google Sign In
    val googleLauncher =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.StartActivityForResult(),
        ) { result ->
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            try {
                val account = task.getResult(ApiException::class.java)
                val idToken = account.idToken
                if (idToken != null) {
                    viewModel.onGoogleRegister(idToken)
                } else {
                    coroutineScope.launch {
                        snackbarHostState.showSnackbar("Google Sign Up Failed: No ID token received. Please ensure your app is properly configured in Firebase Console with SHA-1 fingerprint.")
                    }
                }
            } catch (e: ApiException) {
                coroutineScope.launch {
                    val errorMessage =
                        when (e.statusCode) {
                            12500 -> "Google Sign Up failed: Please check SHA-1 configuration in Firebase Console"
                            12501 -> "Google Sign Up cancelled"
                            12502 -> "Google Sign Up failed: Network error"
                            else -> "Google Sign Up Failed: ${e.statusCode} - ${e.message}"
                        }
                    snackbarHostState.showSnackbar(errorMessage)
                }
            }
        }

    // Facebook Sign In
    val callbackManager = remember { CallbackManager.Factory.create() }
    val fbLauncher =
        rememberLauncherForActivityResult(
            contract = LoginManager.getInstance().createLogInActivityResultContract(callbackManager, null),
        ) { result ->
            // ActivityResult -> CallbackManager.ActivityResult
        }

    val googleSignInOptions =
        remember {
            GoogleSignInOptions
                .Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(context.getString(R.string.default_web_client_id))
                .requestEmail()
                .build()
        }

    var fullName by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phoneNumber by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    // Validation States
    var nameError by remember { mutableStateOf<String?>(null) }
    var usernameError by remember { mutableStateOf<String?>(null) }
    var emailError by remember { mutableStateOf<String?>(null) }
    var phoneError by remember { mutableStateOf<String?>(null) }
    var passwordError by remember { mutableStateOf<String?>(null) }
    var confirmPasswordError by remember { mutableStateOf<String?>(null) }

    val scrollState = rememberScrollState()

    // Handle UI States
    LaunchedEffect(uiState) {
        when (uiState) {
            is RegisterUiState.Success -> {
                onRegisterSuccess()
                viewModel.resetState()
            }
            is RegisterUiState.Error -> {
                snackbarHostState.showSnackbar(
                    message = (uiState as RegisterUiState.Error).message,
                    duration = SnackbarDuration.Short,
                )
                viewModel.resetState()
            }
            else -> {}
        }
    }

    fun validate(): Boolean {
        var isValid = true

        if (fullName.isBlank()) {
            nameError = "Full name is required"
            isValid = false
        } else {
            nameError = null
        }

        if (username.isBlank()) {
            usernameError = "Username is required"
            isValid = false
        } else {
            usernameError = null
        }

        if (email.isBlank()) {
            emailError = "Email cannot be empty"
            isValid = false
        } else if (!android.util.Patterns.EMAIL_ADDRESS
                .matcher(email)
                .matches()
        ) {
            emailError = "Invalid email format"
            isValid = false
        } else {
            emailError = null
        }

        if (phoneNumber.isBlank()) {
            phoneError = "Phone number is required"
            isValid = false
        } else {
            phoneError = null
        }

        if (password.isBlank()) {
            passwordError = "Password cannot be empty"
            isValid = false
        } else if (password.length < 6) {
            passwordError = "Password must be at least 6 characters"
            isValid = false
        } else {
            passwordError = null
        }

        if (confirmPassword != password) {
            confirmPasswordError = "Passwords do not match"
            isValid = false
        } else {
            confirmPasswordError = null
        }

        return isValid
    }

    fun attemptRegister() {
        if (validate()) {
            viewModel.register(
                fullName = fullName,
                username = username,
                email = email,
                password = password,
                phoneNumber = phoneNumber,
            )
        }
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
                    .fillMaxSize()
                    .verticalScroll(scrollState),
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
                text = "Create Account",
                style =
                    MaterialTheme.typography.displaySmall.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 32.sp,
                    ),
                color = MaterialTheme.colorScheme.onBackground,
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Join LoFi to manage your finances.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Spacer(modifier = Modifier.height(32.dp))

            // 👤 Full Name
            LofiTextField(
                value = fullName,
                onValueChange = {
                    fullName = it
                    if (nameError != null) nameError = null
                },
                label = "Full Name",
                isError = nameError != null,
                errorMessage = nameError,
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 👤 Username
            LofiTextField(
                value = username,
                onValueChange = {
                    username = it
                    if (usernameError != null) usernameError = null
                },
                label = "Username",
                isError = usernameError != null,
                errorMessage = usernameError,
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 📧 Email
            LofiTextField(
                value = email,
                onValueChange = {
                    email = it
                    if (emailError != null) emailError = null
                },
                label = "Email Address",
                keyboardOptions =
                    androidx.compose.foundation.text.KeyboardOptions(
                        keyboardType = KeyboardType.Email,
                    ),
                isError = emailError != null,
                errorMessage = emailError,
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 📞 Phone Number
            LofiTextField(
                value = phoneNumber,
                onValueChange = {
                    phoneNumber = it
                    if (phoneError != null) phoneError = null
                },
                label = "Phone Number",
                keyboardOptions =
                    androidx.compose.foundation.text.KeyboardOptions(
                        keyboardType = KeyboardType.Phone,
                    ),
                isError = phoneError != null,
                errorMessage = phoneError,
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 🔒 Password
            LofiTextField(
                value = password,
                onValueChange = {
                    password = it
                    if (passwordError != null) passwordError = null
                },
                label = "Password",
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions =
                    androidx.compose.foundation.text.KeyboardOptions(
                        keyboardType = KeyboardType.Password,
                    ),
                isError = passwordError != null,
                errorMessage = passwordError,
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 🔒 Confirm Password
            LofiTextField(
                value = confirmPassword,
                onValueChange = {
                    confirmPassword = it
                    if (confirmPasswordError != null) confirmPasswordError = null
                },
                label = "Confirm Password",
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions =
                    androidx.compose.foundation.text.KeyboardOptions(
                        keyboardType = KeyboardType.Password,
                    ),
                isError = confirmPasswordError != null,
                errorMessage = confirmPasswordError,
            )

            Spacer(modifier = Modifier.height(32.dp))

            // 🚀 Register Button
            LofiButton(
                text = "Create Account",
                onClick = { attemptRegister() },
                isLoading = uiState is RegisterUiState.Loading,
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Or Divider
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
            ) {
                HorizontalDivider(modifier = Modifier.weight(1f))
                Text(
                    text = "  Or sign up with  ",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                HorizontalDivider(modifier = Modifier.weight(1f))
            }

            // Only Google Sign-Up for now (Facebook requires Play Store verification)
            SocialAuthButton(
                text = "Sign up with Google",
                onClick = {
                    val client = GoogleSignIn.getClient(context, googleSignInOptions)
                    // Sign out first to ensure fresh sign-in flow and show account picker
                    client.signOut().addOnCompleteListener {
                        googleLauncher.launch(client.signInIntent)
                    }
                },
                modifier = Modifier.fillMaxWidth(),
            )

            // TODO: Enable Facebook login after app is published to Play Store
            // SocialAuthButton(
            //     text = "Facebook",
            //     onClick = { /* Facebook Login */ },
            //     modifier = Modifier.weight(1f)
            // )

            Spacer(modifier = Modifier.height(24.dp))

            // 📝 Login Link
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
            ) {
                Text(
                    text = "Already have an account? ",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    text = "Sign In",
                    style =
                        MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Bold,
                        ),
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.clickable { onLoginClick() },
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun RegisterScreenPreview() {
    LofiTheme {
        RegisterScreen()
    }
}
