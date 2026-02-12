package com.loanfinancial.lofi.ui.features.loan

import com.loanfinancial.lofi.core.media.DocumentType

data class DocumentUploadUiState(
    val documents: Map<DocumentType, DocumentState> = mapOf(
        DocumentType.KTP to DocumentState(),
        DocumentType.NPWP to DocumentState(),
        DocumentType.KK to DocumentState(),
        DocumentType.PAYSLIP to DocumentState(),
        DocumentType.PROOFOFRESIDENCE to DocumentState(),
        DocumentType.BANK_STATEMENT to DocumentState()
    ),
    val isLoading: Boolean = false,
    val error: String? = null,
    val isAllUploaded: Boolean = false,
    val isAllQueued: Boolean = false // All required docs have been selected/queued
)

data class DocumentState(
    val filePath: String? = null,
    val isUploading: Boolean = false,
    val isUploaded: Boolean = false,
    val error: String? = null
)
