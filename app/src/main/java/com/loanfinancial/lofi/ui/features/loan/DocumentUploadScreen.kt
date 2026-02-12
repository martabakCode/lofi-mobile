package com.loanfinancial.lofi.ui.features.loan

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.loanfinancial.lofi.core.media.DocumentType
import com.loanfinancial.lofi.ui.components.LofiTopBar
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DocumentUploadScreen(
    loanId: String,
    onNavigateToPreview: () -> Unit,
    onNavigateBack: () -> Unit,
    viewModel: DocumentUploadViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    androidx.compose.runtime.LaunchedEffect(loanId) {
        if (loanId.isNotBlank()) {
            viewModel.observeUploadStatus(loanId)
        }
    }
    
    // Bottom Sheet for Camera/Gallery selection
    var showBottomSheet by remember { mutableStateOf(false) }
    var selectedDocumentType by remember { mutableStateOf<DocumentType?>(null) }
    val sheetState = rememberModalBottomSheetState()
    
    // Camera Logic
    var tempPhotoUri by remember { mutableStateOf<Uri?>(null) }
    var tempPhotoFile by remember { mutableStateOf<File?>(null) }
    
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success && tempPhotoFile != null && selectedDocumentType != null) {
            viewModel.onDocumentSelected(loanId, selectedDocumentType!!, tempPhotoFile!!.absolutePath)
        }
        showBottomSheet = false
    }
    
    // Correct gallery launcher
    val galleryLauncherWithCopy = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null && selectedDocumentType != null) {
            try {
                // Determine extension
                val extension = context.contentResolver.getType(uri)?.split("/")?.lastOrNull() ?: "jpg"
                val file = File.createTempFile("gallery_", ".$extension", context.cacheDir)
                context.contentResolver.openInputStream(uri)?.use { input ->
                    file.outputStream().use { output ->
                        input.copyTo(output)
                    }
                }
                viewModel.onDocumentSelected(loanId, selectedDocumentType!!, file.absolutePath)
            } catch (e: Exception) {
                // Handle error
            }
        }
        showBottomSheet = false
    }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted && selectedDocumentType != null) {
             val (file, uri) = viewModel.createTempFileUri(selectedDocumentType!!)
             tempPhotoFile = file
             tempPhotoUri = uri
             cameraLauncher.launch(uri)
        }
    }

    Scaffold(
        topBar = {
            LofiTopBar(
                title = "Upload Documents",
                canNavigateBack = true,
                navigateUp = onNavigateBack
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Please upload the required documents to proceed.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Define the order of documents
                val docTypes = listOf(
                    DocumentType.KTP,
                    DocumentType.NPWP,
                    DocumentType.KK,
                    DocumentType.PAYSLIP,
                    DocumentType.PROOFOFRESIDENCE,
                    DocumentType.BANK_STATEMENT
                )
                
                items(docTypes) { type ->
                    val state = uiState.documents[type] ?: com.loanfinancial.lofi.ui.features.loan.DocumentState()
                    DocumentUploadCard(
                        documentType = type,
                        state = state,
                        onUploadClick = {
                            selectedDocumentType = type
                            showBottomSheet = true
                        },
                        onRetryClick = {
                            viewModel.onRetryUpload(loanId, type)
                        }
                    )
                }
            }

            Button(
                onClick = onNavigateToPreview,
                enabled = uiState.isAllQueued, // Allow preview when docs are queued
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Lanjut ke Preview")
            }
        }
    }

    if (showBottomSheet) {
        ModalBottomSheet(
            onDismissRequest = { showBottomSheet = false },
            sheetState = sheetState
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Select Source",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            // Camera
                            val (file, uri) = viewModel.createTempFileUri(selectedDocumentType!!)
                            tempPhotoFile = file
                            tempPhotoUri = uri
                            
                            // Check permission logic simplified
                            // Assuming permission check done or handled
                            cameraPermissionLauncher.launch(android.Manifest.permission.CAMERA)
                        }
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.CameraAlt, contentDescription = "Camera")
                    Spacer(Modifier.width(16.dp))
                    Text("Take Photo")
                }
                
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            galleryLauncherWithCopy.launch("image/*")
                        }
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Image, contentDescription = "Gallery")
                    Spacer(Modifier.width(16.dp))
                    Text("Choose from Gallery")
                }
                
                Spacer(Modifier.height(16.dp))
            }
        }
    }
}

@Composable
fun DocumentUploadCard(
    documentType: DocumentType,
    state: com.loanfinancial.lofi.ui.features.loan.DocumentState,
    onUploadClick: () -> Unit,
    onRetryClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = getDocumentLabel(documentType),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                
                if (state.error != null) {
                    Text(
                        text = state.error,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                } else if (state.isUploading) {
                    Text(
                        text = "Uploading...",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                } else if (state.isUploaded) {
                     Text(
                        text = "Uploaded",
                        style = MaterialTheme.typography.bodySmall,
                        color = SuccessGreen
                    )
                } else {
                     Text(
                        text = "Not uploaded",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            Spacer(modifier = Modifier.width(8.dp))
            
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(60.dp)
            ) {
                if (state.filePath != null) {
                    AsyncImage(
                        model = state.filePath,
                        contentDescription = "Preview",
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(8.dp)),
                        contentScale = ContentScale.Crop
                    )
                }
                
                if (state.isUploading) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp))
                } else if (state.isUploaded) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "Uploaded",
                        tint = SuccessGreen,
                        modifier = Modifier.align(Alignment.TopEnd).padding(2.dp).size(16.dp)
                    )
                } else if (state.error != null) {
                    IconButton(onClick = onRetryClick) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Retry",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                } else {
                    IconButton(onClick = onUploadClick) {
                        Icon(
                            imageVector = Icons.Default.CloudUpload,
                            contentDescription = "Upload",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
    }
}

fun getDocumentLabel(type: DocumentType): String {
    return when(type) {
        DocumentType.KTP -> "KTP"
        DocumentType.NPWP -> "NPWP (Nomor Pokok Wajib Pajak)"
        DocumentType.KK -> "Kartu Keluarga"
        DocumentType.PAYSLIP -> "Slip Gaji"
        DocumentType.PROOFOFRESIDENCE -> "Bukti Tempat Tinggal"
        DocumentType.BANK_STATEMENT -> "Rekening Koran Bank"
        DocumentType.PROFILE_PICTURE -> "Foto Profil"
        DocumentType.OTHER -> "Dokumen Lainnya"
    }
}

// SuccessGreen color constant
private val SuccessGreen = Color(0xFF4CAF50)
