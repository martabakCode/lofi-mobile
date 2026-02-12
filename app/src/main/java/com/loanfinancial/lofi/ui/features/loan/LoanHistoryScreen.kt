package com.loanfinancial.lofi.ui.features.loan

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material3.Badge
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.loanfinancial.lofi.R
import com.loanfinancial.lofi.domain.model.Loan
import com.loanfinancial.lofi.domain.model.PendingSubmissionStatus
import com.loanfinancial.lofi.ui.components.LofiCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoanHistoryScreen(
    isGuest: Boolean = false,
    onLoanClick: (String) -> Unit = {},
    viewModel: LoanHistoryViewModel = hiltViewModel(),
) {
    if (isGuest) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.List,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = stringResource(R.string.login_to_view_history),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
        }
        return
    }

    val uiState by viewModel.uiState.collectAsState()
    val listState = rememberLazyListState()

    // Detect when reached end of list for pagination
    val shouldLoadMore by remember {
        derivedStateOf {
            val lastVisibleItem =
                listState.layoutInfo.visibleItemsInfo.lastOrNull()
                    ?: return@derivedStateOf false

            lastVisibleItem.index >= listState.layoutInfo.totalItemsCount - 2
        }
    }

    LaunchedEffect(shouldLoadMore) {
        if (shouldLoadMore) {
            viewModel.loadMore()
        }
    }

    PullToRefreshBox(
        isRefreshing = uiState.isRefreshing,
        onRefresh = { viewModel.refresh() },
        modifier = Modifier.fillMaxSize(),
    ) {
        LazyColumn(
            state = listState,
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            item {
                Text(
                    text = stringResource(R.string.loan_history),
                    style = MaterialTheme.typography.displaySmall.copy(fontWeight = FontWeight.Bold),
                    modifier = Modifier.padding(bottom = 8.dp),
                )
            }

            if (uiState.isLoading && uiState.loans.isEmpty()) {
                item {
                    Box(modifier = Modifier.fillParentMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
            } else if (uiState.error != null && uiState.loans.isEmpty()) {
                item {
                    Text(text = uiState.error!!, color = MaterialTheme.colorScheme.error)
                }
            } else {
                items(uiState.loans) { loan ->
                    LoanHistoryItem(
                        loan = loan,
                        onClick = { onLoanClick(loan.id) },
                        onRetry = { viewModel.retrySubmission(loan.id) },
                        onCancel = { viewModel.cancelSubmission(loan.id) },
                    )
                }

                if (uiState.isLoadingMore) {
                    item {
                        Box(modifier = Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(modifier = Modifier.size(32.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun LoanHistoryItem(
    loan: Loan,
    onClick: () -> Unit = {},
    onRetry: () -> Unit = {},
    onCancel: () -> Unit = {},
) {
    LofiCard(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = loan.product.productName,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                )
                Badge(
                    containerColor =
                        when {
                            loan.pendingStatus != null -> {
                                when (loan.pendingStatus) {
                                    PendingSubmissionStatus.PENDING -> Color(0xFFFFC107) // Amber
                                    PendingSubmissionStatus.SUBMITTING -> Color(0xFF2196F3) // Blue
                                    PendingSubmissionStatus.SUCCESS -> Color(0xFF4CAF50) // Green
                                    PendingSubmissionStatus.FAILED -> Color(0xFFF44336) // Red
                                    PendingSubmissionStatus.CANCELLED -> Color.Gray
                                    else -> Color.Gray
                                }
                            }
                            loan.loanStatus == "APPROVED" -> Color(0xFF4CAF50)
                            loan.loanStatus == "DISBURSED" -> Color(0xFF2196F3)
                            else -> Color.Gray
                        },
                ) {
                    Text(text = loan.loanStatusDisplay, color = Color.White, modifier = Modifier.padding(horizontal = 4.dp))
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = stringResource(R.string.amount_label, "Rp ${loan.loanAmount}"), style = MaterialTheme.typography.bodyMedium)
            Text(text = stringResource(R.string.date_label, loan.submittedAt?.take(10) ?: "-"), style = MaterialTheme.typography.bodySmall, color = Color.Gray)

            if (loan.pendingStatus == PendingSubmissionStatus.FAILED || loan.pendingStatus == PendingSubmissionStatus.PENDING) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                ) {
                    if (loan.pendingStatus == PendingSubmissionStatus.FAILED) {
                        TextButton(onClick = onRetry) {
                            Text("Coba Lagi")
                        }
                    }
                    TextButton(onClick = onCancel) {
                        Text(if (loan.pendingStatus == PendingSubmissionStatus.FAILED) "Hapus" else "Batalkan", color = MaterialTheme.colorScheme.error)
                    }
                }
            }
        }
    }
}
