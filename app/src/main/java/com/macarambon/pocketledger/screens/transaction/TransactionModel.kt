package com.macarambon.pocketledger.screens.transaction

import android.content.Context
import com.macarambon.pocketledger.data.PocketLedgerResult
import com.macarambon.pocketledger.data.local.PocketLedgerDatabase
import com.macarambon.pocketledger.data.local.entity.CategoryEntity
import com.macarambon.pocketledger.data.local.entity.TransactionType
import com.macarambon.pocketledger.data.local.entity.WalletEntity
import com.macarambon.pocketledger.data.local.helpers.TransactionHelper
import com.macarambon.pocketledger.data.repository.AuthStore

class TransactionModel(
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

    suspend fun getCategories(): List<CategoryEntity> =
        db.categoryDao().getAll()

    suspend fun addTransaction(
        context: Context,
        userId: Long,
        walletId: Long,
        categoryId: Long,
        amount: Double,
        date: String,
        type: TransactionType,
        appliedInterestRate: Double?,
    ): PocketLedgerResult<Long> = helper.addTransaction(
        context, userId, walletId, categoryId, amount, date, type, appliedInterestRate,
    )
}
