package com.loanfinancial.lofi.ui.features.loan

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.hilt.navigation.compose.hiltViewModel
import com.loanfinancial.lofi.core.common.result.ErrorType
import com.loanfinancial.lofi.core.media.DocumentType
import com.loanfinancial.lofi.core.util.FileUtil
import com.loanfinancial.lofi.ui.components.LofiButton
import com.loanfinancial.lofi.ui.components.LofiTextField
import com.loanfinancial.lofi.ui.components.LofiTopBar
import kotlinx.coroutines.launch
import java.io.File
import java.text.NumberFormat
import java.util.Locale

@Composable
fun ApplyLoanScreen(
    navigateUp: () -> Unit,
    onNavigateToDocumentUpload: (() -> Unit)? = null,
    draftId: String? = null,
    viewModel: ApplyLoanViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val formState by viewModel.formState.collectAsState()

    var showErrorDialog by remember { mutableStateOf<ErrorType?>(null) }
    var showSuccessDialog by remember { mutableStateOf(false) }
    var showDraftSavedDialog by remember { mutableStateOf(false) }

    // Load draft if draftId is provided
    androidx.compose.runtime.LaunchedEffect(draftId) {
        if (draftId != null && formState.draftId == null) {
            viewModel.loadDraft(draftId)
        }
    }

    val context = LocalContext.current
    var tempPhotoUri by remember { mutableStateOf<Uri?>(null) }
    var currentDocumentType by remember { mutableStateOf<DocumentType?>(null) }

    // Camera Launcher
    val cameraLauncher =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.TakePicture(),
        ) { success ->
            if (success && tempPhotoUri != null && currentDocumentType != null) {
                FileUtil.from(context = context, uri = tempPhotoUri!!)?.let { file ->
                    viewModel.onEvent(ApplyLoanUiEvent.DocumentSelected(currentDocumentType!!, file.absolutePath))
                }
            }
        }

    // Gallery Launcher
    val galleryLauncher =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.GetContent(),
        ) { uri: Uri? ->
            if (uri != null && currentDocumentType != null) {
                FileUtil.from(context = context, uri = uri)?.let { file ->
                    viewModel.onEvent(ApplyLoanUiEvent.DocumentSelected(currentDocumentType!!, file.absolutePath))
                }
            }
        }


    // Camera Permission Launcher
    val cameraPermissionLauncher =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.RequestPermission(),
        ) { isGranted ->
            if (isGranted && tempPhotoUri != null) {
                cameraLauncher.launch(tempPhotoUri!!)
            }
        }

    val activity = context as? androidx.fragment.app.FragmentActivity
    val biometricAuthenticator =
        remember(activity) {
            activity?.let {
                com.loanfinancial.lofi.core.biometric
                    .BiometricAuthenticatorImpl(it)
            }
        }


    // Handle AutoCapturingBiometric state
    androidx.compose.runtime.LaunchedEffect(uiState) {
        if (uiState is ApplyLoanUiState.AutoCapturingBiometric) {
            if (biometricAuthenticator != null) {
                biometricAuthenticator
                    .authenticate(
                        title = "Verify Identity",
                        subtitle = "Use your biometric credential",
                        description = "Authenticate to proceed with loan application",
                    ).collect { result ->
                        viewModel.onBiometricResult(result)
                    }
            } else {
                 // Fallback or error if authenticator is not available (e.g. not an Activity context)
                 // For now, we rely on the implementation being correct.
            }
        }
    }

    val wrappedOnEvent: (ApplyLoanUiEvent) -> Unit = { event ->
        when (event) {
            is ApplyLoanUiEvent.CaptureDocument -> {
                currentDocumentType = event.documentType
                val photoFile =
                    File.createTempFile(
                        "IMG_${System.currentTimeMillis()}_",
                        ".jpg",
                        context.cacheDir,
                    )
                val uri =
                    FileProvider.getUriForFile(
                        context,
                        "${context.packageName}.fileprovider",
                        photoFile,
                    )
                tempPhotoUri = uri

                if (androidx.core.content.ContextCompat.checkSelfPermission(
                        context,
                        android.Manifest.permission.CAMERA,
                    ) == android.content.pm.PackageManager.PERMISSION_GRANTED
                ) {
                    cameraLauncher.launch(uri)
                } else {
                    cameraPermissionLauncher.launch(android.Manifest.permission.CAMERA)
                }
            }
            is ApplyLoanUiEvent.SelectDocumentFromGallery -> {
                currentDocumentType = event.documentType
                galleryLauncher.launch("image/*")
            }
            else -> viewModel.onEvent(event)
        }
    }

    // Location Permission Launcher
    val locationPermissionLauncher =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.RequestMultiplePermissions(),
        ) { permissions ->
            if (permissions.getOrDefault(android.Manifest.permission.ACCESS_FINE_LOCATION, false) ||
                permissions.getOrDefault(android.Manifest.permission.ACCESS_COARSE_LOCATION, false)
            ) {
                wrappedOnEvent(ApplyLoanUiEvent.AutoCaptureLocation)
            }
        }

    // Automatic Biometric & Location Capture
    androidx.compose.runtime.LaunchedEffect(
        formState.currentStep,
        formState.bankName,
        formState.accountNumber,
        formState.isBiometricVerified
    ) {
        if (formState.currentStep == 4 &&
            formState.bankName.isNotBlank() &&
            formState.accountNumber.isNotBlank()
        ) {
            // Auto Biometric First
            if (!formState.isBiometricVerified) {
                wrappedOnEvent(ApplyLoanUiEvent.AutoBiometricAuthenticate)
            } else if (formState.latitude == null) {
                // Auto Location Second (only if biometric verified)
                val hasPermission =
                    androidx.core.content.ContextCompat.checkSelfPermission(
                        context,
                        android.Manifest.permission.ACCESS_COARSE_LOCATION,
                    ) == android.content.pm.PackageManager.PERMISSION_GRANTED

                if (hasPermission) {
                    wrappedOnEvent(ApplyLoanUiEvent.AutoCaptureLocation)
                } else {
                    locationPermissionLauncher.launch(
                        arrayOf(
                            android.Manifest.permission.ACCESS_FINE_LOCATION,
                            android.Manifest.permission.ACCESS_COARSE_LOCATION,
                        ),
                    )
                }
            }
        }
    }

    when (val state = uiState) {
        is ApplyLoanUiState.Error -> {
            showErrorDialog = state.error
        }
        is ApplyLoanUiState.ValidationError -> {
            showErrorDialog = com.loanfinancial.lofi.core.common.result.ErrorType.UnknownError(
                state.errors.values.joinToString("\n")
            )
        }
        is ApplyLoanUiState.Success -> {
            showSuccessDialog = true
        }
        is ApplyLoanUiState.DraftLoaded -> {
             androidx.compose.runtime.LaunchedEffect(Unit) {
                 kotlinx.coroutines.delay(1000)
                 viewModel.onEvent(ApplyLoanUiEvent.RetryClicked)
             }
             LoadingState(message = "Draft loaded successfully")
        }
        is ApplyLoanUiState.DraftSaved -> {
            if (state.showDialog) {
                showDraftSavedDialog = true
            } else {
                // Auto-reset state when not showing dialog
                viewModel.onEvent(ApplyLoanUiEvent.RetryClicked)
            }
        }
        is ApplyLoanUiState.PinRequired -> {
            PinInputDialog(
                onPinSubmit = { wrappedOnEvent(ApplyLoanUiEvent.PinSubmitted(it)) },
                onDismiss = { wrappedOnEvent(ApplyLoanUiEvent.CancelClicked) },
            )
        }
        is ApplyLoanUiState.PinError -> {
            PinInputDialog(
                onPinSubmit = { wrappedOnEvent(ApplyLoanUiEvent.PinSubmitted(it)) },
                onDismiss = { wrappedOnEvent(ApplyLoanUiEvent.CancelClicked) },
                errorMessage = state.message,
            )
        }
        is ApplyLoanUiState.PinLocked -> {
            AlertDialog(
                onDismissRequest = { wrappedOnEvent(ApplyLoanUiEvent.CancelClicked) },
                title = { Text("Account Locked") },
                text = { Text(state.message) },
                confirmButton = {
                    Button(onClick = { wrappedOnEvent(ApplyLoanUiEvent.CancelClicked) }) {
                        Text("Close")
                    }
                },
            )
        }
        is ApplyLoanUiState.ProfileIncomplete -> {
            AlertDialog(
                onDismissRequest = { wrappedOnEvent(ApplyLoanUiEvent.RetryClicked) },
                title = { Text("Profile Incomplete") },
                text = { Text("User profile is incomplete. Please complete your profile first.") },
                confirmButton = {
                    Button(onClick = {
                        wrappedOnEvent(ApplyLoanUiEvent.RetryClicked)
                        navigateUp()
                    }) {
                        Text("OK")
                    }
                },
            )
        }
        is ApplyLoanUiState.PinNotSet -> {
            AlertDialog(
                onDismissRequest = { wrappedOnEvent(ApplyLoanUiEvent.RetryClicked) },
                title = { Text("PIN Required") },
                text = { Text("Please set your PIN before applying for a loan.") },
                confirmButton = {
                    Button(onClick = {
                        wrappedOnEvent(ApplyLoanUiEvent.RetryClicked)
                        navigateUp()
                    }) {
                        Text("OK")
                    }
                },
            )
        }
        else -> { }
    }

    androidx.compose.runtime.LaunchedEffect(uiState) {
        if (uiState is ApplyLoanUiState.ReadyForDocumentUpload) {
            onNavigateToDocumentUpload?.invoke()
            // resetting state to avoid loop if we pop back
             viewModel.onEvent(ApplyLoanUiEvent.RetryClicked) 
        }
    }

    Scaffold(
        topBar = {
            LofiTopBar(
                title = "Apply Loan",
                canNavigateBack = true,
                navigateUp = navigateUp,
            )
        },
    ) { padding ->
        Column(
            modifier =
                Modifier
                    .padding(padding)
                    .padding(16.dp)
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            // Step-based form sections
            when (formState.currentStep) {
                1 -> {
                    BasicInfoSection(
                        formState = formState,
                        onEvent = wrappedOnEvent,
                        enabled = uiState !is ApplyLoanUiState.Loading
                    )
                }
                2 -> {
                    EmploymentInfoSection(
                        formState = formState,
                        onEvent = wrappedOnEvent,
                        enabled = uiState !is ApplyLoanUiState.Loading
                    )
                }
                3 -> {
                    EmergencyContactSection(
                        formState = formState,
                        onEvent = wrappedOnEvent,
                        enabled = uiState !is ApplyLoanUiState.Loading
                    )
                }
                4 -> {
                    BankInfoSection(
                        formState = formState,
                        onEvent = wrappedOnEvent,
                        enabled = uiState !is ApplyLoanUiState.Loading
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Biometric and Location sections are hidden

                    /*
                    BiometricSection(
                        isVerified = formState.isBiometricVerified,
                        onVerifyClick = { wrappedOnEvent(ApplyLoanUiEvent.BiometricAuthenticate) },
                        enabled = uiState !is ApplyLoanUiState.Loading,
                        viewModel = viewModel,
                    )

                    LocationSection(
                        latitude = formState.latitude,
                        longitude = formState.longitude,
                        onCaptureClick = { wrappedOnEvent(ApplyLoanUiEvent.CaptureLocation) },
                        enabled = uiState !is ApplyLoanUiState.Loading,
                    )
                    */
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Loan Preview Section - Show summary at the end or as a small card
            if (formState.currentStep == 1 && (formState.amount.isNotBlank() || formState.tenor.isNotBlank())) {
                LoanPreviewSection(
                    amount = formState.amount,
                    tenor = formState.tenor,
                )
                Spacer(modifier = Modifier.height(16.dp))
            }

            when (uiState) {
                is ApplyLoanUiState.Loading -> {
                    LoadingState(message = "Processing...")
                }
                is ApplyLoanUiState.DraftLoaded -> {
                    LoadingState(message = "Draft loaded successfully")
                }
                is ApplyLoanUiState.BiometricAuthenticating, is ApplyLoanUiState.AutoCapturingBiometric -> {
                    LoadingState(message = "Verifying biometric...")
                }
                is ApplyLoanUiState.CapturingLocation, is ApplyLoanUiState.AutoCapturingLocation -> {
                    LoadingState(message = "Capturing location...")
                }
                is ApplyLoanUiState.UploadingDocuments -> {
                    val uploadState = uiState as ApplyLoanUiState.UploadingDocuments
                    LoadingState(
                        message =
                            "Uploading ${uploadState.currentDocument.name}... " +
                                "(${uploadState.completedDocuments}/${uploadState.totalDocuments})",
                    )
                }
                else -> {
                    StepNavigationButtons(
                        currentStep = formState.currentStep,
                        onNextClick = { 
                            if (formState.currentStep < 4) {
                                wrappedOnEvent(ApplyLoanUiEvent.NextStepClicked)
                            } else {
                                if (onNavigateToDocumentUpload != null && formState.isValid()) {
                                    wrappedOnEvent(ApplyLoanUiEvent.ProceedToDocumentUpload)
                                } else {
                                    wrappedOnEvent(ApplyLoanUiEvent.SubmitClicked)
                                }
                            }
                        },
                        onBackClick = { 
                            if (formState.currentStep > 1) {
                                wrappedOnEvent(ApplyLoanUiEvent.PreviousStepClicked)
                            } else {
                                navigateUp()
                            }
                        },
                        onSaveDraftClick = { wrappedOnEvent(ApplyLoanUiEvent.SaveAsDraftClicked) },
                        isNextEnabled = true, // Validation happens inside next step handler
                        submitButtonText = if (formState.currentStep < 4) "Lanjut" else if (onNavigateToDocumentUpload != null) "Upload Dokumen" else "Submit Application"
                    )
                }
            }
        }
    }

    showErrorDialog?.let { error ->
        AlertDialog(
            onDismissRequest = { showErrorDialog = null },
            title = { Text("Error") },
            text = { Text(error.getErrorMessage()) },
            confirmButton = {
                Button(
                    onClick = {
                        showErrorDialog = null
                        wrappedOnEvent(ApplyLoanUiEvent.RetryClicked)
                    },
                ) {
                    Text("Retry")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showErrorDialog = null }) {
                    Text("Dismiss")
                }
            },
        )
    }

    if (showSuccessDialog) {
        AlertDialog(
            onDismissRequest = { },
            title = { Text("Success") },
            text = { Text("Your loan application has been submitted successfully!") },
            confirmButton = {
                Button(
                    onClick = {
                        showSuccessDialog = false
                        wrappedOnEvent(ApplyLoanUiEvent.ResetClicked)
                        navigateUp()
                    },
                ) {
                    Text("OK")
                }
            },
        )
    }

    if (showDraftSavedDialog) {
        AlertDialog(
            onDismissRequest = { },
            title = { Text("Draft Saved") },
            text = { Text("Your loan application has been saved as draft. You can continue later from the loan history.") },
            confirmButton = {
                Button(
                    onClick = {
                        showDraftSavedDialog = false
                        wrappedOnEvent(ApplyLoanUiEvent.ResetClicked)
                        navigateUp()
                    },
                ) {
                    Text("OK")
                }
            },
        )
    }
}

@Composable
private fun BasicInfoSection(
    formState: ApplyLoanFormState,
    onEvent: (ApplyLoanUiEvent) -> Unit,
    enabled: Boolean,
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text("Loan Details", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        
        LofiTextField(
            value = formState.amount,
            onValueChange = { onEvent(ApplyLoanUiEvent.AmountChanged(it)) },
            label = "Amount (Rp)",
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            enabled = enabled,
        )

        LofiTextField(
            value = formState.tenor,
            onValueChange = { onEvent(ApplyLoanUiEvent.TenorChanged(it)) },
            label = "Tenor (Months)",
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            enabled = enabled,
        )

        LofiTextField(
            value = formState.purpose,
            onValueChange = { onEvent(ApplyLoanUiEvent.PurposeChanged(it)) },
            label = "Purpose",
            enabled = enabled,
        )
        
        LofiTextField(
            value = formState.downPayment,
            onValueChange = { onEvent(ApplyLoanUiEvent.DownPaymentChanged(it)) },
            label = "Down Payment (Optional)",
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            enabled = enabled,
        )
    }
}

@Composable
private fun EmploymentInfoSection(
    formState: ApplyLoanFormState,
    onEvent: (ApplyLoanUiEvent) -> Unit,
    enabled: Boolean,
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text("Employment Information", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)

        LofiTextField(
            value = formState.jobType,
            onValueChange = { onEvent(ApplyLoanUiEvent.JobTypeChanged(it)) },
            label = "Job Type (e.g. Permanent, Contract)",
            enabled = enabled,
        )

        LofiTextField(
            value = formState.companyName,
            onValueChange = { onEvent(ApplyLoanUiEvent.CompanyNameChanged(it)) },
            label = "Company Name",
            enabled = enabled,
        )

        LofiTextField(
            value = formState.jobPosition,
            onValueChange = { onEvent(ApplyLoanUiEvent.JobPositionChanged(it)) },
            label = "Job Position",
            enabled = enabled,
        )

        LofiTextField(
            value = formState.declaredIncome,
            onValueChange = { onEvent(ApplyLoanUiEvent.DeclaredIncomeChanged(it)) },
            label = "Monthly Income (Rp)",
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            enabled = enabled,
        )

        LofiTextField(
            value = formState.workAddress,
            onValueChange = { onEvent(ApplyLoanUiEvent.WorkAddressChanged(it)) },
            label = "Work Address",
            enabled = enabled,
        )
    }
}

