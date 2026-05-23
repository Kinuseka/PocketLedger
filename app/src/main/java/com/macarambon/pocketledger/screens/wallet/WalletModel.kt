package com.macarambon.pocketledger.screens.wallet

import android.content.Context
import com.macarambon.pocketledger.data.PocketLedgerResult
import com.macarambon.pocketledger.data.local.PocketLedgerDatabase
import com.macarambon.pocketledger.data.local.entity.InterestFrequency
import com.macarambon.pocketledger.data.local.entity.WalletEntity
import com.macarambon.pocketledger.data.local.entity.WalletType
import com.macarambon.pocketledger.data.local.helpers.TransactionHelper
import com.macarambon.pocketledger.data.repository.AuthStore

class WalletModel(
    private val authStore: AuthStore,
    private val db: PocketLedgerDatabase,
) {
    private val helper = TransactionHelper(db)

    suspend fun getCurrentUserId(context: Context): Long? {
        val id = authStore.getCurrentUserId(context)
        return if (id > 0L) id else null
    }

    suspend fun getWallets(userId: Long): List<WalletEntity> =
        db.walletDao().getByUser(userId)

    suspend fun createWallet(
        context: Context,
        userId: Long,
        name: String,
        startingAmount: Double,
        type: WalletType,
        interestRate: Double?,
        interestFrequency: InterestFrequency?,
    ): PocketLedgerResult<Long> = helper.createWallet(
        context, userId, name, startingAmount, type, interestRate, interestFrequency,
    )
}
