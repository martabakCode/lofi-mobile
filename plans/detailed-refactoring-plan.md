# Detailed Refactoring Plan - Lofi Android App

## Executive Summary

Dokumen ini berisi rencana detail untuk refactoring codebase Lofi Android app sesuai dengan requirement yang diberikan. Rencana ini mencakup perbaikan BaseResponse, penanganan hardcoded strings, rename PreferencesManager, dan penambahan unit tests.

---

## 1. BaseResponse Refactoring

### Current State
```kotlin
// core/network/BaseResponse.kt
data class BaseResponse<T>(
    @SerializedName("success")
    val success: Boolean,
    @SerializedName("message")
    val message: String,
    @SerializedName("data")
    val data: T? = null,
    @SerializedName("errors")
    val errors: Any? = null,
)
```

Saat ini `BaseResponse` hanya digunakan untuk API response dan tidak memiliki wrapper untuk local database operations.

### Target State
Buat sealed class yang dapat menangani baik API response maupun local database operations:

```kotlin
// core/common/result/OperationResult.kt
sealed class OperationResult<out T> {
    data class Success<T>(
        val data: T,
        val source: DataSource = DataSource.REMOTE
    ) : OperationResult<T>()
    
    data class Error(
        val error: ErrorType,
        val source: DataSource = DataSource.REMOTE
    ) : OperationResult<Nothing>()
    
    object Loading : OperationResult<Nothing>()
    
    enum class DataSource { REMOTE, LOCAL }
}

// Extension untuk convert BaseResponse ke OperationResult
fun <T> BaseResponse<T>.toOperationResult(): OperationResult<T> {
    return if (success && data != null) {
        OperationResult.Success(data, OperationResult.DataSource.REMOTE)
    } else {
        OperationResult.Error(
            ErrorType.BusinessError(
                code = "API_ERROR",
                message = message
            ),
            OperationResult.DataSource.REMOTE
        )
    }
}
```

### Files to Modify
1. `core/network/BaseResponse.kt` - Tambahkan extension functions
2. `core/common/result/OperationResult.kt` - Buat file baru
3. Update semua RepositoryImpl untuk menggunakan pattern baru

---

## 2. PreferencesManager → DataStoreManager Rename

### Current State
File: `data/local/datastore/PreferencesManager.kt`

### Target State
1. Rename class dari `PreferencesManager` menjadi `DataStoreManager`
2. Update semua reference di:
   - `core/di/AppModule.kt`
   - `data/repository/AuthRepositoryImpl.kt`
   - `data/repository/UserRepositoryImpl.kt`
   - `ui/features/auth/login/LoginViewModel.kt`
   - `ui/features/auth/biometric/BiometricLoginViewModel.kt`
   - Dan semua file lain yang menggunakan PreferencesManager

### Migration Strategy
```kotlin
// Old
@Singleton
class PreferencesManager @Inject constructor(...)

// New
@Singleton
class DataStoreManager @Inject constructor(...)
```

---

## 3. Hardcoded Strings Extraction

### Analysis Results
Ditemukan 300+ hardcoded strings yang perlu diekstrak ke `strings.xml`.

### Categories of Strings

#### A. Validation Messages
- "Email cannot be empty"
- "Invalid email format"
- "Password cannot be empty"
- "Password must be at least 6 characters"
- "Full name is required"
- "Phone number is required"
- "Username is required"
- "Passwords do not match"

#### B. Error Messages
- "Login failed"
- "Registration failed"
- "Failed to load profile"
- "User data not found"
- "Network error occurred"
- "Connection timed out"
- "Server error occurred"
- "An unexpected error occurred"

#### C. UI Labels
- "Welcome Back"
- "Create Account"
- "Sign In"
- "Register"
- "Forgot Password?"
- "Change Password"
- "Profile Information"
- "Personal Information"

#### D. Button Texts
- "Submit"
- "Save"
- "Cancel"
- "Retry"
- "Continue"
- "Back"
- "OK"

### Target State
```xml
<!-- strings.xml -->
<string name="validation_email_empty">Email cannot be empty</string>
<string name="validation_email_invalid">Invalid email format</string>
<string name="validation_password_empty">Password cannot be empty</string>
<string name="validation_password_length">Password must be at least %1$d characters</string>
<string name="error_login_failed">Login failed</string>
<string name="error_network">Network error occurred. Please check your connection and try again.</string>
<!-- etc -->
```

### Files to Update
1. `app/src/main/res/values/strings.xml` - Tambahkan semua strings
2. All UI files in `ui/features/` - Replace hardcoded strings dengan `stringResource(R.string.xxx)`
3. All ViewModel files - Inject `Resources` atau pindahkan string ke UI layer

---

## 4. Unit Test Implementation

### Current Test Coverage
- `AuthRepositoryImplTest.kt` - Partial
- `LoanRepositoryImplTest.kt` - Partial
- `LoanRemoteDataSourceTest.kt` - Partial
- `LoginViewModelTest.kt` - Partial
- `HomeViewModelTest.kt` - Partial
- `ApplyLoanViewModelTest.kt` - Partial

### Missing Tests

#### A. ViewModel Tests
- `RegisterViewModelTest.kt`
- `LoanDetailViewModelTest.kt`
- `LoanHistoryViewModelTest.kt`
- `LoanTnCViewModelTest.kt`
- `ProfileViewModelTest.kt`
- `EditProfileViewModelTest.kt`
- `ChangePasswordViewModelTest.kt`
- `BiometricLoginViewModelTest.kt`
- `HomeViewModelTest.kt` (expand)