@Composable
private fun EmergencyContactSection(
    formState: ApplyLoanFormState,
    onEvent: (ApplyLoanUiEvent) -> Unit,
    enabled: Boolean,
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text("Emergency Contact", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)

        LofiTextField(
            value = formState.emergencyContactName,
            onValueChange = { onEvent(ApplyLoanUiEvent.EmergencyContactNameChanged(it)) },
            label = "Full Name",
            enabled = enabled,
        )

        LofiTextField(
            value = formState.emergencyContactRelation,
            onValueChange = { onEvent(ApplyLoanUiEvent.EmergencyContactRelationChanged(it)) },
            label = "Relation (e.g. Spouse, Parent)",
            enabled = enabled,
        )

        LofiTextField(
            value = formState.emergencyContactPhone,
            onValueChange = { onEvent(ApplyLoanUiEvent.EmergencyContactPhoneChanged(it)) },
            label = "Phone Number",
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
            enabled = enabled,
        )
    }
}

@Composable
private fun BankInfoSection(
    formState: ApplyLoanFormState,
    onEvent: (ApplyLoanUiEvent) -> Unit,
    enabled: Boolean,
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text("Bank Disbursement Account", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)

        LofiTextField(
            value = formState.bankName,
            onValueChange = { onEvent(ApplyLoanUiEvent.BankNameChanged(it)) },
            label = "Bank Name",
            enabled = enabled,
        )

        LofiTextField(
            value = formState.accountNumber,
            onValueChange = { onEvent(ApplyLoanUiEvent.AccountNumberChanged(it)) },
            label = "Account Number",
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            enabled = enabled,
        )

        LofiTextField(
            value = formState.accountHolderName,
            onValueChange = { onEvent(ApplyLoanUiEvent.AccountHolderNameChanged(it)) },
            label = "Account Holder Name",
            enabled = enabled,
        )
    }
}

