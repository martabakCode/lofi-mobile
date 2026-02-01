package com.loanfinancial.lofi.ui.features.loan

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.loanfinancial.lofi.domain.model.Loan
import com.loanfinancial.lofi.ui.components.LofiButton
import com.loanfinancial.lofi.ui.components.LofiTopBar
import com.loanfinancial.lofi.ui.components.SLACountdown
import java.text.NumberFormat
import java.util.Locale

@Composable
fun LoanDetailScreen(
    loanId: String,
    navigateUp: () -> Unit,
    viewModel: LoanDetailViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val submitState by viewModel.submitState.collectAsState()

    LaunchedEffect(loanId) {
        viewModel.loadLoan(loanId)
    }

    // Handle submit state changes
    LaunchedEffect(submitState) {
        when (submitState) {
            is SubmitLoanState.Error -> {
                // Error is shown in UI, will reset after shown
            }
            else -> {}
        }
    }

    Scaffold(
        topBar = {
            LofiTopBar(
                title = "Loan Details",
                canNavigateBack = true,
                navigateUp = navigateUp,
            )
        },
    ) { padding ->
        Box(
            modifier =
                Modifier
                    .padding(padding)
                    .fillMaxSize(),
        ) {
            when (val state = uiState) {
                is LoanDetailUiState.Loading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(androidx.compose.ui.Alignment.Center),
                    )
                }
                is LoanDetailUiState.Error -> {
                    Column(
                        modifier =
                            Modifier
                                .align(androidx.compose.ui.Alignment.Center)
                                .padding(16.dp),
                        horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally,
                    ) {
                        Text(
                            text = "Error: ${state.message}",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.error,
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        LofiButton(
                            text = "Retry",
                            onClick = { viewModel.loadLoan(loanId) },
                        )
                    }
                }
                is LoanDetailUiState.Success -> {
                    LoanDetailContent(
                        loan = state.loan,
                        submitState = submitState,
                        onSubmitClick = { viewModel.submitLoan(loanId) },
                    )
                }
            }
        }
    }
}

@Composable
private fun LoanDetailContent(
    loan: Loan,
    submitState: SubmitLoanState,
    onSubmitClick: () -> Unit,
) {
    val scrollState = rememberScrollState()
    val currencyFormatter = NumberFormat.getCurrencyInstance(Locale("id", "ID"))

    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(16.dp),
    ) {
        // Loan ID
        Text(
            text = "Loan ID",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = loan.id,
            style = MaterialTheme.typography.bodyLarge,
        )
        Spacer(modifier = Modifier.height(16.dp))

        // Amount
        Text(
            text = "Loan Amount",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = currencyFormatter.format(loan.loanAmount),
            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.primary,
        )
        Spacer(modifier = Modifier.height(16.dp))

        // Status Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors =
                CardDefaults.cardColors(
                    containerColor = getStatusColor(loan.loanStatus),
                ),
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Status",
                    style = MaterialTheme.typography.labelMedium,
                )
                Text(
                    text = loan.loanStatusDisplay,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                )
            }
        }
        Spacer(modifier = Modifier.height(16.dp))

        // SLA Countdown
        SLACountdown(
            submittedAt = loan.submittedAt,
            slaDurationHours = loan.slaDurationHours,
        )
        Spacer(modifier = Modifier.height(16.dp))

        // Product Info
        Text(
            text = "Product",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = loan.product.productName,
            style = MaterialTheme.typography.bodyLarge,
        )
        Text(
            text = "Code: ${loan.product.productCode}",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Interest Rate: ${loan.product.interestRate}%",
            style = MaterialTheme.typography.bodyMedium,
        )
        Spacer(modifier = Modifier.height(16.dp))

        // Tenor
        Text(
            text = "Tenor",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = "${loan.tenor} months",
            style = MaterialTheme.typography.bodyLarge,
        )
        Spacer(modifier = Modifier.height(16.dp))

        // Current Stage
        Text(
            text = "Current Stage",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = loan.currentStage,
            style = MaterialTheme.typography.bodyLarge,
        )
        Spacer(modifier = Modifier.height(16.dp))

        // Submitted Date
        loan.submittedAt?.let {
            Text(
                text = "Submitted At",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = it,
                style = MaterialTheme.typography.bodyLarge,
            )
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Customer Info
        Text(
            text = "Customer",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = loan.customerName,
            style = MaterialTheme.typography.bodyLarge,
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Submit Button for DRAFT status
        if (loan.loanStatus == "DRAFT") {
            when (submitState) {
                is SubmitLoanState.Loading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(androidx.compose.ui.Alignment.CenterHorizontally),
                    )
                }
                is SubmitLoanState.Error -> {
                    Column {
                        Text(
                            text = "Error: ${submitState.message}",
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodyMedium,
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        LofiButton(
                            text = "Submit Loan",
                            onClick = onSubmitClick,
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                }
                else -> {
                    LofiButton(
                        text = "Submit Loan",
                        onClick = onSubmitClick,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun getStatusColor(status: String): androidx.compose.ui.graphics.Color =
    when (status) {
        "DRAFT" -> MaterialTheme.colorScheme.surfaceVariant
        "REVIEWED" -> MaterialTheme.colorScheme.primaryContainer
        "APPROVED" -> MaterialTheme.colorScheme.tertiaryContainer
        "DISBURSED" -> MaterialTheme.colorScheme.secondaryContainer
        "REJECTED" -> MaterialTheme.colorScheme.errorContainer
        else -> MaterialTheme.colorScheme.surfaceVariant
    }
