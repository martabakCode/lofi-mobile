package com.loanfinancial.lofi.ui.features.loan.draft

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.loanfinancial.lofi.domain.model.DraftStep
import com.loanfinancial.lofi.domain.model.LoanDraft
import com.loanfinancial.lofi.ui.components.LofiTopBar
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun DraftListScreen(
    navigateUp: () -> Unit,
    onDraftClick: (String) -> Unit,
    viewModel: DraftListViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var draftToDelete by remember { mutableStateOf<LoanDraft?>(null) }

    Scaffold(
        topBar = {
            LofiTopBar(
                title = "My Drafts",
                canNavigateBack = true,
                navigateUp = navigateUp,
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when {
                uiState.isLoading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                uiState.drafts.isEmpty() -> {
                    EmptyDraftsState(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                else -> {
                    LazyColumn(
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        items(
                            items = uiState.drafts,
                            key = { it.id }
                        ) { draft ->
                            DraftCard(
                                draft = draft,
                                isDeleting = uiState.deletingDraftId == draft.id,
                                onContinueClick = { onDraftClick(draft.id) },
                                onDeleteClick = { draftToDelete = draft }
                            )
                        }
                    }
                }
            }
        }
    }

    // Delete confirmation dialog
    draftToDelete?.let { draft ->
        DeleteConfirmationDialog(
            draft = draft,
            onConfirm = {
                viewModel.deleteDraft(draft.id)
                draftToDelete = null
            },
            onDismiss = { draftToDelete = null }
        )
    }
}

@Composable
fun EmptyDraftsState(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = "No Drafts",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Your saved loan applications will appear here",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
fun DraftCard(
    draft: LoanDraft,
    isDeleting: Boolean,
    onContinueClick: () -> Unit,
    onDeleteClick: () -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = !isDeleting) { onContinueClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Draft Info
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = draft.purpose ?: "Loan Application",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold
                    ),
                )
                Spacer(modifier = Modifier.height(4.dp))
                if (draft.amount != null) {
                    Text(
                        text = "Rp ${String.format("%,.0f", draft.amount)}",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Medium,
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Row {
                    StepBadge(step = draft.currentStep)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = formatDate(draft.updatedAt),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            // Actions
            if (isDeleting) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    strokeWidth = 2.dp,
                )
            } else {
                Row {
                    IconButton(onClick = onContinueClick) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Continue",
                            tint = MaterialTheme.colorScheme.primary,
                        )
                    }
                    IconButton(onClick = onDeleteClick) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete",
                            tint = MaterialTheme.colorScheme.error,
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun StepBadge(step: DraftStep) {
    val stepText = when (step) {
        DraftStep.BASIC_INFO -> "Basic Info"
        DraftStep.EMPLOYMENT_INFO -> "Employment"
        DraftStep.EMERGENCY_CONTACT -> "Emergency Contact"
        DraftStep.BANK_INFO -> "Bank Info"
        DraftStep.DOCUMENTS -> "Documents"
        DraftStep.PREVIEW -> "Preview"
        DraftStep.TNC -> "Terms"
        DraftStep.COMPLETED -> "Completed"
    }

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(4.dp))
            .background(MaterialTheme.colorScheme.primaryContainer)
            .padding(horizontal = 8.dp, vertical = 2.dp),
    ) {
        Text(
            text = stepText,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onPrimaryContainer,
        )
    }
}

@Composable
fun DeleteConfirmationDialog(
    draft: LoanDraft,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Delete Draft") },
        text = {
            Text(
                "Are you sure you want to delete this draft? " +
                "This action cannot be undone."
            )
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("Delete", color = MaterialTheme.colorScheme.error)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        },
    )
}

private fun formatDate(timestamp: Long): String {
    val sdf = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault())
    return sdf.format(Date(timestamp))
}
