package com.loanfinancial.lofi.ui.features.splash

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.loanfinancial.lofi.ui.components.LofiLogoLarge
import com.loanfinancial.lofi.ui.theme.LofiTheme
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    onNavigateToOnboarding: () -> Unit = {},
    onNavigateToLogin: () -> Unit = {},
    onNavigateToDashboard: (Boolean) -> Unit = {},
    onNavigateToCompleteProfile: () -> Unit = {},
    onNavigateToSetPin: () -> Unit = {},
    onNavigateToSetGooglePin: () -> Unit = {},
    viewModel: SplashViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()

    // Animation states
    var startAnimation by remember { mutableStateOf(false) }
    val scaleAnim =
        animateFloatAsState(
            targetValue = if (startAnimation) 1f else 0.5f,
            animationSpec =
                tween(
                    durationMillis = 800,
                    easing = EaseOutBack,
                ),
            label = "scale",
        )
    val alphaAnim =
        animateFloatAsState(
            targetValue = if (startAnimation) 1f else 0f,
            animationSpec =
                tween(
                    durationMillis = 600,
                    easing = LinearEasing,
                ),
            label = "alpha",
        )

    LaunchedEffect(Unit) {
        startAnimation = true
        delay(2000) // Show splash for 2 seconds
        viewModel.checkAuthStatus()
    }

    LaunchedEffect(uiState) {
        when (val state = uiState) {
            is SplashUiState.NavigateToOnboarding -> {
                onNavigateToOnboarding()
            }
            is SplashUiState.NavigateToLogin -> {
                onNavigateToLogin()
            }
            is SplashUiState.NavigateToDashboard -> {
                onNavigateToDashboard(state.isGuest)
            }
            is SplashUiState.NavigateToCompleteProfile -> {
                onNavigateToCompleteProfile()
            }
            is SplashUiState.NavigateToSetPin -> {
                onNavigateToSetPin()
            }
            is SplashUiState.NavigateToSetGooglePin -> {
                onNavigateToSetGooglePin()
            }
            else -> {}
        }
    }

    SplashScreenContent(
        scale = scaleAnim.value,
        alpha = alphaAnim.value,
    )
}

@Composable
fun SplashScreenContent(
    scale: Float = 1f,
    alpha: Float = 1f,
) {
    Box(
        modifier =
            Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            // 🎨 Logo with animation
            Box(
                modifier =
                    Modifier
                        .scale(scale)
                        .alpha(alpha),
            ) {
                LofiLogoLarge()
            }

            Spacer(modifier = Modifier.height(24.dp))

            // App Name
            Text(
                text = "LoFi",
                style =
                    MaterialTheme.typography.displayMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 36.sp,
                    ),
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.alpha(alpha),
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Tagline
            Text(
                text = "Loan Financial",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.alpha(alpha),
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SplashScreenPreview() {
    LofiTheme {
        SplashScreenContent()
    }
}
