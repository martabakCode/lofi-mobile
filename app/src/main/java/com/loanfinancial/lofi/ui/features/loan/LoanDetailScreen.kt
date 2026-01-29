package com.loanfinancial.lofi.ui.features.loan

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.loanfinancial.lofi.ui.components.LofiTopBar

@Composable
fun LoanDetailScreen(
    loanId: String,
    navigateUp: () -> Unit,
    viewModel: LoanDetailViewModel =
        androidx.lifecycle.viewmodel.compose
            .viewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(loanId) {
        viewModel.loadLoan(loanId)
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
        Column(
            modifier =
                Modifier
                    .padding(padding)
                    .padding(16.dp)
                    .fillMaxSize(),
        ) {
            Text("Loan ID: ${uiState.id}", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))
            Text("Amount: ${uiState.amount}", style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold))
            Spacer(modifier = Modifier.height(16.dp))
            Text("Status: ${uiState.status}", style = MaterialTheme.typography.bodyLarge)
            Text("Due Date: ${uiState.dueDate}", style = MaterialTheme.typography.bodyLarge)
            Text("Type: ${uiState.type}", style = MaterialTheme.typography.bodyLarge)
        }
    }
}