@Composable
private fun StepNavigationButtons(
    currentStep: Int,
    onNextClick: () -> Unit,
    onBackClick: () -> Unit,
    onSaveDraftClick: () -> Unit,
    isNextEnabled: Boolean,
    submitButtonText: String
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        LofiButton(
            text = submitButtonText,
            onClick = onNextClick,
            enabled = isNextEnabled,
            modifier = Modifier.fillMaxWidth(),
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedButton(
                onClick = onBackClick,
                modifier = Modifier.weight(1f),
            ) {
                Text(if (currentStep > 1) "Kembali" else "Batal")
            }

            OutlinedButton(
                onClick = onSaveDraftClick,
                modifier = Modifier.weight(1f),
            ) {
                Text("Simpan Draft")
            }
        }
    }
}

@Composable
private fun BiometricSection(
    isVerified: Boolean,
    onVerifyClick: () -> Unit,
    enabled: Boolean,
    viewModel: ApplyLoanViewModel,
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val scope = androidx.compose.runtime.rememberCoroutineScope()
    val activity = context as? androidx.fragment.app.FragmentActivity
    val biometricAuthenticator =
        remember(activity) {
            activity?.let {
                com.loanfinancial.lofi.core.biometric
                    .BiometricAuthenticatorImpl(it)
            }
        }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors =
            CardDefaults.cardColors(
                containerColor =
                    if (isVerified) {
                        MaterialTheme.colorScheme.primaryContainer
                    } else {
                        MaterialTheme.colorScheme.surface
                    },
            ),
    ) {
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Icon(
                imageVector = Icons.Default.Fingerprint,
                contentDescription = "Biometric",
                tint =
                    if (isVerified) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    },
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = if (isVerified) "Biometric Verified" else "Biometric Verification Required",
                style = MaterialTheme.typography.bodyMedium,
            )

            if (!isVerified) {
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedButton(
                    onClick = {
                        if (biometricAuthenticator != null) {
                            scope.launch {
                                viewModel.startBiometricAuthentication()
                                biometricAuthenticator
                                    .authenticate(
                                        title = "Verify Identity",
                                        subtitle = "Use your biometric credential",
                                        description = "Authenticate to proceed with loan application",
                                    ).collect { result ->
                                        viewModel.onBiometricResult(result)
                                    }
                            }
                        } else {
                            // Handler for non-activity context if necessary
                        }
                    },
                    enabled = enabled,
                ) {
                    Text("Verify Identity")
                }
            }
        }
    }
}

