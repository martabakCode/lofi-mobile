package com.loanfinancial.lofi.ui.features.onboarding

import com.loanfinancial.lofi.R

/**
 * Data class representing a single onboarding slide.
 *
 * @property id Unique identifier for the slide
 * @property title The title text displayed on the slide
 * @property description The description text displayed on the slide
 * @property illustrationResId Resource ID for the slide's illustration drawable
 * @property backgroundColor Background color for the slide (using LoFi brand colors)
 * @property benefits Optional list of benefit items for benefit-style slides
 * @property steps Optional list of steps for how-it-works-style slides
 * @property trustBadges Optional list of trust badges for security-style slides
 * @property isLastSlide Whether this is the final slide in the onboarding flow
 */
data class OnboardingSlide(
    val id: Int,
    val title: String,
    val description: String,
    val illustrationResId: Int,
    val backgroundColor: Long,
    val benefits: List<String>? = null,
    val steps: List<OnboardingStep>? = null,
    val trustBadges: List<String>? = null,
    val isLastSlide: Boolean = false
)

/**
 * Data class representing a step in the onboarding process.
 *
 * @property number Step number (displayed in a circle)
 * @property title Title of the step
 */
data class OnboardingStep(
    val number: Int,
    val title: String
)

/**
 * LoFi Brand Colors (matching ui.theme.Color.kt)
 * Primary: Orange (#FF9D0A)
 * Accent: Cyan (#00E3EA)
 * Secondary: Deep Blue (#1E3A5F)
 */
private val LofiPrimary = 0xFFFF9D0A  // Orange brand color
private val LofiAccent = 0xFF00E3EA   // Cyan accent color
private val LofiDeepBlue = 0xFF1E3A5F // Deep blue for trust/security
private val LofiTeal = 0xFF00897B     // Teal for success/growth
private val LofiPurple = 0xFF7C4DFF   // Purple for features

/**
 * List of onboarding slides for the LoFi loan application.
 *
 * The onboarding flow consists of 5 slides:
 * 1. Welcome - Introduction to LoFi
 * 2. Benefits - Key advantages of using LoFi
 * 3. How It Works - Step-by-step process
 * 4. Security - Trust and security features
 * 5. Call to Action - Final slide to get started
 */
val onboardingSlides = listOf(
    OnboardingSlide(
        id = 0,
        title = "Selamat Datang di LOFI",
        description = "Solusi peminjaman digital yang cepat, mudah, dan terpercaya untuk kebutuhan finansial Anda. Ajukan pinjaman kapan saja, di mana saja.",
        illustrationResId = R.drawable.onboarding_welcome,
        backgroundColor = LofiPrimary
    ),
    OnboardingSlide(
        id = 1,
        title = "Pinjaman Mudah & Cepat",
        description = "Nikmati kemudahan mengajukan pinjaman dengan proses yang sederhana dan persetujuan yang cepat.",
        illustrationResId = R.drawable.onboarding_benefits,
        backgroundColor = LofiPurple,
        benefits = listOf(
            "✓ Persetujuan dalam 24 jam",
            "✓ Bunga kompetitif 0.8% - 1.5%",
            "✓ Tanpa jaminan diperlukan",
            "✓ Dana cair langsung ke rekening"
        )
    ),
    OnboardingSlide(
        id = 2,
        title = "Cara Mengajukan Pinjaman",
        description = "Hanya butuh 4 langkah sederhana untuk mendapatkan dana segar yang Anda butuhkan:",
        illustrationResId = R.drawable.onboarding_how_it_works,
        backgroundColor = LofiTeal,
        steps = listOf(
            OnboardingStep(1, "Daftar & Verifikasi Akun"),
            OnboardingStep(2, "Lengkapi Data Profil"),
            OnboardingStep(3, "Ajukan Pinjaman Anda"),
            OnboardingStep(4, "Dana Langsung Cair")
        )
    ),
    OnboardingSlide(
        id = 3,
        title = "Aman & Terpercaya",
        description = "Keamanan data Anda adalah prioritas kami. LOFI telah terdaftar dan diawasi oleh OJK dengan standar keamanan terbaik.",
        illustrationResId = R.drawable.onboarding_security,
        backgroundColor = LofiDeepBlue,
        trustBadges = listOf(
            "Terdaftar OJK",
            "Enkripsi SSL 256-bit",
            "ISO 27001 Certified",
            "Data Privacy Protection"
        )
    ),
    OnboardingSlide(
        id = 4,
        title = "Siap Memulai?",
        description = "Bergabunglah dengan ribuan pengguna LOFI yang telah merasakan kemudahan peminjaman digital. Mulai perjalanan finansial Anda sekarang!",
        illustrationResId = R.drawable.onboarding_cta,
        backgroundColor = LofiAccent,
        isLastSlide = true
    )
)
