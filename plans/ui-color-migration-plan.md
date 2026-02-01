# UI Color Migration Plan - Primary 60:40 Accent

## Ringkasan
Migrasi warna UI aplikasi LoFi dengan perbandingan Primary (Orange) 60% dan Accent (Cyan) 40%.

---

## Skema Warna Baru

### Primary Colors (Orange - 60%)
```kotlin
--primary-50: #fffaec;  → Background terang
    --primary-100: #fff5d3; → Container terang
    --primary-200: #ffe7a5; → Hover terang
    --primary-300: #ffd46d;  → Accent terang
    --primary-400: #ffb632;  → Secondary
    --primary-500: #ff9d0a; → Brand Main (Primary)
    --primary-600: #ff8500; → Brand Hover
    --primary-700: #cc6102; → Container gelap
    --primary-800: #a14b0b; → Text gelap
    --primary-900: #823f0c; → OnPrimaryContainer
    --primary-950: #461e04; → Text sangat gelap
```

### Accent Colors (Cyan - 40%)
```kotlin
    --accent-100: #cafff9; → SecondaryContainer (Light)
    --accent-200: #9cfff8;  → TertiaryContainer (Light)
    --accent-300: #57fff5; → Tertiary (Light)
    --accent-400: #0cfffd;  → Secondary (Light)
    --accent-500: #00e3ea; → Accent Main
    --accent-600: #00b5c4; → Accent Hover / Tertiary (Dark)
    --accent-700: #008d9b; → SecondaryContainer (Dark)
    --accent-800: #0b727f;  → TertiaryContainer (Dark)
    --accent-900: #0e5e6b; → OnSecondaryContainer
    --accent-950: #023f4a; → OnTertiaryContainer
```

---

## Mapping MaterialTheme.colorScheme

### Light Theme
| Role | Warna Lama | Warna Baru | Keterangan |
|------|------------|------------|------------|
| primary | Primary500 | Primary500 | Tetap Orange |
| secondary | Primary600 | Accent500 | **Berubah ke Cyan** |
| secondaryContainer | - | Accent100 | **Baru - Cyan Soft** |
| onSecondaryContainer | - | Accent900 | **Baru** |
| tertiary | - | Accent600 | **Baru - Cyan Hover** |
| tertiaryContainer | - | Accent200 | **Baru** |
| onTertiaryContainer | - | Accent950 | **Baru** |

### Dark Theme
| Role | Warna Lama | Warna Baru | Keterangan |
|------|------------|------------|------------|
| primary | Primary500 | Primary500 | Tetap Orange |
| secondary | Primary400 | Accent400 | **Berubah ke Cyan** |
| secondaryContainer | - | Accent700 | **Baru** |
| onSecondaryContainer | - | White | **Baru** |
| tertiary | - | Accent300 | **Baru** |
| tertiaryContainer | - | Accent800 | **Baru** |
| onTertiaryContainer | - | White | **Baru** |

---

## Daftar File UI yang Perlu Diperiksa

### 1. Components
- [ ] [`LofiButton.kt`](app/src/main/java/com/loanfinancial/lofi/ui/components/LofiButton.kt) - Uses `primary` default
- [ ] [`LofiCard.kt`](app/src/main/java/com/loanfinancial/lofi/ui/components/LofiCard.kt) - Uses `surface`
- [ ] [`LofiLoader.kt`](app/src/main/java/com/loanfinancial/lofi/ui/components/LofiLoader.kt) - Uses `primary`
- [ ] [`LofiTextField.kt`](app/src/main/java/com/loanfinancial/lofi/ui/components/LofiTextField.kt) - Uses `primary` focused
- [ ] [`LofiTopBar.kt`](app/src/main/java/com/loanfinancial/lofi/ui/components/LofiTopBar.kt) - Uses `primary` icons
- [ ] [`SocialAuthButton.kt`](app/src/main/java/com/loanfinancial/lofi/ui/components/SocialAuthButton.kt) - Uses `surface`, `outline`
- [ ] [`SLACountdown.kt`](app/src/main/java/com/loanfinancial/lofi/ui/components/SLACountdown.kt) - Uses hardcoded colors

