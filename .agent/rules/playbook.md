---
trigger: always_on
---

# 📘 Android Engineering Playbook
Project Standards, Architecture & Design Guidelines

---

## 1. Purpose
Dokumen ini menjadi **single source of truth** untuk standar engineering Android, mencakup:
- Arsitektur aplikasi
- Aturan coding dan struktur project
- Prinsip UI/UX berbasis **Apple Human Interface Guidelines (HIG)** yang diadaptasi ke Android
- Konsistensi kualitas kode, desain, dan pengalaman pengguna

Semua engineer **WAJIB** mengikuti playbook ini.

---

## 2. Core Principles
- Human-first design
- Offline-first architecture
- Reusable by default
- Scalable & maintainable
- Testable by design

---

## 3. Architecture Overview

### 3.1 Architecture Pattern — MVVM
Wajib menggunakan **MVVM**:

UI (Compose / XML)
↓ observes
ViewModel (State & Logic)
↓ calls
UseCase (Business Rules)
↓ depends on
Repository (Abstraction)
↓
Data Source (Local / Remote)

yaml
Salin kode

### Layer Responsibilities
| Layer | Responsibility |
|----|----|
| UI | Rendering & user interaction |
| ViewModel | State holder & event handler |
| UseCase | Business rules |
| Repository | Data abstraction |
| Data Source | Local DB / API |

---

## 4. SOLID Engineering Rules

### 4.1 Single Responsibility Principle
- 1 class = 1 responsibility
- ViewModel **tidak boleh**:
  - Mengakses Retrofit langsung
  - Mengakses database langsung

---

### 4.2 Open / Closed Principle
- Gunakan abstraction:
  - Interface
  - Sealed class
  - Extension
- Hindari hard-coded logic di UI

---

### 4.3 Liskov Substitution Principle
- Implementasi interface **tidak mengubah behavior**
- Subclass harus bisa menggantikan parent tanpa side effect

---

### 4.4 Interface Segregation Principle
❌ Buruk:
```kotlin
interface UserRepository {
    fun login()
    fun logout()
    fun getProfile()
}
✅ Baik:

kotlin
Salin kode
interface AuthRepository {
    fun login()
    fun logout()
}

interface ProfileRepository {
    fun getProfile()
}
4.5 Dependency Inversion Principle
Gunakan Dependency Injection (Hilt / Koin)

High-level module hanya bergantung pada abstraction

kotlin
Salin kode
class GetUserUseCase(
    private val repository: UserRepository
)
5. Reusable Component Rules
UI Components
Stateless jika memungkinkan

Tidak bergantung ke screen tertentu

Configurable via parameter

kotlin
Salin kode
@Composable
fun PrimaryButton(
    text: String,
    onClick: () -> Unit,
    enabled: Boolean = true
)
Non-UI Components
UseCase harus reusable lintas feature

Repository harus bisa di-mock

6. Offline-First Strategy
Rules
Local database adalah source of truth

Remote API hanya untuk sinkronisasi

Data Flow
pgsql
Salin kode
UI → ViewModel → UseCase → Repository
→ Local DB (Read/Write)
→ Remote API (Sync)
Requirements
Cache-first

Graceful error handling

UI state jelas:

Loading

Cached

Error

Synced

7. Firebase Integration
Required
Firebase Cloud Messaging (FCM)

Notification Rules
Relevan & kontekstual

Mendukung deep-link

Menghormati permission user

8. Tablet & Large Screen Support
Mandatory
Responsive layout (WindowSizeClass)

Adaptive navigation:

Bottom bar (phone)

Navigation rail / drawer (tablet)

Don’ts
❌ Stretch UI phone ke tablet
❌ Fixed width / height

9. Android-Adapted Apple HIG Rules
9.1 Clarity
Gunakan typography system

Spacing konsisten (8dp grid)

1 screen = 1 tujuan utama

9.2 Deference
Konten adalah fokus utama

UI tidak mendominasi

Hindari over-elevation

9.3 Depth
Elevation hanya untuk hierarchy

Animasi natural & bermakna

9.4 Intuitive Gestures
Gunakan gesture Android standar

Jangan override system gesture

9.5 Consistent Navigation
Back behavior konsisten

Hindari multiple navigation pattern tanpa alasan jelas

9.6 Adaptive Layouts
Support:

Orientation change

Multi-window

Large screen

9.7 Accessibility First
WAJIB:

Content description

Dynamic text

Color contrast (AA)

TalkBack friendly

10. UI State Management
Gunakan single source of truth:

kotlin
Salin kode
sealed class UiState<out T> {
    object Loading : UiState<Nothing>()
    data class Success<T>(val data: T) : UiState<T>()
    data class Error(val message: String) : UiState<Nothing>()
}
11. Package Structure
wasm
Salin kode
feature/
 ├── ui
 ├── viewmodel
 ├── domain
 │   ├── usecase
 │   └── model
 └── data
     ├── repository
     ├── local
     └── remote
12. Code Review Checklist
 Reusable component

 MVVM respected

 SOLID compliant

 Offline-first safe

 Firebase integrated properly

 Tablet responsive

 Accessible

 Clear UI purpose

Jika satu saja gagal → PR ditolak

13. Final Rule
If it’s hard to use, it’s wrong — even if it compiles.