package com.loanfinancial.lofi.ui.features.loan.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.loanfinancial.lofi.core.media.DocumentType
import com.loanfinancial.lofi.ui.features.loan.ApplyLoanUiEvent
import com.loanfinancial.lofi.ui.features.loan.DocumentUploadState

@Composable
fun DocumentUploadSection(
    ktpState: DocumentUploadState?,
    selfieState: DocumentUploadState?,
    onEvent: (ApplyLoanUiEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(
            text = "Required Documents",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )

        DocumentUploadCard(
            title = "KTP (ID Card)",
            description = "Upload a clear photo of your KTP",
            documentType = DocumentType.KTP,
            uploadState = ktpState,
            onEvent = onEvent,
        )

        DocumentUploadCard(
            title = "Selfie with KTP",
            description = "Take a selfie while holding your KTP",
            documentType = DocumentType.SELFIE,
            uploadState = selfieState,
            onEvent = onEvent,
        )
    }
}

@Composable
private fun DocumentUploadCard(
    title: String,
    description: String,
    documentType: DocumentType,
    uploadState: DocumentUploadState?,
    onEvent: (ApplyLoanUiEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    var showSourceDialog by remember { mutableStateOf(false) }

    Card(
        modifier = modifier.fillMaxWidth(),
        colors =
            CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface,
            ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Text(
                        text = description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }

                when {
                    uploadState?.isUploaded == true -> {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Uploaded",
                            tint = Color.Green,
                            modifier = Modifier.size(32.dp),
                        )
                    }
                    uploadState?.isUploading == true -> {
                        CircularProgressIndicator(
                            modifier = Modifier.size(32.dp),
                            strokeWidth = 2.dp,
                        )
                    }
                    uploadState?.error != null -> {
                        Icon(
                            imageVector = Icons.Default.Error,
                            contentDescription = "Error",
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(32.dp),
                        )
                    }
                    else -> {
                        IconButton(onClick = { showSourceDialog = true }) {
                            Icon(
                                imageVector = Icons.Default.AddAPhoto,
                                contentDescription = "Add Document",
                                tint = MaterialTheme.colorScheme.primary,
                            )
                        }
                    }
                }
            }

            if (uploadState?.isUploading == true) {
                Spacer(modifier = Modifier.height(8.dp))
                LinearProgressIndicator(
                    progress = { uploadState.uploadProgress / 100f },
                    modifier = Modifier.fillMaxWidth(),
                )
                Text(
                    text = "${uploadState.uploadProgress}%",
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.align(Alignment.End),
                )
            }

            if (uploadState?.error != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = uploadState.error,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                )
                OutlinedButton(
                    onClick = { onEvent(ApplyLoanUiEvent.DocumentUploadStarted(documentType)) },
                    modifier = Modifier.align(Alignment.End),
                ) {
                    Text("Retry")
                }
            }

            uploadState?.filePath?.let { filePath ->
                Spacer(modifier = Modifier.height(8.dp))
                Box(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .height(120.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .border(
                                width = 1.dp,
                                color = MaterialTheme.colorScheme.outline,
                                shape = RoundedCornerShape(8.dp),
                            ),
                ) {
                    AsyncImage(
                        model = filePath,
                        contentDescription = title,
                        modifier = Modifier.fillMaxWidth(),
                        contentScale = ContentScale.Crop,
                    )

                    if (uploadState.isUploaded) {
                        IconButton(
                            onClick = { onEvent(ApplyLoanUiEvent.DocumentRemoved(documentType)) },
                            modifier =
                                Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(4.dp)
                                    .background(
                                        color = Color.Black.copy(alpha = 0.5f),
                                        shape = RoundedCornerShape(50),
                                    ),
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Remove",
                                tint = Color.White,
                            )
                        }
                    }
                }
            }
        }
    }

    if (showSourceDialog) {
        AlertDialog(
            onDismissRequest = { showSourceDialog = false },
            title = { Text("Select Document Source") },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Button(
                        onClick = {
                            onEvent(ApplyLoanUiEvent.CaptureDocument(documentType))
                            showSourceDialog = false
                        },
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Icon(
                            imageVector = Icons.Default.CameraAlt,
                            contentDescription = null,
                            modifier = Modifier.padding(end = 8.dp),
                        )
                        Text("Take Photo")
                    }
                    OutlinedButton(
                        onClick = {
                            onEvent(ApplyLoanUiEvent.SelectDocumentFromGallery(documentType))
                            showSourceDialog = false
                        },
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Icon(
                            imageVector = Icons.Default.PhotoLibrary,
                            contentDescription = null,
                            modifier = Modifier.padding(end = 8.dp),
                        )
                        Text("Choose from Gallery")
                    }
                }
            },
            confirmButton = { },
            dismissButton = {
                OutlinedButton(onClick = { showSourceDialog = false }) {
                    Text("Cancel")
                }
            },
        )
    }
}