### 2. Auth Features
- [ ] [`LoginScreen.kt`](app/src/main/java/com/loanfinancial/lofi/ui/features/auth/login/LoginScreen.kt) - Uses `primary`, `secondary`, `background`
- [ ] [`RegisterScreen.kt`](app/src/main/java/com/loanfinancial/lofi/ui/features/auth/register/RegisterScreen.kt) - Uses `primary`, `background`
- [ ] [`BiometricLoginScreen.kt`](app/src/main/java/com/loanfinancial/lofi/ui/features/auth/biometric/BiometricLoginScreen.kt) - Uses `primary`, `errorContainer`
- [ ] [`ForgotPasswordScreen.kt`](app/src/main/java/com/loanfinancial/lofi/ui/screens/auth/ForgotPasswordScreen.kt) - Uses `primary`, `background`

### 3. Dashboard & Navigation
- [ ] [`DashboardScreen.kt`](app/src/main/java/com/loanfinancial/lofi/ui/features/dashboard/DashboardScreen.kt) - Uses `primary` for nav items
- [ ] [`HomeScreen.kt`](app/src/main/java/com/loanfinancial/lofi/ui/features/home/HomeScreen.kt) - Uses `primary`, `secondaryContainer`

### 4. Loan Features
- [ ] [`ApplyLoanScreen.kt`](app/src/main/java/com/loanfinancial/lofi/ui/features/loan/ApplyLoanScreen.kt) - Uses `primary`, `primaryContainer`
- [ ] [`LoanDetailScreen.kt`](app/src/main/java/com/loanfinancial/lofi/ui/features/loan/LoanDetailScreen.kt) - Uses `primary`, `tertiaryContainer`
- [ ] [`LoanHistoryScreen.kt`](app/src/main/java/com/loanfinancial/lofi/ui/features/loan/LoanHistoryScreen.kt) - Uses `primary`, hardcoded status colors
- [ ] [`LoanPreviewScreen.kt`](app/src/main/java/com/loanfinancial/lofi/ui/features/loan/LoanPreviewScreen.kt) - Uses `primary`, `secondary`, `tertiary`
- [ ] [`LoanTnCScreen.kt`](app/src/main/java/com/loanfinancial/lofi/ui/features/loan/LoanTnCScreen.kt) - Uses `primary`, `errorContainer`
- [ ] [`DocumentUploadSection.kt`](app/src/main/java/com/loanfinancial/lofi/ui/features/loan/components/DocumentUploadSection.kt) - Uses `primary`, `error`

### 5. Profile Features
- [ ] [`ProfileScreen.kt`](app/src/main/java/com/loanfinancial/lofi/ui/features/profile/ProfileScreen.kt) - Uses `primary`, `surface`
- [ ] [`ProfileDetailScreen.kt`](app/src/main/java/com/loanfinancial/lofi/ui/features/profile/ProfileDetailScreen.kt) - Uses `primary`, `surface`
- [ ] [`EditProfileScreen.kt`](app/src/main/java/com/loanfinancial/lofi/ui/features/profile/EditProfileScreen.kt) - Uses `primary`, `primaryContainer`
- [ ] [`ChangePasswordScreen.kt`](app/src/main/java/com/loanfinancial/lofi/ui/features/profile/ChangePasswordScreen.kt) - Uses `error`, `background`

### 6. Other Features
- [ ] [`NotificationScreen.kt`](app/src/main/java/com/loanfinancial/lofi/ui/features/notification/NotificationScreen.kt) - Uses `primary`, `secondary` gradient
- [ ] [`NotificationDetailScreen.kt`](app/src/main/java/com/loanfinancial/lofi/ui/features/notification/NotificationDetailScreen.kt) - Uses `primary`, `surface`
- [ ] [`LoanSimulationScreen.kt`](app/src/main/java/com/loanfinancial/lofi/ui/features/simulation/LoanSimulationScreen.kt) - Uses `primary`, `primaryContainer`
- [ ] [`SplashScreen.kt`](app/src/main/java/com/loanfinancial/lofi/ui/features/splash/SplashScreen.kt) - Uses `primary`, `background`

