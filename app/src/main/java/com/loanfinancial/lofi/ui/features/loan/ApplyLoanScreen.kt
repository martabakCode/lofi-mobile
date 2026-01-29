package com.loanfinancial.lofi.ui.features.loan

import androidx.compose.foundation.layout.Arrangement
import kotlinx.coroutines.launch
import androidx.compose.foundation.layout.Column
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.loanfinancial.lofi.core.common.result.ErrorType
import com.loanfinancial.lofi.core.media.DocumentType
import com.loanfinancial.lofi.ui.components.LofiButton
import com.loanfinancial.lofi.ui.components.LofiTextField
import com.loanfinancial.lofi.ui.components.LofiTopBar
import androidx.core.content.FileProvider
import com.loanfinancial.lofi.core.util.FileUtil
import java.io.File
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.platform.LocalContext
import android.net.Uri
import com.loanfinancial.lofi.ui.features.loan.components.DocumentUploadSection

@Composable
fun ApplyLoanScreen(
    navigateUp: () -> Unit,
    viewModel: ApplyLoanViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val formState by viewModel.formState.collectAsState()

    var showErrorDialog by remember { mutableStateOf<ErrorType?>(null) }
    var showSuccessDialog by remember { mutableStateOf(false) }

    val context = LocalContext.current
    var tempPhotoUri by remember { mutableStateOf<Uri?>(null) }
    var currentDocumentType by remember { mutableStateOf<DocumentType?>(null) }

    // Camera Launcher
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success && tempPhotoUri != null && currentDocumentType != null) {
            FileUtil.from(context = context, uri = tempPhotoUri!!)?.let { file ->
                viewModel.onEvent(ApplyLoanUiEvent.DocumentSelected(currentDocumentType!!, file.absolutePath))
            }
        }
    }

    // Gallery Launcher
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null && currentDocumentType != null) {
            FileUtil.from(context = context, uri = uri)?.let { file ->
                viewModel.onEvent(ApplyLoanUiEvent.DocumentSelected(currentDocumentType!!, file.absolutePath))
            }
        }
    }

    val wrappedOnEvent: (ApplyLoanUiEvent) -> Unit = { event ->
        when (event) {
            is ApplyLoanUiEvent.CaptureDocument -> {
                currentDocumentType = event.documentType
                val photoFile = File.createTempFile(
                    "IMG_${System.currentTimeMillis()}_",
                    ".jpg",
                    context.cacheDir
                )
                val uri = FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.fileprovider",
                    photoFile
                )
                tempPhotoUri = uri
                cameraLauncher.launch(uri)
            }
            is ApplyLoanUiEvent.SelectDocumentFromGallery -> {
                currentDocumentType = event.documentType
                galleryLauncher.launch("image/*")
            }
            else -> viewModel.onEvent(event)
        }
    }


    when (val state = uiState) {
        is ApplyLoanUiState.Error -> {
            showErrorDialog = state.error
        }
        is ApplyLoanUiState.Success -> {
            showSuccessDialog = true
        }
        else -> { }
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
            LoanFormSection(
                formState = formState,
                onEvent = wrappedOnEvent,
                enabled =
                    uiState !is ApplyLoanUiState.Loading &&
                        uiState !is ApplyLoanUiState.BiometricAuthenticating &&
                        uiState !is ApplyLoanUiState.CapturingLocation &&
                        uiState !is ApplyLoanUiState.UploadingDocuments,
            )

            Spacer(modifier = Modifier.height(8.dp))

            DocumentUploadSection(
                ktpState = formState.documents[DocumentType.KTP],
                selfieState = formState.documents[DocumentType.SELFIE],
                onEvent = wrappedOnEvent,
            )

            Spacer(modifier = Modifier.height(8.dp))

            BiometricSection(
                isVerified = formState.isBiometricVerified,
                onVerifyClick = { wrappedOnEvent(ApplyLoanUiEvent.BiometricAuthenticate) },
                enabled =
                    uiState !is ApplyLoanUiState.Loading &&
                        uiState !is ApplyLoanUiState.BiometricAuthenticating &&
                        uiState !is ApplyLoanUiState.CapturingLocation &&
                        uiState !is ApplyLoanUiState.UploadingDocuments,
                viewModel = viewModel,
            )

            LocationSection(
                latitude = formState.latitude,
                longitude = formState.longitude,
                onCaptureClick = { wrappedOnEvent(ApplyLoanUiEvent.CaptureLocation) },
                enabled =
                    uiState !is ApplyLoanUiState.Loading &&
                        uiState !is ApplyLoanUiState.BiometricAuthenticating &&
                        uiState !is ApplyLoanUiState.CapturingLocation &&
                        uiState !is ApplyLoanUiState.UploadingDocuments,
            )

            Spacer(modifier = Modifier.height(16.dp))

            when (uiState) {
                is ApplyLoanUiState.Loading -> {
                    LoadingState(message = "Submitting loan application...")
                }
                is ApplyLoanUiState.BiometricAuthenticating -> {
                    LoadingState(message = "Verifying biometric...")
                }
                is ApplyLoanUiState.CapturingLocation -> {
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
                    SubmitButton(
                        onClick = { wrappedOnEvent(ApplyLoanUiEvent.SubmitClicked) },
                        enabled = formState.isValid(),
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
}

@Composable
private fun LoanFormSection(
    formState: ApplyLoanFormState,
    onEvent: (ApplyLoanUiEvent) -> Unit,
    enabled: Boolean,
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
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
    val biometricAuthenticator = remember(activity) {
        activity?.let { com.loanfinancial.lofi.core.biometric.BiometricAuthenticatorImpl(it) }
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
                                biometricAuthenticator.authenticate(
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
    val locationPermissionLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.RequestMultiplePermissions()
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
                                android.Manifest.permission.ACCESS_COARSE_LOCATION
                            )
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
