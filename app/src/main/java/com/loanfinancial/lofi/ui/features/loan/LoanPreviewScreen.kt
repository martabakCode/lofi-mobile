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
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.loanfinancial.lofi.core.media.DocumentType
import com.loanfinancial.lofi.ui.components.LofiButton
import com.loanfinancial.lofi.ui.components.LofiTopBar
import com.loanfinancial.lofi.ui.features.loan.model.LoanPreviewData
import java.text.NumberFormat
import java.util.Locale

@Composable
fun LoanPreviewScreen(
    previewData: LoanPreviewData,
    onNavigateToTnC: () -> Unit,
    onNavigateBack: () -> Unit,
) {
    val currencyFormatter = NumberFormat.getCurrencyInstance(Locale.forLanguageTag("id-ID"))

    Scaffold(
        topBar = {
            LofiTopBar(
                title = "Preview Pengajuan Pinjaman",
                canNavigateBack = true,
                navigateUp = onNavigateBack,
            )
        },
        bottomBar = {
            Column(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
            ) {
                LofiButton(
                    text = "Ajukan Pinjaman",
                    onClick = onNavigateToTnC,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
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
            // Loan Summary Card
            LoanSummaryCard(
                amount = previewData.amountValue,
                tenor = previewData.tenorValue,
                purpose = previewData.purpose,
                currencyFormatter = currencyFormatter,
            )

            // Installment Calculation Card
            InstallmentCalculationCard(
                previewData = previewData,
                currencyFormatter = currencyFormatter,
            )

            // Documents Status Card
            DocumentsStatusCard(
                documents = previewData.documents,
            )

            // Verification Status Card
            VerificationStatusCard(
                isBiometricVerified = previewData.isBiometricVerified,
                latitude = previewData.latitude,
                longitude = previewData.longitude,
            )
        }
    }
}

@Composable
private fun LoanSummaryCard(
    amount: Long,
    tenor: Int,
    purpose: String,
    currencyFormatter: NumberFormat,
) {
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
                    imageVector = Icons.Default.Payments,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                )
                Spacer(modifier = Modifier.padding(start = 8.dp))
                Text(
                    text = "Ringkasan Pengajuan",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.primary,
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Amount
            SummaryRow(
                label = "Jumlah Pinjaman",
                value = currencyFormatter.format(amount),
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Tenor
            SummaryRow(
                label = "Tenor",
                value = "$tenor bulan",
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Purpose
            SummaryRow(
                label = "Tujuan Pinjaman",
                value = purpose,
            )
        }
    }
}

@Composable
private fun InstallmentCalculationCard(
    previewData: LoanPreviewData,
    currencyFormatter: NumberFormat,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors =
            CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant,
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
                    imageVector = Icons.Default.Schedule,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.secondary,
                )
                Spacer(modifier = Modifier.padding(start = 8.dp))
                Text(
                    text = "Estimasi Perhitungan",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.secondary,
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Interest Rate
            SummaryRow(
                label = "Suku Bunga",
                value = "${previewData.interestRate}% per tahun",
                isSecondary = true,
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Admin Fee
            SummaryRow(
                label = "Biaya Administrasi",
                value = currencyFormatter.format(previewData.adminFee),
                isSecondary = true,
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Provision Fee
            SummaryRow(
                label = "Biaya Provisi (1%)",
                value = currencyFormatter.format(previewData.provisionFee),
                isSecondary = true,
            )

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 12.dp),
                color = MaterialTheme.colorScheme.outlineVariant,
            )

            // Total Interest
            SummaryRow(
                label = "Total Bunga",
                value = currencyFormatter.format(previewData.totalInterest),
                isSecondary = true,
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Total Payment
            SummaryRow(
                label = "Total Pembayaran",
                value = currencyFormatter.format(previewData.totalPayment),
                isSecondary = true,
            )

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 12.dp),
                color = MaterialTheme.colorScheme.outlineVariant,
            )

            // Monthly Installment (Highlighted)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "Estimasi Cicilan/Bulan",
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = currencyFormatter.format(previewData.estimatedMonthlyInstallment),
                    style =
                        MaterialTheme.typography.headlineSmall.copy(
                            fontWeight = FontWeight.Bold,
                        ),
                    color = MaterialTheme.colorScheme.primary,
                )
            }
        }
    }
}

