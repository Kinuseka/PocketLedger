package com.macarambon.pocketledger.screens.profile

import android.content.Context
import com.macarambon.pocketledger.data.local.PocketLedgerDatabase
import com.macarambon.pocketledger.data.local.entity.UserEntity
import com.macarambon.pocketledger.data.repository.AuthStore

class ProfileModel(
    private val authStore: AuthStore,
    private val db: PocketLedgerDatabase,
) {
    suspend fun getCurrentUser(context: Context): UserEntity? =
        authStore.getCurrentUser(context)

    suspend fun getNetWorth(userId: Long): Double =
        db.walletDao().getNetWorth(userId)
}
