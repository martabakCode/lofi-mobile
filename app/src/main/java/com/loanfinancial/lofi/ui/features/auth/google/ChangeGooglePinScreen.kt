package com.loanfinancial.lofi.ui.features.auth.google

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.loanfinancial.lofi.ui.components.NumericKeypad
import com.loanfinancial.lofi.ui.components.PinDot

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChangeGooglePinScreen(
    onBackClick: () -> Unit,
    onSuccess: () -> Unit,
    viewModel: ChangeGooglePinViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    
    LaunchedEffect(uiState.isSuccess) {
        if (uiState.isSuccess) {
            onSuccess()
        }
    }
    
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(text = "Change PIN") },
                navigationIcon = {
                    IconButton(onClick = { 
                        if (uiState.step > 0) viewModel.onBackClick() else onBackClick() 
                    }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = when (uiState.step) {
                        0 -> "Enter Current PIN"
                        1 -> "Enter New PIN"
                        else -> "Confirm New PIN"
                    },
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
                
                Spacer(modifier = Modifier.height(32.dp))
                
                // Pin Display
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val currentPin = when (uiState.step) {
                        0 -> uiState.oldPin
                        1 -> uiState.newPin
                        else -> uiState.confirmPin
                    }
                    val currentPinLength = currentPin.length
                    repeat(6) { index ->
                        PinDot(isFilled = index < currentPinLength)
                    }
                }
                
                if (uiState.error != null) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = uiState.error!!,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
            
            NumericKeypad(
                onInput = { key -> 
                    val current = when (uiState.step) {
                        0 -> uiState.oldPin
                        1 -> uiState.newPin
                        else -> uiState.confirmPin
                    }
                    viewModel.updatePin(current + key) 
                },
                onDeleteClick = {
                    val current = when (uiState.step) {
                        0 -> uiState.oldPin
                        1 -> uiState.newPin
                        else -> uiState.confirmPin
                    }
                    if (current.isNotEmpty()) {
                        viewModel.updatePin(current.dropLast(1))
                    }
                }
            )
        }
    }
    
    if (uiState.isLoading) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
    }
}
