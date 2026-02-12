package com.loanfinancial.lofi.ui.features.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.loanfinancial.lofi.R
import com.loanfinancial.lofi.domain.model.Loan
import com.loanfinancial.lofi.ui.components.LofiCard

@Composable
fun HomeScreen(
    isGuest: Boolean = false,
    viewModel: HomeViewModel = hiltViewModel(),
    onApplyLoanClick: () -> Unit = {},
    onMyLoansClick: () -> Unit = {},
    onCompleteProfileClick: () -> Unit = {},
    onViewDraftsClick: () -> Unit = {},
) {
    val uiState by viewModel.uiState.collectAsState()

    HomeScreenContent(
        isGuest = isGuest,
        uiState = uiState,
        onRefresh = { viewModel.refreshLoans() },
        onApplyLoanClick = onApplyLoanClick,
        onMyLoansClick = onMyLoansClick,
        onCompleteProfileClick = onCompleteProfileClick,
        onViewDraftsClick = onViewDraftsClick,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreenContent(
    isGuest: Boolean = false,
    uiState: HomeUiState,
    onRefresh: () -> Unit,
    onApplyLoanClick: () -> Unit = {},
    onMyLoansClick: () -> Unit = {},
    onCompleteProfileClick: () -> Unit = {},
    onViewDraftsClick: () -> Unit = {},
) {
    PullToRefreshBox(
        isRefreshing = uiState.isRefreshing,
        onRefresh = onRefresh,
        modifier = Modifier.fillMaxSize(),
    ) {
        LazyColumn(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            item {
                Text(
                    text = if (isGuest) "Selamat Datang di LoFi" else stringResource(R.string.hi_user, uiState.userProfile?.fullName ?: "User"),
                    style =
                        MaterialTheme.typography.headlineLarge.copy(
                            fontWeight = FontWeight.Bold,
                        ),
                    color = MaterialTheme.colorScheme.onBackground,
                )
            }

            // 💳 Loan Summary Card or Complete Profile Card
            item {
                if (isGuest) {
                    LofiCard(
                        modifier = Modifier.fillMaxWidth().clickable { onCompleteProfileClick() },
                    ) {
                        Column(
                            modifier =
                                Modifier
                                    .background(MaterialTheme.colorScheme.primary)
                                    .padding(20.dp)
                                    .fillMaxWidth(),
                        ) {
                            Text(
                                text = "Mulai Perjalanan Finansial Anda",
                                color = Color.White.copy(alpha = 0.8f),
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Daftar sekarang untuk melihat limit pinjaman Anda dan mulai mengajukan pinjaman dengan bunga rendah.",
                                color = Color.White.copy(alpha = 0.9f),
                                style = MaterialTheme.typography.bodyMedium,
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(
                                onClick = onCompleteProfileClick,
                                colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = MaterialTheme.colorScheme.primary),
                            ) {
                                Text("Daftar Sekarang")
                            }
                        }
                    }
                } else {
                    val availableProduct = uiState.availableProduct
                    if (uiState.isProfileCompleted && availableProduct != null) {
                        // Show Plafond
                        LofiCard(
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Column(
                                modifier =
                                    Modifier
                                        .background(MaterialTheme.colorScheme.primary)
                                        .padding(20.dp)
                                        .fillMaxWidth(),
                            ) {
                                Text(
                                    text = stringResource(R.string.loan_limit),
                                    color = Color.White.copy(alpha = 0.8f),
                                    style = MaterialTheme.typography.titleSmall,
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                val displayedAvailable = availableProduct.productLimit - availableProduct.approvedLoanAmount
                                Text(
                                    text = "Rp ${String.format("%,.0f", displayedAvailable)}",
                                    fontSize = 32.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                )

                                Spacer(modifier = Modifier.height(16.dp))

                                Text(
                                    text = stringResource(R.string.used, "Rp ${String.format("%,.0f", availableProduct.approvedLoanAmount)}"),
                                    color = Color.White.copy(alpha = 0.9f),
                                    style = MaterialTheme.typography.bodyMedium,
                                )
                            }
                        }
                    } else if (uiState.hasCompletedFirstSession && !uiState.isProfileCompleted) {
                        // Show Complete Profile only after registration and PIN setup is complete
                        LofiCard(
                            modifier = Modifier.fillMaxWidth().clickable { onCompleteProfileClick() },
                        ) {
                            Column(
                                modifier =
                                    Modifier
                                        .background(MaterialTheme.colorScheme.secondaryContainer)
                                        .padding(20.dp)
                                        .fillMaxWidth(),
                            ) {
                                Text(
                                    text = stringResource(R.string.complete_profile_title),
                                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = stringResource(R.string.complete_profile_desc),
                                    color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.8f),
                                    style = MaterialTheme.typography.bodyMedium,
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Button(
                                    onClick = onCompleteProfileClick,
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                                ) {
                                    Text(stringResource(R.string.complete_profile_cta))
                                }
                            }
                        }
                    }
                }
            }

            // 🚀 Quick Actions
            if (!isGuest) {
                item {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        val availableProduct = uiState.availableProduct
                        val canApply = (availableProduct?.productLimit ?: 0.0) - (availableProduct?.approvedLoanAmount ?: 0.0) >= 100000.0 // Min amount check? Let's say > 0.
                        HomeActionButton(stringResource(R.string.apply_loan), Modifier.weight(1f), enabled = canApply, onClick = onApplyLoanClick)
                        HomeActionButton(stringResource(R.string.my_loans), Modifier.weight(1f), onClick = onMyLoansClick)
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        HomeActionButton("View Drafts", Modifier.weight(1f), onClick = onViewDraftsClick)
                        HomeActionButton(stringResource(R.string.complete_profile_cta), Modifier.weight(1f), onClick = onCompleteProfileClick)
                    }
                }
            }

            // 🎁 Product Catalog
            if (uiState.products.isNotEmpty()) {
                item {
                    Text(
                        text = "Explore Our Products",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    )
                }
                item {
                    val availableProduct = uiState.availableProduct
                    val canApply = (availableProduct?.productLimit ?: 0.0) - (availableProduct?.approvedLoanAmount ?: 0.0) >= 100000.0
                    androidx.compose.foundation.lazy.LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        contentPadding = PaddingValues(bottom = 8.dp),
                    ) {
                        items(uiState.products) { product ->
                            ProductCard(product = product, enabled = canApply, onClick = onApplyLoanClick)
                        }
                    }
                }
            }

            // 📄 Active Loans Title
            if (!isGuest) {
                item {
                    Text(
                        text = stringResource(R.string.active_loan),
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    )
                }

                if (uiState.isLoading) {
                    item {
                        Box(modifier = Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator()
                        }
                    }
                } else if (uiState.error != null) {
                    item {
                        Text(text = uiState.error, color = MaterialTheme.colorScheme.error)
                    }
                } else if (uiState.loans.isEmpty()) {
                    item {
                        Text(text = stringResource(R.string.no_active_loans), color = Color.Gray)
                    }
                } else {
                    items(uiState.loans.take(2)) { loan ->
                        LoanItem(loan = loan)
                    }
                }
            }
        }
    }
}

@Composable
fun LoanItem(loan: Loan) {
    LofiCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Column {
                    Text(text = loan.product.productName, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                    Text(text = "ID: ${loan.id.take(8)}...", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                }
                Badge(
                    containerColor =
                        when (loan.loanStatus) {
                            "APPROVED" -> Color(0xFF4CAF50)
                            "DISBURSED" -> Color(0xFF2196F3)
                            "REVIEWED" -> Color(0xFFFFC107)
                            else -> Color.LightGray
                        },
                ) {
                    Text(text = loan.loanStatusDisplay, color = Color.White, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Column {
                    Text(stringResource(R.string.type), style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                    Text(loan.product.productCode, style = MaterialTheme.typography.bodyLarge)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(stringResource(R.string.next_due), style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                    Text(loan.submittedAt?.take(10) ?: "-", style = MaterialTheme.typography.bodyLarge)
                }
            }
        }
    }
}

@Composable
fun ProductCard(
    product: com.loanfinancial.lofi.data.model.dto.ProductDto,
    onClick: () -> Unit,
    enabled: Boolean = true,
) {
    Box(
        modifier =
            Modifier
                .width(160.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(if (enabled) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.surfaceVariant)
                .then(if (enabled) Modifier.clickable { onClick() } else Modifier)
                .padding(16.dp),
    ) {
        Column {
            Box(
                modifier =
                    Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = product.productCode.take(1),
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    fontWeight = FontWeight.Bold,
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = product.productName,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
            )
            Text(
                text = "Up to Rp ${String.format("%,.0f", product.maxLoanAmount ?: 0.0)}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "${product.interestRate}% Interest",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.SemiBold,
            )
        }
    }
}

@Composable
fun HomeActionButton(
    title: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    onClick: () -> Unit = {},
) {
    Box(
        modifier =
            modifier
                .clip(RoundedCornerShape(16.dp))
                .background(if (enabled) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.surfaceVariant)
                .then(if (enabled) Modifier.clickable { onClick() } else Modifier)
                .padding(vertical = 16.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
            color = if (enabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
