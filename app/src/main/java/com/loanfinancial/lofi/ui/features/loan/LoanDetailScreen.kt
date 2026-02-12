package com.loanfinancial.lofi.ui.features.loan

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.loanfinancial.lofi.domain.model.Loan
import com.loanfinancial.lofi.domain.model.LoanStep
import com.loanfinancial.lofi.domain.model.formatLoanDate
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
    val currencyFormatter = NumberFormat.getCurrencyInstance(Locale.forLanguageTag("id-ID"))

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

        // Loan Progress Steps
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface,
            ),
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Progress Pengajuan",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.primary,
                )
                Spacer(modifier = Modifier.height(16.dp))
                LoanStepTracker(steps = loan.getLoanSteps())
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
        "SUBMITTED" -> MaterialTheme.colorScheme.primaryContainer
        "REVIEWED", "IN_REVIEW" -> MaterialTheme.colorScheme.primaryContainer
        "APPROVED" -> MaterialTheme.colorScheme.tertiaryContainer
        "DISBURSED" -> MaterialTheme.colorScheme.secondaryContainer
        "REJECTED" -> MaterialTheme.colorScheme.errorContainer
        else -> MaterialTheme.colorScheme.surfaceVariant
    }

@Composable
private fun LoanStepTracker(steps: List<LoanStep>) {
    Column(modifier = Modifier.fillMaxWidth()) {
        steps.forEachIndexed { index, step ->
            LoanStepItem(
                step = step,
                isLast = index == steps.size - 1
            )
        }
    }
}

@Composable
private fun LoanStepItem(step: LoanStep, isLast: Boolean) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Timeline column with icon and line
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.width(32.dp)
        ) {
            // Step icon
            StepIcon(step = step)
            
            // Connector line (if not last)
            if (!isLast) {
                Box(
                    modifier = Modifier
                        .width(2.dp)
                        .height(40.dp)
                        .background(
                            if (step.isCompleted) 
                                MaterialTheme.colorScheme.primary 
                            else 
                                MaterialTheme.colorScheme.outlineVariant
                        )
                )
            }
        }
        
        // Step content
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(bottom = if (isLast) 0.dp else 16.dp)
        ) {
            Text(
                text = step.label,
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontWeight = if (step.isCurrent) FontWeight.Bold else FontWeight.Normal
                ),
                color = when {
                    step.isCompleted || step.isCurrent -> MaterialTheme.colorScheme.onSurface
                    else -> MaterialTheme.colorScheme.onSurfaceVariant
                }
            )
            
            // Show date if available
            step.date?.formatLoanDate()?.let { formattedDate ->
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.CalendarToday,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = formattedDate,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
            
            // Show pending/waiting text if no date and not completed
            if (step.date == null && !step.isCompleted) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = if (step.isCurrent) "Sedang diproses..." else "Menunggu...",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun StepIcon(step: LoanStep) {
    val backgroundColor = when {
        step.isCompleted -> MaterialTheme.colorScheme.primary
        step.isCurrent -> MaterialTheme.colorScheme.primaryContainer
        else -> MaterialTheme.colorScheme.outlineVariant
    }
    
    val iconColor = when {
        step.isCompleted -> MaterialTheme.colorScheme.onPrimary
        step.isCurrent -> MaterialTheme.colorScheme.primary
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }
    
    val icon = when {
        step.status == "SUBMITTED" -> Icons.Default.Send
        step.status == "REVIEWED" -> Icons.Default.Search
        step.status == "APPROVED" -> 
            if (step.date != null && step.label.contains("Ditolak")) 
                Icons.Default.Cancel 
            else 
                Icons.Default.CheckCircle
        step.status == "DISBURSED" -> Icons.Default.AccountBalanceWallet
        else -> Icons.Default.RadioButtonUnchecked
    }
    
    Box(
        modifier = Modifier
            .size(32.dp)
            .clip(CircleShape)
            .background(backgroundColor),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = step.label,
            tint = iconColor,
            modifier = Modifier.size(18.dp)
        )
    }
}
