package com.loanfinancial.lofi.core.navigation

object Routes {
    const val SPLASH = "splash"
    const val HOME = "home"
    const val REGISTER = "register"
    const val FORGOT_PASSWORD = "forgot_password"
    const val LOGIN = "login"
    const val BIOMETRIC_LOGIN = "biometric_login"
    const val DASHBOARD = "dashboard" // New entry point
    const val LOAN_DETAIL = "loan_detail/{loanId}"
    const val NOTIFICATIONS = "notifications"
    const val NOTIFICATION_DETAIL = "notification_detail/{notificationId}"
    const val CHANGE_PASSWORD = "change_password"
    const val EDIT_PROFILE = "edit_profile"
    const val SET_PIN = "set_pin"
    const val SET_PIN_WITH_ARGS = "set_pin?fromProfile={fromProfile}"
    const val DRAFT_LIST = "draft_list"
    const val APPLY_LOAN_BASE = "apply_loan"
    const val APPLY_LOAN = "apply_loan?draftId={draftId}"
    const val DOCUMENT_UPLOAD_BASE = "document_upload"
    const val DOCUMENT_UPLOAD = "document_upload?draftId={draftId}"
    const val LOAN_PREVIEW = "loan_preview"
    const val LOAN_TNC = "loan_tnc"
    const val PROFILE_DETAIL = "profile_detail"
    const val COMPLETE_PROFILE = "complete_profile"
    const val ONBOARDING = "onboarding"
    const val SET_GOOGLE_PIN = "set_google_pin"
    const val CHANGE_GOOGLE_PIN = "change_google_pin"

    // Helper functions to create routes with parameters
    fun applyLoan(draftId: String? = null): String {
        return if (draftId != null) {
            "apply_loan?draftId=$draftId"
        } else {
            "apply_loan"
        }
    }

    fun documentUpload(draftId: String? = null): String {
        return if (draftId != null) {
            "document_upload?draftId=$draftId"
        } else {
            "document_upload"
        }
    }

    fun loanDetail(loanId: String): String = "loan_detail/$loanId"

    fun notificationDetail(notificationId: String): String = "notification_detail/$notificationId"

    fun setPin(fromProfile: Boolean = false): String {
        return "set_pin?fromProfile=$fromProfile"
    }

    fun setGooglePin(): String = SET_GOOGLE_PIN
    fun changeGooglePin(): String = CHANGE_GOOGLE_PIN
}
