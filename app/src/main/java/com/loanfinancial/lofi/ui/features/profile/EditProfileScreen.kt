package com.loanfinancial.lofi.ui.features.profile

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.loanfinancial.lofi.R
import com.loanfinancial.lofi.ui.components.LofiButton
import com.loanfinancial.lofi.ui.components.LofiDatePicker
import com.loanfinancial.lofi.ui.components.LofiDropdown
import com.loanfinancial.lofi.ui.components.LofiTextField
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileScreen(
    navigateUp: () -> Unit,
    viewModel: EditProfileViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val scrollState = rememberScrollState()

    LaunchedEffect(uiState.isSuccess) {
        if (uiState.isSuccess) {
            navigateUp()
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "Edit Profile",
                        style =
                            MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 17.sp,
                            ),
                    )
                },
                navigationIcon = {
                    IconButton(onClick = navigateUp) {
                        Icon(
                            imageVector = androidx.compose.material.icons.Icons.Default.KeyboardArrowLeft,
                            contentDescription = "Back",
                            modifier = Modifier.size(28.dp),
                            tint = MaterialTheme.colorScheme.primary,
                        )
                    }
                },
                colors =
                    TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = MaterialTheme.colorScheme.background,
                        titleContentColor = MaterialTheme.colorScheme.onBackground,
                    ),
            )
        },
    ) { padding ->
        Column(
            modifier =
                Modifier
                    .padding(padding)
                    .fillMaxSize(),
        ) {
            StepIndicator(currentStep = uiState.currentStep)

            Column(
                modifier =
                    Modifier
                        .weight(1f)
                        .padding(16.dp)
                        .verticalScroll(scrollState),
            ) {
                when (uiState.currentStep) {
                    1 -> PersonalInfoStep(uiState, viewModel)
                    2 -> FinancialInfoStep(uiState, viewModel)
                    3 -> AddressInfoStep(uiState, viewModel)
                }

                if (uiState.errorMessage != null) {
                    Text(
                        text = uiState.errorMessage!!,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(top = 16.dp),
                    )
                }
            }

            Row(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                if (uiState.currentStep > 1) {
                    OutlinedButton(
                        onClick = { viewModel.prevStep() },
                        modifier =
                            Modifier
                                .weight(1f)
                                .padding(end = 8.dp),
                    ) {
                        Text("Previous")
                    }
                }

                LofiButton(
                    text = if (uiState.currentStep == 3) "Save Changes" else "Next",
                    onClick = {
                        if (uiState.currentStep == 3) viewModel.submit() else viewModel.nextStep()
                    },
                    isLoading = uiState.isLoading,
                    modifier =
                        Modifier
                            .weight(1f)
                            .padding(start = if (uiState.currentStep > 1) 8.dp else 0.dp),
                )
            }
        }
    }
}

@Composable
fun StepIndicator(currentStep: Int) {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp, horizontal = 32.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        StepDot(1, currentStep >= 1, "Personal")
        Line(currentStep >= 2)
        StepDot(2, currentStep >= 2, "Financial")
        Line(currentStep >= 3)
        StepDot(3, currentStep >= 3, "Address")
    }
}

@Composable
fun StepDot(
    step: Int,
    isActive: Boolean,
    label: String,
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Surface(
            shape = MaterialTheme.shapes.extraLarge,
            color = if (isActive) MaterialTheme.colorScheme.primary else Color.LightGray,
            modifier = Modifier.size(32.dp),
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    text = step.toString(),
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                )
            }
        }
        Text(text = label, fontSize = 10.sp, color = if (isActive) MaterialTheme.colorScheme.primary else Color.Gray)
    }
}

@Composable
fun Line(isActive: Boolean) {
    Divider(
        modifier =
            Modifier
                .width(40.dp)
                .padding(horizontal = 4.dp)
                .offset(y = (-8).dp),
        thickness = 2.dp,
        color = if (isActive) MaterialTheme.colorScheme.primary else Color.LightGray,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PersonalInfoStep(
    state: EditProfileUiState,
    viewModel: EditProfileViewModel,
) {
    Text(stringResource(R.string.personal_information), style = MaterialTheme.typography.titleLarge)
    Spacer(modifier = Modifier.height(16.dp))

    // Profile Photo Section (Read Only)
    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier.size(100.dp),
            contentAlignment = Alignment.Center,
        ) {
            // Placeholder behind
            Surface(
                modifier = Modifier.fillMaxSize(),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primaryContainer,
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(50.dp),
                    )
                }
            }

            // Actual Image
            AsyncImage(
                model =
                    ImageRequest
                        .Builder(LocalContext.current)
                        .data(if (state.profilePictureUrl.isBlank()) null else state.profilePictureUrl)
                        .crossfade(true)
                        .build(),
                contentDescription = "Profile Picture",
                modifier =
                    Modifier
                        .fillMaxSize()
                        .clip(CircleShape),
                contentScale = ContentScale.Crop,
            )
        }
    }

    Spacer(modifier = Modifier.height(16.dp))

    LofiTextField(
        value = state.fullName,
        onValueChange = { viewModel.onFieldChange("fullName", it) },
        label = "Full Name",
        isError = state.validationErrors["fullName"] != null,
        errorMessage = state.validationErrors["fullName"],
    )
    Spacer(modifier = Modifier.height(12.dp))

    LofiTextField(
        value = state.phoneNumber,
        onValueChange = { viewModel.onFieldChange("phoneNumber", it) },
        label = "Phone Number",
        isError = state.validationErrors["phoneNumber"] != null,
        errorMessage = state.validationErrors["phoneNumber"],
    )
    Spacer(modifier = Modifier.height(12.dp))

    LofiTextField(
        value = state.nik,
        onValueChange = { viewModel.onFieldChange("nik", it) },
        label = "NIK",
        isError = state.validationErrors["nik"] != null,
        errorMessage = state.validationErrors["nik"],
    )
    Spacer(modifier = Modifier.height(12.dp))

    LofiDatePicker(
        selectedDate = state.dateOfBirth,
        onDateSelected = { viewModel.onFieldChange("dateOfBirth", it) },
        label = "Date of Birth",
        isError = state.validationErrors["dateOfBirth"] != null,
        errorMessage = state.validationErrors["dateOfBirth"],
    )
    Spacer(modifier = Modifier.height(12.dp))

    LofiTextField(
        value = state.placeOfBirth,
        onValueChange = { viewModel.onFieldChange("placeOfBirth", it) },
        label = "Place of Birth",
    )
    Spacer(modifier = Modifier.height(12.dp))

    LofiDropdown(
        label = "Gender",
        options = state.genderOptions,
        selectedOption = state.gender,
        onOptionSelected = { viewModel.onFieldChange("gender", it) },
    )
    Spacer(modifier = Modifier.height(12.dp))

    LofiDropdown(
        label = "Marital Status",
        options = state.maritalStatusOptions,
        selectedOption = state.maritalStatus,
        onOptionSelected = { viewModel.onFieldChange("maritalStatus", it) },
    )
}

