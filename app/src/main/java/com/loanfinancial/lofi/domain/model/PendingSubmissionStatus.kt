package com.loanfinancial.lofi.domain.model

enum class PendingSubmissionStatus {
    PENDING, // Menunggu koneksi
    SUBMITTING, // Sedang dikirim
    SUCCESS, // Berhasil
    FAILED, // Gagal setelah 3x retry
    CANCELLED, // Dibatalkan oleh user
}