---

## Rekomendasi Perubahan UI

### 1. Gradient & Accent Usage
File yang menggunakan gradient `primary` + `secondary`:
- [`NotificationScreen.kt`](app/src/main/java/com/loanfinancial/lofi/ui/features/notification/NotificationScreen.kt:183-184)
  
**Rekomendasi:** Gradient sekarang akan menjadi Orange + Cyan yang memberikan kontras tinggi dan modern.

### 2. Status Colors (Hardcoded)
File dengan warna status hardcoded:
- [`LoanHistoryScreen.kt`](app/src/main/java/com/loanfinancial/lofi/ui/features/loan/LoanHistoryScreen.kt:165-169) - APPROVED, DISBURSED
- [`HomeScreen.kt`](app/src/main/java/com/loanfinancial/lofi/ui/features/home/HomeScreen.kt:205-210) - APPROVED, DISBURSED, REVIEWED
- [`LoanDetailScreen.kt`](app/src/main/java/com/loanfinancial/lofi/ui/features/loan/LoanDetailScreen.kt:275-283) - getStatusColor()

**Rekomendasi:** Warna status tetap menggunakan warna semantik (Green, Blue, Yellow, Red) karena merepresentasikan state, bukan brand.

### 3. Secondary Container Usage
File yang menggunakan `secondaryContainer`:
- [`HomeScreen.kt`](app/src/main/java/com/loanfinancial/lofi/ui/features/home/HomeScreen.kt:122) - Complete Profile card

**Rekomendasi:** Card "Complete Profile" sekarang akan menggunakan warna Cyan soft (Accent100) sebagai background.

### 4. Tertiary Usage
File yang menggunakan `tertiary`:
- [`LoanPreviewScreen.kt`](app/src/main/java/com/loanfinancial/lofi/ui/features/loan/LoanPreviewScreen.kt:308,314,363,369) - Document Status & Verification Status

**Rekomendasi:** Icon dan title status dokumen sekarang menggunakan warna Cyan (Accent600).

---

## Checklist Implementasi

### Phase 1: Theme Update (DONE)
- [x] Update [`Color.kt`](app/src/main/java/com/loanfinancial/lofi/ui/theme/Color.kt) - Tambah Accent colors
- [x] Update [`Theme.kt`](app/src/main/java/com/loanfinancial/lofi/ui/theme/Theme.kt) - Mapping secondary ke Accent

### Phase 2: Component Review
- [ ] Review semua Components
- [ ] Pastikan tidak ada hardcoded colors yang bertabrakan

### Phase 3: Screen-by-Screen Review
- [ ] Auth Screens
- [ ] Dashboard & Home
- [ ] Loan Screens
- [ ] Profile Screens
- [ ] Notification Screens

### Phase 4: Testing
- [ ] Test Light Theme
- [ ] Test Dark Theme
- [ ] Verifikasi kontras aksesibilitas

---

## Catatan Penting

1. **Perubahan Otomatis:** Semua komponen yang menggunakan `MaterialTheme.colorScheme.secondary` akan otomatis berubah ke Cyan.

2. **Tertiary Color:** Sekarang digunakan untuk elemen tersier dengan warna Cyan yang lebih terang/gelap tergantung tema.

3. **Primary Tetap:** Warna utama (Orange) tidak berubah, hanya penambahan Accent.

4. **Backward Compatibility:** Tidak ada breaking changes, hanya visual updates.

---

## Visual Preview

### Light Theme
```
Primary:     Orange #FF9D0A ████████
Secondary:   Cyan   #00E3EA ████████  
Tertiary:    Cyan   #00B5C4 ████████
Background:  White  #F8FAFC ████████
Surface:     White  #FFFFFF ████████
```

### Dark Theme
```
Primary:     Orange #FF9D0A ████████
Secondary:   Cyan   #0CFFFD ████████
Tertiary:    Cyan   #57FFF5 ████████
Background:  Dark   #0F172A ████████
Surface:     Dark   #1E293B ████████
```