@Composable
fun FinancialInfoStep(
    state: EditProfileUiState,
    viewModel: EditProfileViewModel,
) {
    Text("Financial Information", style = MaterialTheme.typography.titleLarge)
    Spacer(modifier = Modifier.height(16.dp))

    LofiDropdown(
        label = "Income Source",
        options = state.incomeSourceOptions,
        selectedOption = state.incomeSource,
        onOptionSelected = { viewModel.onFieldChange("incomeSource", it) },
        isError = state.validationErrors["incomeSource"] != null,
        errorMessage = state.validationErrors["incomeSource"],
    )
    Spacer(modifier = Modifier.height(12.dp))

    LofiDropdown(
        label = "Income Type",
        options = state.incomeTypeOptions,
        selectedOption = state.incomeType,
        onOptionSelected = { viewModel.onFieldChange("incomeType", it) },
    )
    Spacer(modifier = Modifier.height(12.dp))

    LofiTextField(
        value = state.monthlyIncome,
        onValueChange = { viewModel.onFieldChange("monthlyIncome", it) },
        label = "Monthly Income",
        prefix = { Text("Rp ") },
        isError = state.validationErrors["monthlyIncome"] != null,
        errorMessage = state.validationErrors["monthlyIncome"],
    )
}

@Composable
fun AddressInfoStep(
    state: EditProfileUiState,
    viewModel: EditProfileViewModel,
) {
    Text("Address & Other Information", style = MaterialTheme.typography.titleLarge)
    Spacer(modifier = Modifier.height(16.dp))

    LofiDropdown(
        label = "Province",
        options = state.provinces.map { it.name },
        selectedOption = state.province,
        onOptionSelected = { viewModel.onFieldChange("province", it) },
        isSearchable = true,
        isError = state.validationErrors["province"] != null,
        errorMessage = state.validationErrors["province"],
    )
    Spacer(modifier = Modifier.height(12.dp))

    LofiDropdown(
        label = "City",
        options = state.regencies.map { it.name },
        selectedOption = state.city,
        onOptionSelected = { viewModel.onFieldChange("city", it) },
        isSearchable = true,
        isError = state.validationErrors["city"] != null,
        errorMessage = state.validationErrors["city"],
    )
    Spacer(modifier = Modifier.height(12.dp))

    LofiDropdown(
        label = "District (Kecamatan)",
        options = state.districts.map { it.name },
        selectedOption = state.district,
        onOptionSelected = { viewModel.onFieldChange("district", it) },
        isSearchable = true,
        // isError = state.validationErrors["district"] != null, // Add validation if needed
        // errorMessage = state.validationErrors["district"],
    )
    Spacer(modifier = Modifier.height(12.dp))

    LofiDropdown(
        label = "Sub-District (Kelurahan)",
        options = state.subDistricts.map { it.name },
        selectedOption = state.subDistrict,
        onOptionSelected = { viewModel.onFieldChange("subDistrict", it) },
        isSearchable = true,
    )
    Spacer(modifier = Modifier.height(12.dp))

    LofiTextField(
        value = state.address,
        onValueChange = { viewModel.onFieldChange("address", it) },
        label = "Full Address",
        isError = state.validationErrors["address"] != null,
        errorMessage = state.validationErrors["address"],
    )
    Spacer(modifier = Modifier.height(12.dp))

    LofiTextField(
        value = state.postalCode,
        onValueChange = { viewModel.onFieldChange("postalCode", it) },
        label = "Postal Code",
    )
    Spacer(modifier = Modifier.height(12.dp))

    LofiDropdown(
        label = "Occupation",
        options = state.occupationOptions,
        selectedOption = state.occupation,
        isSearchable = true,
        onOptionSelected = { viewModel.onFieldChange("occupation", it) },
    )
}

private fun createImageFile(context: android.content.Context): File {
    val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
    val storageDir = context.cacheDir
    return File.createTempFile(
        "JPEG_${timeStamp}_",
        ".jpg",
        storageDir,
    )
}
