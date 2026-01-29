package com.loanfinancial.lofi.core.navigation
import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.loanfinancial.lofi.data.model.dto.NotificationType
import com.loanfinancial.lofi.ui.features.auth.login.LoginScreen
import com.loanfinancial.lofi.ui.features.auth.register.RegisterScreen
import com.loanfinancial.lofi.ui.features.auth.biometric.BiometricLoginScreen
import com.loanfinancial.lofi.ui.features.dashboard.DashboardScreen
import com.loanfinancial.lofi.ui.features.loan.ApplyLoanScreen
import com.loanfinancial.lofi.ui.features.loan.LoanDetailScreen
import com.loanfinancial.lofi.ui.features.notification.NotificationDetailScreen
import com.loanfinancial.lofi.ui.features.notification.NotificationScreen
import com.loanfinancial.lofi.ui.features.profile.ChangePasswordScreen
import com.loanfinancial.lofi.ui.features.profile.EditProfileScreen
import com.loanfinancial.lofi.ui.features.profile.ProfileDetailScreen
import com.loanfinancial.lofi.ui.features.splash.SplashScreen
import com.loanfinancial.lofi.ui.screens.auth.ForgotPasswordScreen

@Composable
fun NavGraph() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Routes.SPLASH,
    ) {
        composable(Routes.SPLASH) {
            SplashScreen(
                onNavigateToLogin = {
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(Routes.SPLASH) { inclusive = true }
                    }
                },
                onNavigateToDashboard = {
                    navController.navigate(Routes.DASHBOARD) {
                        popUpTo(Routes.SPLASH) { inclusive = true }
                    }
                },
            )
        }

        composable(
            route = Routes.DASHBOARD + "?guest={guest}",
            arguments =
                listOf(
                    navArgument("guest") {
                        type = NavType.BoolType
                        defaultValue = false
                    },
                ),
        ) { backStackEntry ->
            val isGuest = backStackEntry.arguments?.getBoolean("guest") ?: false
            DashboardScreen(
                isGuest = isGuest,
                onNavigateToLogin = {
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(Routes.DASHBOARD) { inclusive = true }
                    }
                },
                onNavigateToApplyLoan = { navController.navigate(Routes.APPLY_LOAN) },
                onNavigateToLoanDetail = { loanId -> navController.navigate(Routes.LOAN_DETAIL.replace("{loanId}", loanId)) },
                onNavigateToNotifications = { navController.navigate(Routes.NOTIFICATIONS) },
                onNavigateToEditProfile = { navController.navigate(Routes.EDIT_PROFILE) },
                onNavigateToChangePassword = { navController.navigate(Routes.CHANGE_PASSWORD) },
                onNavigateToProfileDetail = { navController.navigate(Routes.PROFILE_DETAIL) },
            )
        }

        composable(Routes.LOGIN) {
            LoginScreen(
                onLoginClick = {
                    navController.navigate(Routes.DASHBOARD) {
                        popUpTo(Routes.LOGIN) { inclusive = true }
                    }
                },
                onSkipLoginClick = {
                    navController.navigate(Routes.DASHBOARD + "?guest=true") {
                        popUpTo(Routes.LOGIN) { inclusive = true }
                    }
                },
                onRegisterClick = {
                    navController.navigate(Routes.REGISTER)
                },
                onForgotPasswordClick = {
                    navController.navigate(Routes.FORGOT_PASSWORD)
                },
                onBiometricLoginClick = {
                    navController.navigate(Routes.BIOMETRIC_LOGIN)
                },
            )
        }

        composable(Routes.BIOMETRIC_LOGIN) {
            BiometricLoginScreen(
                onBiometricSuccess = {
                    navController.navigate(Routes.DASHBOARD) {
                        popUpTo(Routes.LOGIN) { inclusive = true }
                    }
                },
                onBiometricFailed = { error ->
                    // Show error snackbar and stay on screen
                    // User can tap to retry or use password
                },
                onUsePasswordClick = {
                    navController.popBackStack()
                },
            )
        }

        composable(Routes.REGISTER) {
            RegisterScreen(
                onRegisterSuccess = {
                    navController.popBackStack()
                },
                onLoginClick = {
                    navController.popBackStack()
                },
            )
        }

        composable(Routes.FORGOT_PASSWORD) {
            ForgotPasswordScreen(
                onBackClick = {
                    navController.popBackStack()
                },
            )
        }

        composable(Routes.APPLY_LOAN) {
            ApplyLoanScreen(
                navigateUp = { navController.navigateUp() },
            )
        }

        composable(Routes.LOAN_DETAIL) { backStackEntry ->
            val loanId = backStackEntry.arguments?.getString("loanId") ?: ""
            LoanDetailScreen(
                loanId = loanId,
                navigateUp = { navController.navigateUp() },
            )
        }

        composable(Routes.NOTIFICATIONS) {
            NotificationScreen(
                navigateUp = { navController.navigateUp() },
                onNotificationClick = { notification ->
                    when (notification.type) {
                        NotificationType.LOAN -> {
                            notification.referenceId?.let { refId ->
                                navController.navigate(Routes.LOAN_DETAIL.replace("{loanId}", refId))
                            }
                        }
                        NotificationType.AUTH -> {
                            navController.navigate(Routes.PROFILE_DETAIL)
                        }
                        NotificationType.SYSTEM -> {
                            navController.navigate(Routes.NOTIFICATION_DETAIL.replace("{notificationId}", notification.id))
                        }
                    }
                },
            )
        }

        composable(Routes.NOTIFICATION_DETAIL) { backStackEntry ->
            val notifId = backStackEntry.arguments?.getString("notificationId") ?: ""
            NotificationDetailScreen(
                notificationId = notifId,
                navigateUp = { navController.navigateUp() },
            )
        }

        composable(Routes.EDIT_PROFILE) {
            EditProfileScreen(
                navigateUp = { navController.navigateUp() },
            )
        }

        composable(Routes.CHANGE_PASSWORD) {
            ChangePasswordScreen(
                navigateUp = { navController.navigateUp() },
            )
        }

        composable(Routes.PROFILE_DETAIL) {
            ProfileDetailScreen(
                navigateUp = { navController.navigateUp() },
            )
        }
    }
}
