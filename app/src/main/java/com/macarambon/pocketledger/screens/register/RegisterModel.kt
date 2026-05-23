package com.macarambon.pocketledger.screens.register

import android.content.Context
import com.macarambon.pocketledger.data.repository.AuthStore

class RegisterModel(private val authStore: AuthStore) {
    suspend fun register(
        context: Context,
        firstName: String,
        middleName: String,
        lastName: String,
        email: String,
        password: String,
    ) = authStore.register(context, firstName, middleName, lastName, email, password)
}
