package com.macarambon.pocketledger.screens.login

import android.content.Context
import com.macarambon.pocketledger.data.repository.AuthStore

class LoginModel(private val authStore: AuthStore) {
    suspend fun signIn(context: Context, email: String, password: String) =
        authStore.signInWithEmail(context, email, password)
}
