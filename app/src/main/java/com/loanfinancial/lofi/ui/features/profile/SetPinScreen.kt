package com.loanfinancial.lofi.ui.features.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.loanfinancial.lofi.ui.components.LofiButton
import com.loanfinancial.lofi.ui.components.LofiTextField
import com.loanfinancial.lofi.ui.components.NumericKeypad
import com.loanfinancial.lofi.ui.components.PinDot

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SetPinScreen(
    onBackClick: () -> Unit,
    onSuccess: () -> Unit,
    viewModel: SetPinViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(uiState.isSuccess) {
        if (uiState.isSuccess) {
            onSuccess()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        when(uiState.step) {
                            0 -> "Enter Password"
                            1 -> "Set PIN"
                            else -> "Confirm PIN"
                        }
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = { 
                        if (uiState.step > 0) viewModel.onBack() else onBackClick() 
                    }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (uiState.step == 0) {
                 Spacer(modifier = Modifier.height(24.dp))
                 Text(
                     text = "Please enter your password to continue.",
                     style = MaterialTheme.typography.bodyMedium,
                     color = MaterialTheme.colorScheme.onSurfaceVariant,
                     textAlign = TextAlign.Center
                 )
                 Spacer(modifier = Modifier.height(32.dp))
                 
                 LofiTextField(
                     value = uiState.password,
                     onValueChange = { viewModel.onPasswordInput(it) },
                     label = "Password",
                     visualTransformation = PasswordVisualTransformation(),
                     isError = uiState.error != null,
                     errorMessage = uiState.error
                 )
                 
                 Spacer(modifier = Modifier.weight(1f))
                 
                 LofiButton(
                     text = "Continue",
                     onClick = { viewModel.onSubmitPassword() },
                     modifier = Modifier.fillMaxWidth().height(56.dp)
                 )
                 Spacer(modifier = Modifier.height(24.dp))

            } else {
                Spacer(modifier = Modifier.height(40.dp))
                
                Text(
                    text = if (uiState.step == 1) "Create a 6-digit PIN" else "Confirm your 6-digit PIN",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = if (uiState.step == 1) 
                        "This PIN will be used for secure transactions." 
                    else 
                        "Please re-enter your PIN to verify.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
                
                Spacer(modifier = Modifier.height(48.dp))
                
                // PIN Dots
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val currentPinLength = if (uiState.step == 1) uiState.pin.length else uiState.confirmPin.length
                    repeat(6) { index ->
                        PinDot(isFilled = index < currentPinLength)
                    }
                }
                
                if (uiState.error != null) {
                    Spacer(modifier = Modifier.height(24.dp))
                    Text(
                        text = uiState.error!!,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                
                if (uiState.isLoading) {
                    Spacer(modifier = Modifier.height(24.dp))
                    CircularProgressIndicator(modifier = Modifier.size(24.dp))
                }
                
                Spacer(modifier = Modifier.weight(1f))
                
                // Numeric Keypad
                NumericKeypad(
                    onInput = { number -> 
                         val current = if (uiState.step == 1) uiState.pin else uiState.confirmPin
                         viewModel.updatePin(current + number)
                    },
                    onDeleteClick = { 
                        val current = if (uiState.step == 1) uiState.pin else uiState.confirmPin
                        if (current.isNotEmpty()) {
                            viewModel.updatePin(current.dropLast(1))
                        }
                    }
                )
                
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}


