package com.loanfinancial.lofi.ui.features.profile

import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.loanfinancial.lofi.ui.components.LofiButton
import com.loanfinancial.lofi.ui.components.LofiDatePicker
import com.loanfinancial.lofi.ui.components.LofiDropdown
import com.loanfinancial.lofi.ui.components.LofiTextField

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CompleteProfileScreen(
    onCompleteSuccess: () -> Unit,
    onBackClick: () -> Unit,
    viewModel: CompleteProfileViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(uiState.isSuccess) {
        if (uiState.isSuccess) {
            onCompleteSuccess()
        }
    }
    
    BackHandler {
        if (uiState.currentStep > 1) {
            viewModel.onEvent(CompleteProfileEvent.PreviousStep)
        } else {
            onBackClick()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Complete Profile") },
                navigationIcon = {
                    IconButton(onClick = {
                        if (uiState.currentStep > 1) {
                            viewModel.onEvent(CompleteProfileEvent.PreviousStep)
                        } else {
                            onBackClick()
                        }
                    }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Step Indicator - 3 steps: Personal, Financial, Address
            CompleteProfileStepIndicator(currentStep = uiState.currentStep, totalSteps = 3)
            
            AnimatedContent(
                targetState = uiState.currentStep,
                label = "ProfileSteps",
                modifier = Modifier.fillMaxSize()
            ) { step ->
                when (step) {
                    1 -> PersonalInfoStep(uiState, viewModel)
                    2 -> FinancialInfoStep(uiState, viewModel)
                    3 -> AddressInfoStep(uiState, viewModel)
                }
            }
        }
    }
}

@Composable
fun CompleteProfileStepIndicator(currentStep: Int, totalSteps: Int = 3) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(totalSteps) { index ->
            val stepNumber = index + 1 // Steps are 1-based
            val isCurrent = stepNumber == currentStep
            val isCompleted = stepNumber < currentStep
            
            Box(
                modifier = Modifier
                    .size(if (isCurrent) 12.dp else 8.dp)
                    .clip(CircleShape)
                    .background(
                        when {
                            isCompleted || isCurrent -> MaterialTheme.colorScheme.primary
                            else -> MaterialTheme.colorScheme.outlineVariant
                        }
                    )
            )
            
            if (index < totalSteps - 1) {
                Spacer(modifier = Modifier.width(8.dp))
                Box(
                    modifier = Modifier
                        .width(40.dp)
                        .height(2.dp)
                        .background(
                            if (isCompleted) MaterialTheme.colorScheme.primary 
                            else MaterialTheme.colorScheme.outlineVariant
                        )
                )
                Spacer(modifier = Modifier.width(8.dp))
            }
        }
    }
}

@Composable
fun PersonalInfoStep(
    uiState: CompleteProfileUiState,
    viewModel: CompleteProfileViewModel
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Personal Information",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Please verify and complete your personal details.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        LofiTextField(
            value = uiState.fullName,
            onValueChange = { viewModel.onEvent(CompleteProfileEvent.UpdateField(ProfileField.FULL_NAME, it)) },
            label = "Full Name",
            isError = uiState.validationErrors["fullName"] != null,
            errorMessage = uiState.validationErrors["fullName"],
        )
        
        LofiTextField(
            value = uiState.phoneNumber,
            onValueChange = { viewModel.onEvent(CompleteProfileEvent.UpdateField(ProfileField.PHONE_NUMBER, it)) },
            label = "Phone Number",
            keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = KeyboardType.Phone),
            isError = uiState.validationErrors["phoneNumber"] != null,
            errorMessage = uiState.validationErrors["phoneNumber"],
        )
        
        LofiTextField(
            value = uiState.nik,
            onValueChange = { viewModel.onEvent(CompleteProfileEvent.UpdateField(ProfileField.NIK, it)) },
            label = "NIK",
            keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = KeyboardType.Number),
            isError = uiState.validationErrors["nik"] != null,
            errorMessage = uiState.validationErrors["nik"],
        )

        LofiDatePicker(
            selectedDate = uiState.dateOfBirth,
            onDateSelected = { viewModel.onEvent(CompleteProfileEvent.UpdateField(ProfileField.DATE_OF_BIRTH, it)) },
            label = "Date of Birth",
            isError = uiState.validationErrors["dateOfBirth"] != null,
            errorMessage = uiState.validationErrors["dateOfBirth"],
        )

        LofiTextField(
            value = uiState.placeOfBirth,
            onValueChange = { viewModel.onEvent(CompleteProfileEvent.UpdateField(ProfileField.PLACE_OF_BIRTH, it)) },
            label = "Place of Birth"
        )
        
        LofiDropdown(
            label = "Gender",
            options = uiState.genderOptions,
            selectedOption = uiState.gender,
            onOptionSelected = { viewModel.onEvent(CompleteProfileEvent.UpdateField(ProfileField.GENDER, it)) },
        )
        
        LofiDropdown(
            label = "Marital Status",
            options = uiState.maritalStatusOptions,
            selectedOption = uiState.maritalStatus,
            onOptionSelected = { viewModel.onEvent(CompleteProfileEvent.UpdateField(ProfileField.MARITAL_STATUS, it)) },
        )
        
        if (uiState.error != null) {
            Text(
                text = uiState.error!!,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(vertical = 4.dp)
            )
        }
        
        Spacer(modifier = Modifier.weight(1f))
        
        LofiButton(
            text = "Next",
            onClick = { viewModel.onEvent(CompleteProfileEvent.NextStep) },
        )
    }
}

