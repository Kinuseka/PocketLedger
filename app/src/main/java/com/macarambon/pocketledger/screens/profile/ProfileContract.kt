package com.macarambon.pocketledger.screens.profile

interface ProfileContract {
    interface View {
        fun showToast(message: String)
        fun showProfile(fullName: String, email: String, netWorth: String)
        fun navigateToLogin()
    }

    interface Presenter {
        fun loadProfile()
    }
}
