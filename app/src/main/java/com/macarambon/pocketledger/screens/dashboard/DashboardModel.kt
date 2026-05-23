package com.macarambon.pocketledger.screens.dashboard

import android.content.Context
import com.macarambon.pocketledger.data.PocketLedgerResult
import com.macarambon.pocketledger.data.local.LedgerEntry
import com.macarambon.pocketledger.data.local.PocketLedgerDatabase
import com.macarambon.pocketledger.data.local.entity.CategoryEntity
import com.macarambon.pocketledger.data.local.entity.TransactionType
import com.macarambon.pocketledger.data.local.entity.UserEntity
import com.macarambon.pocketledger.data.local.entity.WalletEntity
import com.macarambon.pocketledger.data.local.helpers.CategoryHelper
import com.macarambon.pocketledger.data.local.helpers.TransactionHelper
import com.macarambon.pocketledger.data.repository.AuthStore

class DashboardModel(
    private val authStore: AuthStore,
    private val db: PocketLedgerDatabase,
) {
    private val helper = TransactionHelper(db)
    private val categoryHelper = CategoryHelper(db)

    suspend fun getCurrentUser(context: Context): UserEntity? =
        authStore.getCurrentUser(context)

    suspend fun getWallets(userId: Long): List<WalletEntity> =
        db.walletDao().getByUser(userId)

    suspend fun getCategories(): List<CategoryEntity> =
        db.categoryDao().getAll()

    suspend fun getBalanceSummary(userId: Long, walletId: Long?): BalanceSummary {
        return if (walletId != null) {
            val wallet = db.walletDao().findById(walletId)
                ?: return BalanceSummary(0.0, 0.0, 0.0)
            BalanceSummary(
                balance = wallet.currentAmount,
                startingTotal = wallet.startingAmount,
                netChange = wallet.currentAmount - wallet.startingAmount,
            )
        } else {
            val balance = db.walletDao().getNetWorth(userId)
            val starting = db.walletDao().getStartingTotal(userId)
            BalanceSummary(balance, starting, balance - starting)
        }
    }

    suspend fun getLedger(userId: Long, walletId: Long?, limit: Int? = null): List<LedgerEntry> {
        val rows = if (walletId != null) {
            db.transactionDao().getLedgerEntriesByWallet(userId, walletId)
        } else {
            db.transactionDao().getLedgerEntries(userId)
        }
        val entries = rows.map { it.toLedgerEntry() }
        return if (limit != null) entries.take(limit) else entries
    }

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

    suspend fun addCategory(context: Context, name: String): PocketLedgerResult<Long> =
        categoryHelper.addCategory(context, name)

    suspend fun removeTransaction(
        context: Context,
        userId: Long,
        transactionId: Long,
    ): PocketLedgerResult<Unit> = helper.removeTransaction(context, userId, transactionId)

    fun logout(context: Context) = authStore.logout(context)
}
