package com.loanfinancial.lofi.ui.features.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.loanfinancial.lofi.core.util.Resource
import com.loanfinancial.lofi.data.local.datastore.DataStoreManager
import com.loanfinancial.lofi.data.model.dto.UserUpdateRequest
import com.loanfinancial.lofi.data.remote.api.DistrictResponse
import com.loanfinancial.lofi.data.remote.api.ProvinceResponse
import com.loanfinancial.lofi.data.remote.api.RegencyResponse
import com.loanfinancial.lofi.data.remote.api.VillageResponse
import com.loanfinancial.lofi.domain.repository.IRegionRepository
import com.loanfinancial.lofi.domain.usecase.auth.GetAuthSourceUseCase
import com.loanfinancial.lofi.domain.usecase.user.UpdateProfileUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CompleteProfileUiState(
    val currentStep: Int = 1, // 1: Personal Info, 2: Financial, 3: Address
    val isLoading: Boolean = false,
    val error: String? = null,
    val isSuccess: Boolean = false,
    val isGoogleUser: Boolean = false,
    val validationErrors: Map<String, String> = emptyMap(),
    // Step 1: Personal Info
    val fullName: String = "",
    val phoneNumber: String = "",
    val nik: String = "",
    val dateOfBirth: String = "",
    val placeOfBirth: String = "",
    val gender: String = "MALE",
    val maritalStatus: String = "SINGLE",
    // Step 2: Financial
    val incomeSource: String = "",
    val incomeType: String = "",
    val monthlyIncome: String = "",
    val occupation: String = "",
    // Step 3: Address
    val address: String = "",
    val province: String = "",
    val city: String = "",
    val district: String = "",
    val subDistrict: String = "",
    val postalCode: String = "",
    // Dropdown options
    val genderOptions: List<String> = listOf("MALE", "FEMALE"),
    val maritalStatusOptions: List<String> = listOf("SINGLE", "MARRIED", "DIVORCED", "WIDOWED"),
    val incomeSourceOptions: List<String> = listOf("SALARY", "BUSINESS", "INVESTMENT", "OTHER"),
    val incomeTypeOptions: List<String> = listOf("FIXED", "VARIABLE", "COMMISSION"),
    val occupationOptions: List<String> = listOf("EMPLOYEE", "ENTREPRENEUR", "STUDENT", "UNEMPLOYED", "RETIRED", "OTHER"),
    // Region data
    val provinces: List<ProvinceResponse> = emptyList(),
    val regencies: List<RegencyResponse> = emptyList(),
    val districts: List<DistrictResponse> = emptyList(),
    val subDistricts: List<VillageResponse> = emptyList(),
    val isLoadingProvinces: Boolean = false,
    val isLoadingRegencies: Boolean = false,
    val isLoadingDistricts: Boolean = false,
    val isLoadingSubDistricts: Boolean = false,
)