#### B. UseCase Tests
- `LoginUseCaseTest.kt`
- `RegisterUseCaseTest.kt`
- `LogoutUseCaseTest.kt`
- `ChangePasswordUseCaseTest.kt`
- `GoogleAuthUseCaseTest.kt`
- `GetFirebaseIdTokenUseCaseTest.kt`
- `GetUserUseCaseTest.kt`
- `GetUserProfileUseCaseTest.kt`
- `UpdateProfileUseCaseTest.kt`
- `UploadProfilePictureUseCaseTest.kt`
- `GetProductsUseCaseTest.kt`
- `GetNotificationsUseCaseTest.kt`
- `SubmitLoanUseCaseTest.kt`
- `GetLoanDetailUseCaseTest.kt`
- `GetMyLoansUseCaseTest.kt` (expand)
- `SaveProfileDraftUseCaseTest.kt`
- `GetProfileDraftUseCaseTest.kt`
- `ClearProfileDraftUseCaseTest.kt`

#### C. Repository Tests
- `UserRepositoryImplTest.kt`
- `NotificationRepositoryImplTest.kt`
- `ProductRepositoryImplTest.kt`
- `RegionRepositoryImplTest.kt`
- `LoanRepositoryImplTest.kt` (expand)

#### D. DataSource Tests
- `LoanLocalDataSourceTest.kt`
- `NotificationRemoteDataSourceTest.kt`

### Test Structure Template
```kotlin
@ExperimentalCoroutinesApi
class XxxViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @MockK
    private lateinit var useCase: XxxUseCase

    private lateinit var viewModel: XxxViewModel

    @Before
    fun setup() {
        MockKAnnotations.init(this)
        viewModel = XxxViewModel(useCase)
    }

    @Test
    fun `initial state should be correct`() = runTest { }

    @Test
    fun `success case should update state correctly`() = runTest { }

    @Test
    fun `error case should update state correctly`() = runTest { }

    @Test
    fun `loading state should be emitted`() = runTest { }
}
```

---

## 5. Dependency Verification (libs.versions.toml)

### Current State
Dependencies sudah terorganisir dengan baik menggunakan version catalog.

### Verification Checklist
- [x] All dependencies use version refs
- [x] Bundles defined for related dependencies
- [x] Plugins properly configured
- [x] No hardcoded versions in build.gradle.kts

### Bundles to Verify
```toml
[bundles]
compose = [...]
hilt = [...]
network = [...]
room = [...]
testing = [...]
hardware = [...]
```

---

## 6. BASE_URL Configuration Verification

### Current State
```kotlin
// app/build.gradle.kts
val baseUrl = project.findProperty("BASE_URL") as? String ?: "\"\""
buildConfigField("String", "BASE_URL", baseUrl)
```

```properties
# gradle.properties
BASE_URL="http://192.168.100.66:8080/api/v1/"
```

### Status: ✓ Already properly configured

---

## 7. CI/CD Workflow Verification

### Current State (`.github/workflows/android.yml`)
Already includes:
1. Spotless check
2. Unit tests
3. Build debug APK
4. Build release APK
5. Test reporting
6. Artifact upload

### Status: ✓ Already properly configured

---

## 8. Architecture Verification

### Current Architecture Flow
```
UI (Compose Screen)
    ↓
ViewModel (State Holder)
    ↓
UseCase (Business Logic)
    ↓
Repository Interface (Domain Layer)
    ↓
Repository Implementation (Data Layer)
    ↓
DataSource (Remote/Local)
    ↓
Service (API/Database)
```

### Verification Checklist
- [x] UI layer only interacts with ViewModel
- [x] ViewModel uses UseCase for business logic
- [x] UseCase uses Repository interface
- [x] RepositoryImpl uses DataSource
- [x] DataSource interacts with Service/Database
- [x] Proper dependency injection with Hilt

### Status: ✓ Architecture properly implemented

---

## Implementation Priority

### Phase 1: Core Changes (High Priority)
1. BaseResponse refactoring
2. PreferencesManager rename
3. Hardcoded strings extraction

### Phase 2: Testing (High Priority)
1. ViewModel tests
2. UseCase tests
3. Repository tests
4. DataSource tests

### Phase 3: Verification (Medium Priority)
1. Dependency verification
2. CI/CD verification
3. Architecture verification

---

## Files to Create/Modify Summary

### New Files
1. `core/common/result/OperationResult.kt`
2. `core/common/result/ResultExtensions.kt`
3. Multiple test files (see section 4)

### Modified Files
1. `core/network/BaseResponse.kt`
2. `data/local/datastore/PreferencesManager.kt` → `DataStoreManager.kt`
3. `core/di/AppModule.kt`
4. `app/src/main/res/values/strings.xml`
5. All UI files in `ui/features/`
6. All ViewModel files
7. All RepositoryImpl files

---

## Success Criteria

1. ✓ Tidak ada hardcoded strings di codebase (kecuali constants yang memang sengaja)
2. ✓ BaseResponse dapat menangani API dan local database responses
3. ✓ PreferencesManager sudah di-rename menjadi DataStoreManager
4. ✓ Unit test coverage > 80% untuk ViewModel, UseCase, Repository, DataSource
5. ✓ Semua dependency terdaftar di libs.versions.toml
6. ✓ BASE_URL di gradle.properties
7. ✓ CI/CD pipeline berjalan sukses (spotless, unit test, build)
8. ✓ Architecture pattern terjaga (UI → ViewModel → UseCase → Repository → DataSource)
