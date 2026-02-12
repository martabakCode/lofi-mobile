package com.loanfinancial.lofi.ui.features.loan

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.loanfinancial.lofi.ui.components.LofiButton
import com.loanfinancial.lofi.ui.components.LofiTopBar
import com.loanfinancial.lofi.ui.features.loan.model.LoanTnCData
import com.loanfinancial.lofi.ui.features.loan.model.TnCSection

@Composable
fun LoanTnCScreen(
    onNavigateBack: () -> Unit,
    onSubmitSuccess: () -> Unit,
    viewModel: LoanTnCViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val tncData = LoanTnCData()

    Scaffold(
        topBar = {
            LofiTopBar(
                title = "Syarat & Ketentuan",
                canNavigateBack = true,
                navigateUp = onNavigateBack,
            )
        },
        bottomBar = {
            TnCBottomBar(
                isAgreementChecked = uiState.isAgreementChecked,
                isSubmitting = uiState.isSubmitting,
                onAgreementCheckedChange = viewModel::onAgreementCheckedChange,
                onSubmitClick = viewModel::onSubmitClicked,
                agreementText = tncData.agreementText,
            )
        },
    ) { padding ->
        Column(
            modifier =
                Modifier
                    .padding(padding)
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            // Header
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
                    Text(
                        text = tncData.title,
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Mohon baca dengan seksama seluruh syarat dan ketentuan berikut sebelum menyetujui pengajuan pinjaman.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                    )
                }
            }

            // TnC Sections
            tncData.sections.forEach { section ->
                TnCSectionCard(section = section)
            }

            // Important Notice
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors =
                    CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer,
                    ),
            ) {
                Column(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                ) {
                    Text(
                        text = "Penting!",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onErrorContainer,
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Dengan menyetujui syarat dan ketentuan ini, Anda menyatakan bahwa seluruh informasi yang diberikan adalah benar dan akurat. Pemalsuan dokumen merupakan tindak pidana yang dapat dikenakan sanksi hukum.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                    )
                }
            }

            // Spacer for bottom bar
            Spacer(modifier = Modifier.height(100.dp))
        }
    }

    // Success Dialog
    if (uiState.isSuccess) {
        AlertDialog(
            onDismissRequest = { },
            icon = {
                Icon(
                    imageVector = Icons.Filled.CheckCircle,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                )
            },
            title = { Text("Pengajuan Berhasil") },
            text = {
                Text(
                    "Pengajuan pinjaman Anda telah berhasil dikirim. " +
                        "Tim kami akan segera memproses aplikasi Anda. " +
                        "Silakan cek status pengajuan di menu Riwayat Pinjaman.",
                )
            },
            confirmButton = {
                LofiButton(
                    text = "OK",
                    onClick = {
                        viewModel.onDismissSuccess()
                        onSubmitSuccess()
                    },
                )
            },
        )
    }

    // Error Dialog
    uiState.errorMessage?.let { error ->
        AlertDialog(
            onDismissRequest = viewModel::onDismissError,
            title = { Text("Terjadi Kesalahan") },
            text = { Text(error) },
            confirmButton = {
                TextButton(onClick = viewModel::onDismissError) {
                    Text("OK")
                }
            },
        )
    }
}

@Composable
private fun TnCSectionCard(section: TnCSection) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors =
            CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface,
            ),
    ) {
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
        ) {
            Text(
                text = section.title,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.primary,
            )
            Spacer(modifier = Modifier.height(8.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = section.content,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
    }
}

@Composable
private fun TnCBottomBar(
    isAgreementChecked: Boolean,
    isSubmitting: Boolean,
    onAgreementCheckedChange: (Boolean) -> Unit,
    onSubmitClick: () -> Unit,
    agreementText: String,
) {
    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(16.dp),
    ) {
        // Agreement Checkbox
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors =
                CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                ),
        ) {
            Row(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Checkbox(
                    checked = isAgreementChecked,
                    onCheckedChange = onAgreementCheckedChange,
                )
                Text(
                    text = agreementText,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(start = 8.dp),
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Submit Button
        LofiButton(
            text = if (isSubmitting) "Mengirim..." else "Ajukan Pinjaman",
            onClick = onSubmitClick,
            enabled = isAgreementChecked && !isSubmitting,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}
