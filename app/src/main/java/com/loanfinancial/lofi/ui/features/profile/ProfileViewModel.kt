package com.loanfinancial.lofi.ui.features.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.loanfinancial.lofi.core.util.Resource
import com.loanfinancial.lofi.core.util.UrlUtil
import com.loanfinancial.lofi.domain.repository.IAuthRepository
import com.loanfinancial.lofi.data.model.dto.ChangePinRequest
import com.loanfinancial.lofi.data.model.dto.SetPinRequest
import com.loanfinancial.lofi.domain.usecase.auth.HasPinUseCase
import com.loanfinancial.lofi.domain.usecase.auth.LogoutUseCase
import com.loanfinancial.lofi.domain.usecase.user.ChangePinUseCase
import com.loanfinancial.lofi.domain.usecase.user.GetUserProfileUseCase
import com.loanfinancial.lofi.domain.usecase.user.GetUserUseCase
import com.loanfinancial.lofi.domain.usecase.user.SetPinUseCase
import com.loanfinancial.lofi.domain.usecase.user.UploadProfilePictureUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ProfileUiState(
    val name: String = "Lofi Name",
    val email: String = "lofi@example.com",
    val profilePictureUrl: String? = null,
    val phoneNumber: String = "",
    val isLoading: Boolean = false,
    val roles: List<String> = emptyList(),
    val profileError: String? = null,
    val isLogoutSuccessful: Boolean = false,
    // Biodata
    val incomeSource: String = "",
    val monthlyIncome: String = "0",
    val address: String = "",
    val province: String = "",
    val city: String = "",
    val nik: String = "",
    val hasPin: Boolean = false,
    val pinActionLoading: Boolean = false,
    val pinActionError: String? = null,
    val pinActionSuccess: Boolean = false,
    val isGoogleUser: Boolean = false,
)

@HiltViewModel
class ProfileViewModel
    @Inject
    constructor(
        private val getUserUseCase: GetUserUseCase,
        private val logoutUseCase: LogoutUseCase,
        private val authRepository: IAuthRepository,
        private val getUserProfileUseCase: GetUserProfileUseCase,
        private val uploadProfilePictureUseCase: UploadProfilePictureUseCase,
        private val hasPinUseCase: HasPinUseCase,
        private val setPinUseCase: SetPinUseCase,
        private val changePinUseCase: ChangePinUseCase,
        private val getAuthSourceUseCase: com.loanfinancial.lofi.domain.usecase.auth.GetAuthSourceUseCase,
    ) : ViewModel() {
        private val _uiState = MutableStateFlow(ProfileUiState())
        val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

        init {
            viewModelScope.launch {
                authRepository.getUser().collect { user ->
                    if (user != null) {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                name = user.username,
                                email = user.email,
                                roles = user.roles,
                            )
                        }
                        loadFullProfile()
                        checkHasPin()
                        checkAuthSource()
                    } else {
                        getProfile()
                    }
                }
            }
        }

        private fun checkAuthSource() {
            viewModelScope.launch {
                val result = getAuthSourceUseCase()
                if (result.isSuccess) {
                    _uiState.update { it.copy(isGoogleUser = result.getOrNull()?.googleUser == true) }
                }
            }
        }

        private fun checkHasPin() {
            viewModelScope.launch {
                val result = hasPinUseCase()
                if (result.isSuccess) {
                    _uiState.update { it.copy(hasPin = result.getOrDefault(false)) }
                }
            }
        }

        private fun loadFullProfile() {
            viewModelScope.launch {
                getUserProfileUseCase().collect { result ->
                    when (result) {
                        is Resource.Success -> {
                            val data = result.data ?: return@collect
                            _uiState.update {
                                it.copy(
                                    name = data.fullName ?: "",
                                    phoneNumber = data.phoneNumber ?: "",
                                    profilePictureUrl = UrlUtil.getFullProfileUrl(data.profilePictureUrl),
                                    incomeSource = data.biodata?.incomeSource ?: "",
                                    monthlyIncome = (data.biodata?.monthlyIncome ?: 0.0).toString(),
                                    address = data.biodata?.address ?: "",
                                    province = data.biodata?.province ?: "",
                                    city = data.biodata?.city ?: "",
                                    nik = data.biodata?.nik ?: "",
                                )
                            }
                        }
                        else -> {}
                    }
                }
            }
        }

        private fun getProfile() {
            viewModelScope.launch {
                _uiState.update { it.copy(isLoading = true, profileError = null) }

                val result = getUserUseCase()

                if (result.isSuccess) {
                    val user = result.getOrNull()?.data

                    if (user != null) {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                name = user.username,
                                email = user.email,
                                roles = user.roles,
                            )
                        }
                    } else {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                profileError = "User data not found",
                            )
                        }
                    }
                } else {
                    val errorMessage =
                        result.exceptionOrNull()?.message ?: "Failed to load profile"

                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            profileError = errorMessage,
                        )
                    }
                }
            }
        }

        fun onLogout() {
            viewModelScope.launch {
                _uiState.update { it.copy(isLoading = true) }
                val result = logoutUseCase()
                if (result.isSuccess) {
                    _uiState.update { it.copy(isLogoutSuccessful = true, isLoading = false) }
                } else {
                    _uiState.update { it.copy(isLoading = false, profileError = result.exceptionOrNull()?.message) }
                }
            }
        }

        fun onPhotoSelected(file: java.io.File) {
            viewModelScope.launch {
                _uiState.update { it.copy(isLoading = true) }
                uploadProfilePictureUseCase(file).collect { result ->
                    when (result) {
                        is Resource.Success -> {
                            _uiState.update { it.copy(isLoading = false, profilePictureUrl = UrlUtil.getFullProfileUrl(result.data?.profilePictureUrl)) }
                            loadFullProfile() // Reload to ensure sync
                        }
                        is Resource.Error -> {
                            _uiState.update { it.copy(isLoading = false, profileError = result.message) }
                        }
                        is Resource.Loading -> {}
                    }
                }
            }
        }
        fun setPin(pin: String) {
            viewModelScope.launch {
                _uiState.update { it.copy(pinActionLoading = true, pinActionError = null, pinActionSuccess = false) }
                setPinUseCase(SetPinRequest(pin)).collect { result ->
                    when (result) {
                        is Resource.Success -> {
                            _uiState.update { it.copy(pinActionLoading = false, pinActionSuccess = true, hasPin = true) }
                        }
                        is Resource.Error -> {
                            _uiState.update { it.copy(pinActionLoading = false, pinActionError = result.message) }
                        }
                        is Resource.Loading -> {}
                    }
                }
            }
        }

        fun changePin(oldPin: String, newPin: String) {
            viewModelScope.launch {
                _uiState.update { it.copy(pinActionLoading = true, pinActionError = null, pinActionSuccess = false) }
                changePinUseCase(ChangePinRequest(oldPin, newPin)).collect { result ->
                    when (result) {
                        is Resource.Success -> {
                            _uiState.update { it.copy(pinActionLoading = false, pinActionSuccess = true) }
                        }
                        is Resource.Error -> {
                            _uiState.update { it.copy(pinActionLoading = false, pinActionError = result.message) }
                        }
                        is Resource.Loading -> {}
                    }
                }
            }
        }

        fun resetPinActionState() {
            _uiState.update { it.copy(pinActionSuccess = false, pinActionError = null) }
        }
    }