@HiltViewModel
class CompleteProfileViewModel
    @Inject
    constructor(
        private val updateProfileUseCase: UpdateProfileUseCase,
        private val dataStoreManager: DataStoreManager,
        private val getAuthSourceUseCase: GetAuthSourceUseCase,
        private val regionRepository: IRegionRepository,
    ) : ViewModel() {
        private val _uiState = MutableStateFlow(CompleteProfileUiState())
        val uiState: StateFlow<CompleteProfileUiState> = _uiState.asStateFlow()

        init {
            checkAuthSource()
            loadProvinces()
        }

        private fun checkAuthSource() {
            viewModelScope.launch {
                val result = getAuthSourceUseCase()
                if (result.isSuccess) {
                    _uiState.update { it.copy(isGoogleUser = result.getOrNull()?.googleUser == true) }
                }
            }
        }

        private fun loadProvinces() {
            viewModelScope.launch {
                _uiState.update { it.copy(isLoadingProvinces = true) }
                regionRepository.getProvinces().collect { result ->
                    when (result) {
                        is Resource.Success -> {
                            _uiState.update { it.copy(isLoadingProvinces = false, provinces = result.data ?: emptyList()) }
                        }
                        is Resource.Error -> _uiState.update { it.copy(isLoadingProvinces = false) }
                        is Resource.Loading -> {}
                    }
                }
            }
        }

        private fun loadRegencies(provinceIdOrName: String) {
            val province = _uiState.value.provinces.find { it.name == provinceIdOrName || it.id == provinceIdOrName }
            if (province != null) {
                viewModelScope.launch {
                    _uiState.update { it.copy(isLoadingRegencies = true) }
                    regionRepository.getRegencies(province.id).collect { result ->
                        when (result) {
                            is Resource.Success -> {
                                _uiState.update { it.copy(isLoadingRegencies = false, regencies = result.data ?: emptyList()) }
                            }
                            is Resource.Error -> _uiState.update { it.copy(isLoadingRegencies = false) }
                            is Resource.Loading -> {}
                        }
                    }
                }
            }
        }

        private fun loadDistricts(regencyIdOrName: String) {
            val regency = _uiState.value.regencies.find { it.name == regencyIdOrName || it.id == regencyIdOrName }
            if (regency != null) {
                viewModelScope.launch {
                    _uiState.update { it.copy(isLoadingDistricts = true) }
                    regionRepository.getDistricts(regency.id).collect { result ->
                        when (result) {
                            is Resource.Success -> {
                                _uiState.update { it.copy(isLoadingDistricts = false, districts = result.data ?: emptyList()) }
                            }
                            is Resource.Error -> _uiState.update { it.copy(isLoadingDistricts = false) }
                            is Resource.Loading -> {}
                        }
                    }
                }
            }
        }

        private fun loadSubDistricts(districtIdOrName: String) {
            val district = _uiState.value.districts.find { it.name == districtIdOrName || it.id == districtIdOrName }
            if (district != null) {
                viewModelScope.launch {
                    _uiState.update { it.copy(isLoadingSubDistricts = true) }
                    regionRepository.getVillages(district.id).collect { result ->
                        when (result) {
                            is Resource.Success -> {
                                _uiState.update { it.copy(isLoadingSubDistricts = false, subDistricts = result.data ?: emptyList()) }
                            }
                            is Resource.Error -> _uiState.update { it.copy(isLoadingSubDistricts = false) }
                            is Resource.Loading -> {}
                        }
                    }
                }
            }
        }

        fun onEvent(event: CompleteProfileEvent) {
            when (event) {
                is CompleteProfileEvent.NextStep -> nextStep()
                is CompleteProfileEvent.PreviousStep -> prevStep()
                is CompleteProfileEvent.UpdateField -> updateField(event.field, event.value)
                is CompleteProfileEvent.SubmitProfile -> submitProfile()
            }
        }

        private fun nextStep() {
            val current = _uiState.value.currentStep
            if (validateStep(current)) {
                _uiState.update { it.copy(currentStep = current + 1, error = null) }
            }
        }

        private fun prevStep() {
            val current = _uiState.value.currentStep
            if (current > 1) {
                _uiState.update { it.copy(currentStep = current - 1, error = null, validationErrors = emptyMap()) }
            }
        }

        private fun updateField(
            field: ProfileField,
            value: String,
        ) {
            _uiState.update { state ->
                val newState =
                    when (field) {
                        ProfileField.FULL_NAME -> state.copy(fullName = value)
                        ProfileField.PHONE_NUMBER -> state.copy(phoneNumber = value)
                        ProfileField.DATE_OF_BIRTH -> state.copy(dateOfBirth = value)
                        ProfileField.PLACE_OF_BIRTH -> state.copy(placeOfBirth = value)
                        ProfileField.GENDER -> state.copy(gender = value)
                        ProfileField.MARITAL_STATUS -> state.copy(maritalStatus = value)
                        ProfileField.ADDRESS -> state.copy(address = value)
                        ProfileField.PROVINCE -> {
                            if (state.province != value) {
                                loadRegencies(value)
                                state.copy(province = value, city = "", district = "", subDistrict = "")
                            } else {
                                state
                            }
                        }
                        ProfileField.CITY -> {
                            if (state.city != value) {
                                loadDistricts(value)
                                state.copy(city = value, district = "", subDistrict = "")
                            } else {
                                state
                            }
                        }
                        ProfileField.DISTRICT -> {
                            if (state.district != value) {
                                loadSubDistricts(value)
                                state.copy(district = value, subDistrict = "")
                            } else {
                                state
                            }
                        }
                        ProfileField.SUB_DISTRICT -> state.copy(subDistrict = value)
                        ProfileField.POSTAL_CODE -> state.copy(postalCode = value)
                        ProfileField.NIK -> state.copy(nik = value)
                        ProfileField.INCOME_SOURCE -> state.copy(incomeSource = value)
                        ProfileField.INCOME_TYPE -> state.copy(incomeType = value)
                        ProfileField.MONTHLY_INCOME -> state.copy(monthlyIncome = value)
                        ProfileField.OCCUPATION -> state.copy(occupation = value)
                    }
                // Clear validation error for this field
                newState.copy(validationErrors = newState.validationErrors - field.name.lowercase())
            }
        }

        private fun validateStep(step: Int): Boolean {
            val state = _uiState.value
            val errors = mutableMapOf<String, String>()

            when (step) {
                1 -> { // Personal
                    if (state.fullName.isBlank()) errors["fullName"] = "Full name is required"
                    if (state.phoneNumber.isBlank()) errors["phoneNumber"] = "Phone number is required"
                    if (state.nik.isBlank()) errors["nik"] = "NIK is required"
                    if (state.dateOfBirth.isBlank()) {
                        errors["dateOfBirth"] = "Date of Birth is required"
                    } else if (!isValidDateFormat(state.dateOfBirth)) {
                        errors["dateOfBirth"] = "Invalid date format (YYYY-MM-DD)"
                    }
                }
                2 -> { // Financial
                    if (state.incomeSource.isBlank()) errors["incomeSource"] = "Income Source is required"
                    if (state.monthlyIncome.isBlank() || state.monthlyIncome == "0") {
                        errors["monthlyIncome"] = "Monthly Income is required"
                    }
                }
                3 -> { // Address
                    if (state.province.isBlank()) errors["province"] = "Province is required"
                    if (state.city.isBlank()) errors["city"] = "City is required"
                    if (state.address.isBlank()) errors["address"] = "Address is required"
                }
            }

            _uiState.update { it.copy(validationErrors = errors) }
            if (errors.isNotEmpty()) {
                _uiState.update { it.copy(error = "Please fill in all required fields") }
            }
            return errors.isEmpty()
        }

        private fun isValidDateFormat(date: String): Boolean {
            return try {
                val parts = date.split("-")
                if (parts.size != 3) return false
                val year = parts[0].toInt()
                val month = parts[1].toInt()
                val day = parts[2].toInt()
                year in 1900..2100 && month in 1..12 && day in 1..31
            } catch (e: Exception) {
                false
            }
        }

        private fun submitProfile() {
            if (!validateStep(3)) return

            viewModelScope.launch {
                _uiState.update { it.copy(isLoading = true, error = null) }

                val state = _uiState.value
                val request =
                    UserUpdateRequest(
                        fullName = state.fullName,
                        phoneNumber = state.phoneNumber,
                        incomeSource = state.incomeSource,
                        incomeType = state.incomeType,
                        monthlyIncome = state.monthlyIncome.toDoubleOrNull() ?: 0.0,
                        nik = state.nik,
                        dateOfBirth = state.dateOfBirth,
                        placeOfBirth = state.placeOfBirth,
                        city = state.city,
                        address = state.address,
                        province = state.province,
                        district = state.district,
                        subDistrict = state.subDistrict,
                        postalCode = state.postalCode,
                        gender = state.gender,
                        maritalStatus = state.maritalStatus,
                        occupation = state.occupation,
                    )

                updateProfileUseCase(request).collect { result ->
                    when (result) {
                        is Resource.Success -> {
                            dataStoreManager.setProfileCompleted(true)
                            dataStoreManager.setFirstInstall(false)
                            _uiState.update { it.copy(isLoading = false, isSuccess = true) }
                        }
                        is Resource.Error -> {
                            _uiState.update { it.copy(isLoading = false, error = result.message) }
                        }
                        is Resource.Loading -> {
                            _uiState.update { it.copy(isLoading = true) }
                        }
                    }
                }
            }
        }
    }

sealed class CompleteProfileEvent {
    object NextStep : CompleteProfileEvent()

    object PreviousStep : CompleteProfileEvent()

    data class UpdateField(
        val field: ProfileField,
        val value: String,
    ) : CompleteProfileEvent()

    object SubmitProfile : CompleteProfileEvent()
}

enum class ProfileField {
    FULL_NAME,
    PHONE_NUMBER,
    DATE_OF_BIRTH,
    PLACE_OF_BIRTH,
    GENDER,
    MARITAL_STATUS,
    ADDRESS,
    PROVINCE,
    CITY,
    DISTRICT,
    SUB_DISTRICT,
    POSTAL_CODE,
    NIK,
    INCOME_SOURCE,
    INCOME_TYPE,
    MONTHLY_INCOME,
    OCCUPATION,
}
