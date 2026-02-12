package com.loanfinancial.lofi.ui.features.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.loanfinancial.lofi.core.util.Resource
import com.loanfinancial.lofi.core.util.UrlUtil
import com.loanfinancial.lofi.data.model.entity.ProfileDraftEntity
import com.loanfinancial.lofi.data.remote.api.DistrictResponse
import com.loanfinancial.lofi.data.remote.api.ProvinceResponse
import com.loanfinancial.lofi.data.remote.api.RegencyResponse
import com.loanfinancial.lofi.data.remote.api.VillageResponse
import com.loanfinancial.lofi.domain.repository.IAuthRepository
import com.loanfinancial.lofi.domain.repository.IRegionRepository
import com.loanfinancial.lofi.domain.usecase.user.ClearProfileDraftUseCase
import com.loanfinancial.lofi.domain.usecase.user.GetProfileDraftUseCase
import com.loanfinancial.lofi.domain.usecase.user.GetUserProfileUseCase
import com.loanfinancial.lofi.domain.usecase.user.SaveProfileDraftUseCase
import com.loanfinancial.lofi.domain.usecase.user.UpdateProfileUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class EditProfileUiState(
    val fullName: String = "",
    val phoneNumber: String = "",
    val profilePictureUrl: String = "",
    val incomeSource: String = "",
    val incomeType: String = "",
    val monthlyIncome: String = "0",
    val nik: String = "",
    val dateOfBirth: String = "",
    val placeOfBirth: String = "",
    val city: String = "",
    val address: String = "",
    val province: String = "",
    val district: String = "",
    val subDistrict: String = "",
    val postalCode: String = "",
    val gender: String = "MALE",
    val maritalStatus: String = "SINGLE",
    val occupation: String = "",
    val currentStep: Int = 1,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isSuccess: Boolean = false,
    val validationErrors: Map<String, String> = emptyMap(),
    // Dropdown options and data
    val genderOptions: List<String> = listOf("MALE", "FEMALE"),
    val maritalStatusOptions: List<String> = listOf("SINGLE", "MARRIED", "DIVORCED", "WIDOWED"),
    val incomeSourceOptions: List<String> = listOf("SALARY", "BUSINESS", "INVESTMENT", "OTHER"),
    val incomeTypeOptions: List<String> = listOf("FIXED", "VARIABLE", "COMMISSION"),
    val occupationOptions: List<String> = listOf("EMPLOYEE", "ENTREPRENEUR", "STUDENT", "UNEMPLOYED", "RETIRED", "OTHER"),
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
class EditProfileViewModel
    @Inject
    constructor(
        private val authRepository: IAuthRepository,
        private val updateProfileUseCase: UpdateProfileUseCase,
        private val getProfileDraftUseCase: GetProfileDraftUseCase,
        private val saveProfileDraftUseCase: SaveProfileDraftUseCase,
        private val clearProfileDraftUseCase: ClearProfileDraftUseCase,
        private val regionRepository: IRegionRepository,
        private val getUserProfileUseCase: GetUserProfileUseCase,
    ) : ViewModel() {
        private val _uiState = MutableStateFlow(EditProfileUiState())
        val uiState: StateFlow<EditProfileUiState> = _uiState.asStateFlow()

        private var userId: String? = null

        private var saveDraftJob: Job? = null

        init {
            loadUserProfile()
            loadProvinces()
        }

        private fun loadUserProfile() {
            viewModelScope.launch {
                _uiState.update { it.copy(isLoading = true) }
                val user = authRepository.getUser().firstOrNull()
                userId = user?.id

                if (userId != null) {
                    // Check for draft first
                    val draft = getProfileDraftUseCase(userId!!).firstOrNull()

                    if (draft != null) {
                        _uiState.update {
                            it.copy(
                                fullName = draft.fullName,
                                phoneNumber = draft.phoneNumber,
                                profilePictureUrl = UrlUtil.getFullProfileUrl(draft.profilePictureUrl) ?: "",
                                incomeSource = draft.incomeSource,
                                incomeType = draft.incomeType,
                                monthlyIncome = draft.monthlyIncome.toString(),
                                nik = draft.nik,
                                dateOfBirth = draft.dateOfBirth,
                                placeOfBirth = draft.placeOfBirth,
                                city = draft.city,
                                address = draft.address,
                                province = draft.province,
                                district = draft.district,
                                subDistrict = draft.subDistrict,
                                postalCode = draft.postalCode,
                                gender = draft.gender,
                                maritalStatus = draft.maritalStatus,
                                occupation = draft.occupation,
                                isLoading = false,
                            )
                        }
                        if (draft.province.isNotEmpty()) {
                            // We might need to map province name to ID if the API requires ID,
                            // but the current UI passes mapping { it.name }.
                            // Assuming string names for now.
                            // Ideally we should load regencies for the draft province
                            // loadRegencies(draft.province)
                        }
                    } else {
                        // Load actual profile
                        getUserProfileUseCase().collect { result ->
                            if (result is Resource.Success) {
                                val data = result.data
                                _uiState.update {
                                    it.copy(
                                        fullName = data.fullName ?: "",
                                        phoneNumber = data.phoneNumber ?: "",
                                        profilePictureUrl = UrlUtil.getFullProfileUrl(data.profilePictureUrl) ?: "",
                                        // Biodata fields
                                        incomeSource = data.biodata?.incomeSource ?: "",
                                        incomeType = data.biodata?.incomeType ?: "",
                                        monthlyIncome =
                                            data.biodata
                                                ?.monthlyIncome
                                                ?.toLong()
                                                ?.toString() ?: "0",
                                        // Show as integer string if possible
                                        nik = data.biodata?.nik ?: "",
                                        dateOfBirth = data.biodata?.dateOfBirth ?: "",
                                        placeOfBirth = data.biodata?.placeOfBirth ?: "",
                                        city = data.biodata?.city ?: "",
                                        address = data.biodata?.address ?: "",
                                        province = data.biodata?.province ?: "",
                                        district = data.biodata?.district ?: "",
                                        subDistrict = data.biodata?.subDistrict ?: "",
                                        postalCode = data.biodata?.postalCode ?: "",
                                        gender = data.biodata?.gender ?: "MALE",
                                        maritalStatus = data.biodata?.maritalStatus ?: "SINGLE",
                                        occupation = data.biodata?.occupation ?: "",
                                        isLoading = false,
                                    )
                                }
                            } else if (result is Resource.Error) {
                                _uiState.update { it.copy(isLoading = false, errorMessage = result.message) }
                            }
                        }
                    }
                } else {
                    _uiState.update { it.copy(isLoading = false, errorMessage = "User not found") }
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
                        is Resource.Error -> _uiState.update { it.copy(isLoadingProvinces = false) } // Silent error for now
                        is Resource.Loading -> {}
                    }
                }
            }
        }

        private fun loadRegencies(provinceIdOrName: String) {
            // Assuming we need ID, but the UI passes the name from state.province.
            // We need to find the ID from the province list or usage matches.
            // For now, let's assume specific logic is needed to find ID.
            // If provinceIdOrName is name, we look it up.
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

        fun onFieldChange(
            field: String,
            value: String,
        ) {
            _uiState.update { state ->
                when (field) {
                    "fullName" -> state.copy(fullName = value)
                    "phoneNumber" -> state.copy(phoneNumber = value)
                    "nik" -> state.copy(nik = value)
                    "dateOfBirth" -> state.copy(dateOfBirth = value)
                    "placeOfBirth" -> state.copy(placeOfBirth = value)
                    "gender" -> state.copy(gender = value)
                    "maritalStatus" -> state.copy(maritalStatus = value)
                    "incomeSource" -> state.copy(incomeSource = value)
                    "incomeType" -> state.copy(incomeType = value)
                    "monthlyIncome" -> state.copy(monthlyIncome = value)
                    "province" -> {
                        if (state.province != value) {
                            loadRegencies(value)
                            state.copy(
                                province = value,
                                city = "",
                                district = "",
                                subDistrict = "",
                            )
                        } else {
                            state
                        }
                    }
                    "city" -> {
                        if (state.city != value) {
                            loadDistricts(value)
                            state.copy(
                                city = value,
                                district = "",
                                subDistrict = "",
                            )
                        } else {
                            state
                        }
                    }
                    "district" -> {
                        if (state.district != value) {
                            loadSubDistricts(value)
                            state.copy(
                                district = value,
                                subDistrict = "",
                            )
                        } else {
                            state
                        }
                    }
                    "subDistrict" -> state.copy(subDistrict = value)
                    "address" -> state.copy(address = value)
                    "postalCode" -> state.copy(postalCode = value)
                    "occupation" -> state.copy(occupation = value)
                    else -> state
                }
            }
            // Auto-save draft
            saveDraft()
        }

        private fun saveDraft() {
            saveDraftJob?.cancel()
            saveDraftJob =
                viewModelScope.launch {
                    delay(1000) // Debounce 1s
                    val state = _uiState.value
                    userId?.let { uid ->
                        val draft =
                            ProfileDraftEntity(
                                userId = uid,
                                fullName = state.fullName,
                                phoneNumber = state.phoneNumber,
                                profilePictureUrl = state.profilePictureUrl,
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
                        saveProfileDraftUseCase(draft)
                    }
                }
        }

        fun nextStep() {
            val current = _uiState.value.currentStep
            if (validateStep(current)) {
                _uiState.update { it.copy(currentStep = current + 1) }
            }
        }

        fun prevStep() {
            val current = _uiState.value.currentStep
            if (current > 1) {
                _uiState.update { it.copy(currentStep = current - 1) }
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
                    if (state.dateOfBirth.isBlank()) errors["dateOfBirth"] = "Date of Birth is required"
                    // ... add specific validations
                }
                2 -> { // Financial
                    if (state.incomeSource.isBlank()) errors["incomeSource"] = "Income Source is required"
                    if (state.monthlyIncome.isBlank() || state.monthlyIncome == "0") errors["monthlyIncome"] = "Monthly Income is required"
                }
                3 -> { // Address
                    if (state.province.isBlank()) errors["province"] = "Province is required"
                    if (state.city.isBlank()) errors["city"] = "City is required"
                    if (state.address.isBlank()) errors["address"] = "Address is required"
                }
            }

            _uiState.update { it.copy(validationErrors = errors) }
            return errors.isEmpty()
        }

        fun submit() {
            if (!validateStep(3)) return

            viewModelScope.launch {
                _uiState.update { it.copy(isLoading = true, errorMessage = null) }

                val state = _uiState.value
                val request =
                    ProfileDraftEntity(
                        userId = userId ?: "",
                        fullName = state.fullName,
                        phoneNumber = state.phoneNumber,
                        // profilePictureUrl removed
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
                    ).toUpdateRequest()

                updateProfileUseCase(request).collect { result ->
                    when (result) {
                        is Resource.Loading -> { /* already loading */ }
                        is Resource.Success -> {
                            _uiState.update { it.copy(isLoading = false, isSuccess = true) }
                            clearProfileDraftUseCase(userId ?: "")
                        }
                        is Resource.Error -> {
                            _uiState.update {
                                it.copy(
                                    isLoading = false,
                                    errorMessage = result.message,
                                )
                            }
                        }
                    }
                }
            }
        }
    }
