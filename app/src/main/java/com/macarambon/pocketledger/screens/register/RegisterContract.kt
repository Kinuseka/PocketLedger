package com.macarambon.pocketledger.screens.register

interface RegisterContract {
    interface View {
        fun showToast(message: String)
        fun setRegisterInProgress(inProgress: Boolean)
        fun navigateToLogin()
    }

    interface Presenter {
        fun onRegisterSubmitted(
            firstName: String,
            middleName: String,
            lastName: String,
            email: String,
            password: String,
            confirmPassword: String,
        )
    }
}