@Composable
private fun DocumentsStatusCard(
    documents: Map<DocumentType, com.loanfinancial.lofi.ui.features.loan.model.DocumentPreviewInfo>,
) {
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
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Icon(
                    imageVector = Icons.Default.Description,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.tertiary,
                )
                Spacer(modifier = Modifier.padding(start = 8.dp))
                Text(
                    text = "Status Dokumen",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.tertiary,
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // KTP Status
            DocumentStatusRow(
                label = "KTP",
                isUploaded = documents[DocumentType.KTP]?.isUploaded == true,
            )
        }
    }
}

@Composable
private fun VerificationStatusCard(
    isBiometricVerified: Boolean,
    latitude: Double?,
    longitude: Double?,
) {
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
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.tertiary,
                )
                Spacer(modifier = Modifier.padding(start = 8.dp))
                Text(
                    text = "Status Verifikasi",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.tertiary,
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Biometric Status
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Fingerprint,
                        contentDescription = null,
                        tint =
                            if (isBiometricVerified) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.outline
                            },
                    )
                    Spacer(modifier = Modifier.padding(start = 8.dp))
                    Text(
                        text = "Verifikasi Biometrik",
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
                Text(
                    text = if (isBiometricVerified) "Terverifikasi" else "Opsional",
                    style =
                        MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Medium,
                        ),
                    color =
                        if (isBiometricVerified) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.outline
                        },
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Location Status
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = null,
                        tint =
                            if (latitude != null && longitude != null) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.outline
                            },
                    )
                    Spacer(modifier = Modifier.padding(start = 8.dp))
                    Text(
                        text = "Lokasi",
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
                Text(
                    text =
                        if (latitude != null && longitude != null) {
                            "Tercatat"
                        } else {
                            "Opsional"
                        },
                    style =
                        MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Medium,
                        ),
                    color =
                        if (latitude != null && longitude != null) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.outline
                        },
                )
            }

            if (latitude != null && longitude != null) {
                Text(
                    text = "Lat: %.4f, Lng: %.4f".format(latitude, longitude),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(start = 32.dp, top = 4.dp),
                )
            }
        }
    }
}

@Composable
private fun SummaryRow(
    label: String,
    value: String,
    isSecondary: Boolean = false,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = label,
            style =
                if (isSecondary) {
                    MaterialTheme.typography.bodyMedium
                } else {
                    MaterialTheme.typography.bodyLarge
                },
            color =
                if (isSecondary) {
                    MaterialTheme.colorScheme.onSurfaceVariant
                } else {
                    MaterialTheme.colorScheme.onSurface
                },
        )
        Text(
            text = value,
            style =
                if (isSecondary) {
                    MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium)
                } else {
                    MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold)
                },
            color =
                if (isSecondary) {
                    MaterialTheme.colorScheme.onSurface
                } else {
                    MaterialTheme.colorScheme.onPrimaryContainer
                },
        )
    }
}

@Composable
private fun DocumentStatusRow(
    label: String,
    isUploaded: Boolean,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
        )
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector =
                    if (isUploaded) {
                        Icons.Default.CheckCircle
                    } else {
                        Icons.Default.CheckCircle
                    },
                contentDescription = null,
                tint =
                    if (isUploaded) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.outline
                    },
            )
            Spacer(modifier = Modifier.padding(start = 4.dp))
            Text(
                text = if (isUploaded) "Sudah Upload" else "Belum Upload",
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                color =
                    if (isUploaded) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.outline
                    },
            )
        }
    }
}