@Composable
fun FinancialInfoStep(
    uiState: CompleteProfileUiState,
    viewModel: CompleteProfileViewModel
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Financial Information",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Please provide your financial details.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        LofiDropdown(
            label = "Income Source",
            options = uiState.incomeSourceOptions,
            selectedOption = uiState.incomeSource,
            onOptionSelected = { viewModel.onEvent(CompleteProfileEvent.UpdateField(ProfileField.INCOME_SOURCE, it)) },
            isError = uiState.validationErrors["incomeSource"] != null,
            errorMessage = uiState.validationErrors["incomeSource"],
        )
        
        LofiDropdown(
            label = "Income Type",
            options = uiState.incomeTypeOptions,
            selectedOption = uiState.incomeType,
            onOptionSelected = { viewModel.onEvent(CompleteProfileEvent.UpdateField(ProfileField.INCOME_TYPE, it)) },
        )
        
        LofiTextField(
            value = uiState.monthlyIncome,
            onValueChange = { viewModel.onEvent(CompleteProfileEvent.UpdateField(ProfileField.MONTHLY_INCOME, it)) },
            label = "Monthly Income",
            prefix = { Text("Rp ") },
            keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = KeyboardType.Number),
            isError = uiState.validationErrors["monthlyIncome"] != null,
            errorMessage = uiState.validationErrors["monthlyIncome"],
        )
        
        LofiDropdown(
            label = "Occupation",
            options = uiState.occupationOptions,
            selectedOption = uiState.occupation,
            onOptionSelected = { viewModel.onEvent(CompleteProfileEvent.UpdateField(ProfileField.OCCUPATION, it)) },
            isSearchable = true,
        )
        
        if (uiState.error != null) {
            Text(
                text = uiState.error!!,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(vertical = 4.dp)
            )
        }
        
        Spacer(modifier = Modifier.weight(1f))
        
        LofiButton(
            text = "Next",
            onClick = { viewModel.onEvent(CompleteProfileEvent.NextStep) },
        )
    }
}

@Composable
fun AddressInfoStep(
    uiState: CompleteProfileUiState,
    viewModel: CompleteProfileViewModel
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Address Information",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Please provide your complete address.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        LofiDropdown(
            label = "Province",
            options = uiState.provinces.map { it.name },
            selectedOption = uiState.province,
            onOptionSelected = { viewModel.onEvent(CompleteProfileEvent.UpdateField(ProfileField.PROVINCE, it)) },
            isSearchable = true,
            isError = uiState.validationErrors["province"] != null,
            errorMessage = uiState.validationErrors["province"],
        )
        
        LofiDropdown(
            label = "City",
            options = uiState.regencies.map { it.name },
            selectedOption = uiState.city,
            onOptionSelected = { viewModel.onEvent(CompleteProfileEvent.UpdateField(ProfileField.CITY, it)) },
            isSearchable = true,
            isError = uiState.validationErrors["city"] != null,
            errorMessage = uiState.validationErrors["city"],
        )
        
        LofiDropdown(
            label = "District (Kecamatan)",
            options = uiState.districts.map { it.name },
            selectedOption = uiState.district,
            onOptionSelected = { viewModel.onEvent(CompleteProfileEvent.UpdateField(ProfileField.DISTRICT, it)) },
            isSearchable = true,
        )
        
        LofiDropdown(
            label = "Sub-District (Kelurahan)",
            options = uiState.subDistricts.map { it.name },
            selectedOption = uiState.subDistrict,
            onOptionSelected = { viewModel.onEvent(CompleteProfileEvent.UpdateField(ProfileField.SUB_DISTRICT, it)) },
            isSearchable = true,
        )
        
        LofiTextField(
            value = uiState.address,
            onValueChange = { viewModel.onEvent(CompleteProfileEvent.UpdateField(ProfileField.ADDRESS, it)) },
            label = "Full Address",
            isError = uiState.validationErrors["address"] != null,
            errorMessage = uiState.validationErrors["address"],
        )
        
        LofiTextField(
            value = uiState.postalCode,
            onValueChange = { viewModel.onEvent(CompleteProfileEvent.UpdateField(ProfileField.POSTAL_CODE, it)) },
            label = "Postal Code",
            keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = KeyboardType.Number)
        )
        
        if (uiState.error != null) {
            Text(
                text = uiState.error!!,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(vertical = 4.dp)
            )
        }
        
        Spacer(modifier = Modifier.weight(1f))
        
        LofiButton(
            text = "Submit Details",
            onClick = { viewModel.onEvent(CompleteProfileEvent.SubmitProfile) },
            isLoading = uiState.isLoading,
        )
    }
}


