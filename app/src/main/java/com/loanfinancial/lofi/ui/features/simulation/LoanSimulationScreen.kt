package com.loanfinancial.lofi.ui.features.simulation

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.loanfinancial.lofi.ui.components.LofiCard
import java.text.NumberFormat
import java.util.*

@Composable
fun LoanSimulationScreen(
    viewModel: LoanSimulationViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val scrollState = rememberScrollState()
    val rupiahFormat =
        NumberFormat.getCurrencyInstance(Locale.forLanguageTag("id-ID")).apply {
            maximumFractionDigits = 0
        }

    Scaffold { padding ->
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp)
                    .verticalScroll(scrollState),
        ) {
            Text(
                text = "Loan Calculator",
                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onBackground,
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Estimate your monthly payments simply.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Spacer(modifier = Modifier.height(32.dp))

            if (uiState.isLoading) {
                Box(modifier = Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                // Product Tag
                uiState.selectedProduct?.let { product ->
                    Surface(
                        color = MaterialTheme.colorScheme.primaryContainer,
                        shape = MaterialTheme.shapes.small,
                        modifier = Modifier.padding(bottom = 16.dp),
                    ) {
                        Text(
                            text = "Product: ${product.productName}",
                            style = MaterialTheme.typography.labelMedium,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                        )
                    }
                }

                // Loan Amount Slider
                Text(
                    text = "Loan Amount",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text = rupiahFormat.format(uiState.loanAmount),
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                )
                Slider(
                    value = uiState.loanAmount.toFloat(),
                    onValueChange = { viewModel.onLoanAmountChange(it.toDouble()) },
                    valueRange = uiState.minAmount.toFloat()..uiState.maxAmount.toFloat(),
                    steps = 0, // Continuous
                    modifier = Modifier.fillMaxWidth(),
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(rupiahFormat.format(uiState.minAmount), style = MaterialTheme.typography.bodySmall)
                    Text(rupiahFormat.format(uiState.maxAmount), style = MaterialTheme.typography.bodySmall)
                }

                Spacer(modifier = Modifier.height(32.dp))

                // Tenor Slider
                Text(
                    text = "Tenor (Months)",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text = "${uiState.durationMonths} Months",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                )
                Slider(
                    value = uiState.durationMonths.toFloat(),
                    onValueChange = { viewModel.onDurationChange(it.toInt()) },
                    valueRange = uiState.minTenor.toFloat()..uiState.maxTenor.toFloat(),
                    steps = (uiState.maxTenor - uiState.minTenor),
                    modifier = Modifier.fillMaxWidth(),
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text("${uiState.minTenor} Mo", style = MaterialTheme.typography.bodySmall)
                    Text("${uiState.maxTenor} Mo", style = MaterialTheme.typography.bodySmall)
                }

                Spacer(modifier = Modifier.height(40.dp))

                // Result Card
                uiState.estimatedPayment?.let { payment ->
                    LofiCard(
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Column(modifier = Modifier.padding(24.dp)) {
                            Text(
                                text = "Estimated Monthly Payment",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = rupiahFormat.format(payment),
                                style =
                                    MaterialTheme.typography.headlineLarge.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary,
                                        letterSpacing = (-1).sp,
                                    ),
                            )
                            Spacer(modifier = Modifier.height(16.dp))

                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

                            Spacer(modifier = Modifier.height(16.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                            ) {
                                Text("Interest Rate", style = MaterialTheme.typography.bodyMedium)
                                Text("${uiState.selectedProduct?.interestRate}% / Year", fontWeight = FontWeight.Bold)
                            }

                            uiState.selectedProduct?.adminFee?.let { fee ->
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                ) {
                                    Text("Admin Fee", style = MaterialTheme.typography.bodyMedium)
                                    Text(rupiahFormat.format(fee), fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "*This calculation is for simulation purposes only. Actual results may vary.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.outline,
                    modifier = Modifier.padding(horizontal = 8.dp),
                )
            }
        }
    }
}