@Composable
private fun LocationSection(
    latitude: Double?,
    longitude: Double?,
    onCaptureClick: () -> Unit,
    enabled: Boolean,
) {
    val locationPermissionLauncher =
        androidx.activity.compose.rememberLauncherForActivityResult(
            contract =
                androidx.activity.result.contract.ActivityResultContracts
                    .RequestMultiplePermissions(),
        ) { permissions ->
            if (permissions.getOrDefault(android.Manifest.permission.ACCESS_FINE_LOCATION, false) ||
                permissions.getOrDefault(android.Manifest.permission.ACCESS_COARSE_LOCATION, false)
            ) {
                onCaptureClick()
            } else {
                // Permission denied, handle if needed, e.g. show dialog or trigger onCaptureClick to get error
                onCaptureClick()
            }
        }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors =
            CardDefaults.cardColors(
                containerColor =
                    if (latitude != null && longitude != null) {
                        MaterialTheme.colorScheme.primaryContainer
                    } else {
                        MaterialTheme.colorScheme.surface
                    },
            ),
    ) {
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Icon(
                imageVector = Icons.Default.LocationOn,
                contentDescription = "Location",
                tint =
                    if (latitude != null && longitude != null) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    },
            )

            Spacer(modifier = Modifier.height(8.dp))

            if (latitude != null && longitude != null) {
                Text(
                    text = "Location Captured",
                    style = MaterialTheme.typography.bodyMedium,
                )
                Text(
                    text = "Lat: %.4f, Lng: %.4f".format(latitude, longitude),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            } else {
                Text(
                    text = "Location Required",
                    style = MaterialTheme.typography.bodyMedium,
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedButton(
                    onClick = {
                        locationPermissionLauncher.launch(
                            arrayOf(
                                android.Manifest.permission.ACCESS_FINE_LOCATION,
                                android.Manifest.permission.ACCESS_COARSE_LOCATION,
                            ),
                        )
                    },
                    enabled = enabled,
                ) {
                    Text("Capture Location")
                }
            }
        }
    }
}

