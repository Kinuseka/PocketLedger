package com.macarambon.pocketledger.screens.login

interface LoginContract {
    interface View {
        fun showToast(message: String)
        fun setLoginInProgress(inProgress: Boolean)
        fun navigateToDashboard()
        fun navigateToRegister()
    }

    interface Presenter {
        fun onLoginSubmitted(email: String, password: String)
    }
}
