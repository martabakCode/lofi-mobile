package com.loanfinancial.lofi.core.navigation
import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.loanfinancial.lofi.data.model.dto.NotificationType
import com.loanfinancial.lofi.ui.features.auth.biometric.BiometricLoginScreen
import com.loanfinancial.lofi.ui.features.auth.login.LoginScreen
import com.loanfinancial.lofi.ui.features.auth.register.RegisterScreen
import com.loanfinancial.lofi.ui.features.dashboard.DashboardScreen
import com.loanfinancial.lofi.ui.features.loan.ApplyLoanScreen
import com.loanfinancial.lofi.ui.features.loan.DocumentUploadScreen
import com.loanfinancial.lofi.ui.features.loan.LoanDetailScreen
import com.loanfinancial.lofi.ui.features.loan.LoanPreviewScreen
import com.loanfinancial.lofi.ui.features.loan.LoanTnCScreen
import com.loanfinancial.lofi.ui.features.loan.LoanFormData
import com.loanfinancial.lofi.ui.features.loan.model.toPreviewData
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.loanfinancial.lofi.ui.features.notification.NotificationDetailScreen
import com.loanfinancial.lofi.ui.features.notification.NotificationScreen
import com.loanfinancial.lofi.ui.features.profile.ChangePasswordScreen
import com.loanfinancial.lofi.ui.features.profile.EditProfileScreen
import com.loanfinancial.lofi.ui.features.profile.ProfileDetailScreen
import com.loanfinancial.lofi.ui.features.profile.SetPinScreen
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
                onNavigateToOnboarding = {
                    navController.navigate(Routes.ONBOARDING) {
                        popUpTo(Routes.SPLASH) { inclusive = true }
                    }
                },
                onNavigateToLogin = {
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(Routes.SPLASH) { inclusive = true }
                    }
                },
                onNavigateToDashboard = { isGuest ->
                    navController.navigate(Routes.DASHBOARD + "?guest=$isGuest") {
                        popUpTo(Routes.SPLASH) { inclusive = true }
                    }
                },
                onNavigateToCompleteProfile = {
                    navController.navigate(Routes.COMPLETE_PROFILE) {
                        popUpTo(Routes.SPLASH) { inclusive = true }
                    }
                },
                onNavigateToSetPin = {
                    navController.navigate(Routes.setPin(fromProfile = true)) {
                        popUpTo(Routes.SPLASH) { inclusive = true }
                    }
                },
                onNavigateToSetGooglePin = {
                    navController.navigate(Routes.SET_GOOGLE_PIN) {
                        popUpTo(Routes.SPLASH) { inclusive = true }
                    }
                }
            )
        }

        composable(Routes.ONBOARDING) {
            val viewModel: com.loanfinancial.lofi.ui.features.onboarding.OnboardingViewModel = hiltViewModel()
            com.loanfinancial.lofi.ui.features.onboarding.OnboardingScreen(
                dataStoreManager = viewModel.dataStoreManager,
                onComplete = {
                    navController.navigate(Routes.DASHBOARD + "?guest=true") {
                        popUpTo(Routes.ONBOARDING) { inclusive = true }
                    }
                },
                onSkip = {
                    navController.navigate(Routes.DASHBOARD + "?guest=true") {
                        popUpTo(Routes.ONBOARDING) { inclusive = true }
                    }
                }
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
                onNavigateToApplyLoan = { navController.navigate(Routes.applyLoan()) },
                onNavigateToLoanDetail = { loanId -> navController.navigate(Routes.LOAN_DETAIL.replace("{loanId}", loanId)) },
                onNavigateToNotifications = { navController.navigate(Routes.NOTIFICATIONS) },
                onNavigateToEditProfile = { navController.navigate(Routes.EDIT_PROFILE) },
                onNavigateToChangePassword = { navController.navigate(Routes.CHANGE_PASSWORD) },
                onNavigateToProfileDetail = { navController.navigate(Routes.PROFILE_DETAIL) },
                onNavigateToSetPin = { navController.navigate(Routes.SET_PIN) },
                onNavigateToSetGooglePin = { navController.navigate(Routes.SET_GOOGLE_PIN) },
                onNavigateToChangeGooglePin = { navController.navigate(Routes.CHANGE_GOOGLE_PIN) },
                onNavigateToDraftList = { navController.navigate(Routes.DRAFT_LIST) },
            )
        }

        composable(Routes.LOGIN) {
            LoginScreen(
                onLoginClick = { profileCompleted, pinSet, isGoogleLogin ->
                    val target = when {
                        !profileCompleted -> Routes.COMPLETE_PROFILE
                        !pinSet -> if (isGoogleLogin) Routes.SET_GOOGLE_PIN else Routes.setPin(fromProfile = true)
                        else -> Routes.DASHBOARD
                    }
                    navController.navigate(target) {
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

        composable(
            route = Routes.APPLY_LOAN,
            arguments = listOf(
                androidx.navigation.navArgument("draftId") {
                    type = androidx.navigation.NavType.StringType
                    nullable = true
                    defaultValue = null
                }
            )
        ) { backStackEntry ->
            val applyLoanViewModel: com.loanfinancial.lofi.ui.features.loan.ApplyLoanViewModel = hiltViewModel()
            val draftId = backStackEntry.arguments?.getString("draftId")
            
            ApplyLoanScreen(
                navigateUp = { navController.navigateUp() },
                onNavigateToDocumentUpload = {
                    val currentDraftId = applyLoanViewModel.formState.value.draftId
                    navController.navigate(Routes.documentUpload(currentDraftId))
                },
                draftId = draftId,
                viewModel = applyLoanViewModel,
            )
        }

        composable(
            route = Routes.DOCUMENT_UPLOAD,
            arguments = listOf(
                androidx.navigation.navArgument("draftId") {
                    type = androidx.navigation.NavType.StringType
                    nullable = true
                    defaultValue = null
                }
            )
        ) { backStackEntry ->
            val draftId = backStackEntry.arguments?.getString("draftId")
            
            DocumentUploadScreen(
                loanId = draftId ?: "",
                onNavigateToPreview = { navController.navigate(Routes.LOAN_PREVIEW) },
                onNavigateBack = { navController.navigateUp() }
            )
        }

        composable(Routes.LOAN_PREVIEW) { backStackEntry ->
            // Safely get the back stack entry to prevent crash when navigating away
            val applyLoanBackStackEntry = runCatching {
                navController.getBackStackEntry(Routes.APPLY_LOAN_BASE)
            }.getOrNull()
            
            if (applyLoanBackStackEntry != null) {
                val applyLoanViewModel: com.loanfinancial.lofi.ui.features.loan.ApplyLoanViewModel =
                    hiltViewModel(viewModelStoreOwner = applyLoanBackStackEntry)
                val formState by applyLoanViewModel.formState.collectAsState()
                val previewData = androidx.compose.runtime.remember(formState) { formState.toPreviewData() }

                LoanPreviewScreen(
                    previewData = previewData,
                    onNavigateToTnC = { navController.navigate(Routes.LOAN_TNC) },
                    onNavigateBack = { navController.navigateUp() },
                )
            } else {
                // Back stack entry not found, navigate back to dashboard
                androidx.compose.runtime.LaunchedEffect(Unit) {
                    navController.navigate(Routes.DASHBOARD) {
                        popUpTo(Routes.DASHBOARD) { inclusive = true }
                    }
                }
            }
        }

        composable(Routes.LOAN_TNC) {
            // Safely get the back stack entry to prevent crash when navigating away
            val applyLoanBackStackEntry = runCatching {
                navController.getBackStackEntry(Routes.APPLY_LOAN_BASE)
            }.getOrNull()
            
            if (applyLoanBackStackEntry != null) {
                val applyLoanViewModel: com.loanfinancial.lofi.ui.features.loan.ApplyLoanViewModel =
                    hiltViewModel(viewModelStoreOwner = applyLoanBackStackEntry)
                val formState by applyLoanViewModel.formState.collectAsState()
                val tncViewModel: com.loanfinancial.lofi.ui.features.loan.LoanTnCViewModel = hiltViewModel()

                // Pass form data to TnC ViewModel
                androidx.compose.runtime.LaunchedEffect(formState) {
                    tncViewModel.setLoanFormData(
                        LoanFormData(
                            amount = formState.amount,
                            tenor = formState.tenor,
                            purpose = formState.purpose,
                            latitude = formState.latitude,
                            longitude = formState.longitude,
                            documents = formState.documents
                                .mapKeys { it.key.name }
                                .mapValues { it.value.filePath ?: "" }
                                .filterValues { it.isNotEmpty() }
                        )
                    )
                }

                LoanTnCScreen(
                    onNavigateBack = { navController.navigateUp() },
                    onSubmitSuccess = {
                        navController.navigate(Routes.DASHBOARD) {
                            popUpTo(Routes.DASHBOARD) { inclusive = true }
                        }
                    },
                    viewModel = tncViewModel,
                )
            } else {
                // Back stack entry not found, navigate back to dashboard
                androidx.compose.runtime.LaunchedEffect(Unit) {
                    navController.navigate(Routes.DASHBOARD) {
                        popUpTo(Routes.DASHBOARD) { inclusive = true }
                    }
                }
            }
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

        composable(
            route = Routes.SET_PIN + "?fromProfile={fromProfile}",
            arguments = listOf(
                navArgument("fromProfile") {
                    type = NavType.BoolType
                    defaultValue = false
                }
            )
        ) { backStackEntry ->
            val fromProfile = backStackEntry.arguments?.getBoolean("fromProfile") ?: false
            SetPinScreen(
                onBackClick = { 
                    if (fromProfile) {
                        // If from profile completion, go back to login
                        navController.navigate(Routes.LOGIN) {
                            popUpTo(Routes.SPLASH) { inclusive = true }
                        }
                    } else {
                        navController.navigateUp()
                    }
                },
                onSuccess = { 
                    if (fromProfile) {
                        // After setting PIN from profile flow, go to Dashboard
                        navController.navigate(Routes.DASHBOARD) {
                            popUpTo(Routes.SPLASH) { inclusive = true }
                        }
                    } else {
                        navController.navigateUp()
                    }
                }
            )
        }

        composable(Routes.DRAFT_LIST) {
            com.loanfinancial.lofi.ui.features.loan.draft.DraftListScreen(
                navigateUp = { navController.navigateUp() },
                onDraftClick = { draftId -> navController.navigate(Routes.applyLoan(draftId)) }
            )
        }
        
        composable(Routes.COMPLETE_PROFILE) {
            com.loanfinancial.lofi.ui.features.profile.CompleteProfileScreen(
                onCompleteSuccess = {
                    // After completing profile, navigate to Set PIN
                    navController.navigate(Routes.setPin(fromProfile = true)) {
                        popUpTo(Routes.COMPLETE_PROFILE) { inclusive = true }
                    }
                },
                onBackClick = {
                    // Option to logout or exit
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(Routes.SPLASH) { inclusive = true }
                    }
                }
            )
        }

        composable(Routes.SET_GOOGLE_PIN) {
            com.loanfinancial.lofi.ui.features.auth.google.SetGooglePinScreen(
                onBackClick = { navController.navigateUp() },
                onSuccess = { 
                    navController.navigate(Routes.DASHBOARD) {
                        popUpTo(Routes.SPLASH) { inclusive = true }
                    }
                },
                onNavigateToChangePin = {
                    navController.navigate(Routes.CHANGE_GOOGLE_PIN) {
                        popUpTo(Routes.SET_GOOGLE_PIN) { inclusive = true }
                    }
                }
            )
        }

        composable(Routes.CHANGE_GOOGLE_PIN) {
            com.loanfinancial.lofi.ui.features.auth.google.ChangeGooglePinScreen(
                onBackClick = { navController.navigateUp() },
                onSuccess = { navController.navigateUp() }
            )
        }
    }
}