@Composable
private fun LoadingState(message: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors =
            CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer,
            ),
    ) {
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            CircularProgressIndicator()
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
            )
        }
    }
}

@Composable
private fun LoanPreviewSection(
    amount: String,
    tenor: String,
) {
    val currencyFormatter = NumberFormat.getCurrencyInstance(Locale.forLanguageTag("id-ID"))

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors =
            CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer,
            ),
    ) {
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = "Preview",
                    tint = MaterialTheme.colorScheme.primary,
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Preview Pengajuan",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.primary,
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Amount
            if (amount.isNotBlank()) {
                val amountValue = amount.toLongOrNull() ?: 0
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(
                        text = "Jumlah Pinjaman",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(
                        text = currencyFormatter.format(amountValue),
                        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
            }

            // Tenor
            if (tenor.isNotBlank()) {
                val tenorValue = tenor.toIntOrNull() ?: 0
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(
                        text = "Tenor",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(
                        text = "$tenorValue bulan",
                        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                    )
                }
            }
        }
    }
}

@Composable
fun PinInputDialog(
    onPinSubmit: (String) -> Unit,
    onDismiss: () -> Unit,
    errorMessage: String? = null,
) {
    var pin by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Enter PIN") },
        text = {
            Column {
                Text("Please enter your PIN to continue.")
                Spacer(modifier = Modifier.height(8.dp))
                LofiTextField(
                    value = pin,
                    onValueChange = {
                        if (it.length <= 6 && it.all { char -> char.isDigit() }) {
                            pin = it
                        }
                    },
                    label = "PIN",
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                    isError = errorMessage != null,
                    errorMessage = errorMessage,
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onPinSubmit(pin) },
                enabled = pin.length >= 4,
            ) {
                Text("Verify")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Cancel")
            }
        },
    )
}

@Composable
private fun SubmitButtonsSection(
    onSubmitClick: () -> Unit,
    onSaveDraftClick: () -> Unit,
    isSubmitEnabled: Boolean,
    isSaveDraftEnabled: Boolean,
    submitButtonText: String = "Submit Application",
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        LofiButton(
            text = submitButtonText,
            onClick = onSubmitClick,
            enabled = isSubmitEnabled,
            modifier = Modifier.fillMaxWidth(),
        )

        OutlinedButton(
            onClick = onSaveDraftClick,
            enabled = isSaveDraftEnabled,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text("Save as Draft")
        }
    }
}

@Composable
private fun SubmitButton(
    onClick: () -> Unit,
    enabled: Boolean,
) {
    LofiButton(
        text = "Submit Application",
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier.fillMaxWidth(),
    )
}
