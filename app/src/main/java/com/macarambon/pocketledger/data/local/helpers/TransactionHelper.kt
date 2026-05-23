package com.macarambon.pocketledger.data.local.helpers

import android.content.Context
import androidx.room.withTransaction
import com.macarambon.pocketledger.R
import com.macarambon.pocketledger.data.PocketLedgerResult
import com.macarambon.pocketledger.data.local.PocketLedgerDatabase
import com.macarambon.pocketledger.data.local.entity.CategoryEntity
import com.macarambon.pocketledger.data.local.entity.TransactionEntity
import com.macarambon.pocketledger.data.local.entity.TransactionType
import com.macarambon.pocketledger.data.local.entity.WalletEntity
import com.macarambon.pocketledger.data.local.entity.WalletType
import com.macarambon.pocketledger.data.rules.BusinessRules

object CategorySeeder {
    val DEFAULT_CATEGORY_NAMES = listOf(
        "Food & Dining",
        "Transportation",
        "Salary",
        "Tuition",
        "Subscriptions",
        "Shopping",
        "Utilities",
        "Entertainment",
    )

    suspend fun seedIfEmpty(db: PocketLedgerDatabase) {
        if (db.categoryDao().count() > 0) return
        db.categoryDao().insertAll(
            DEFAULT_CATEGORY_NAMES.map { name ->
                CategoryEntity(name = name)
            },
        )
    }
}

class TransactionHelper(private val db: PocketLedgerDatabase) {

    suspend fun addTransaction(
        context: Context,
        userId: Long,
        walletId: Long,
        categoryId: Long,
        amount: Double,
        dateTime: String,
        type: TransactionType,
        appliedInterestRate: Double?,
    ): PocketLedgerResult<Long> {
        if (!BusinessRules.isValidTransactionAmount(amount)) {
            return PocketLedgerResult.Err.Validation(context.getString(R.string.error_invalid_amount))
        }
        if (BusinessRules.parseTransactionDateTime(dateTime) == null) {
            return PocketLedgerResult.Err.Validation(context.getString(R.string.error_invalid_datetime))
        }
        if (BusinessRules.isFutureTransactionDateTime(dateTime)) {
            return PocketLedgerResult.Err.Validation(context.getString(R.string.error_future_date))
        }

        val normalizedRate = when (type) {
            TransactionType.INTEREST -> {
                if (appliedInterestRate == null || !BusinessRules.isValidInterestRate(appliedInterestRate)) {
                    return PocketLedgerResult.Err.Validation(
                        context.getString(R.string.error_applied_interest_required),
                    )
                }
                appliedInterestRate
            }
            TransactionType.INCOME, TransactionType.EXPENSE -> {
                if (appliedInterestRate != null) {
                    return PocketLedgerResult.Err.Validation(
                        context.getString(R.string.error_applied_interest_not_allowed),
                    )
                }
                null
            }
        }

        val wallet = db.walletDao().findById(walletId)
            ?: return PocketLedgerResult.Err.Validation(context.getString(R.string.error_wallet_not_found))
        if (wallet.userId != userId) {
            return PocketLedgerResult.Err.Validation(context.getString(R.string.error_invalid_wallet))
        }
        if (db.categoryDao().findById(categoryId) == null) {
            return PocketLedgerResult.Err.Validation(context.getString(R.string.error_category_not_found))
        }

        val transaction = TransactionEntity(
            userId = userId,
            walletId = walletId,
            categoryId = categoryId,
            amount = amount,
            date = dateTime,
            type = type,
            appliedInterestRate = BusinessRules.appliedInterestRateOrNull(type, normalizedRate),
        )

        val delta = transactionDelta(type, amount)
        val newBalance = wallet.currentAmount + delta
        if (type == TransactionType.EXPENSE && newBalance < 0) {
            return PocketLedgerResult.Err.Validation(context.getString(R.string.error_insufficient_balance))
        }

        val id = db.withTransaction {
            val insertedId = db.transactionDao().insert(transaction)
            db.walletDao().update(wallet.copy(currentAmount = newBalance))
            insertedId
        }
        return PocketLedgerResult.Ok(id)
    }

    suspend fun removeTransaction(
        context: Context,
        userId: Long,
        transactionId: Long,
    ): PocketLedgerResult<Unit> {
        val transaction = db.transactionDao().findById(transactionId)
            ?: return PocketLedgerResult.Err.Validation(context.getString(R.string.error_transaction_not_found))
        if (transaction.userId != userId) {
            return PocketLedgerResult.Err.Validation(context.getString(R.string.error_invalid_transaction))
        }
        val wallet = db.walletDao().findById(transaction.walletId)
            ?: return PocketLedgerResult.Err.Validation(context.getString(R.string.error_wallet_not_found))

        val delta = transactionDelta(transaction.type, transaction.amount)
        val newBalance = wallet.currentAmount - delta
        if (newBalance < 0) {
            return PocketLedgerResult.Err.Validation(context.getString(R.string.error_insufficient_balance_delete))
        }

        db.withTransaction {
            db.transactionDao().deleteById(transactionId)
            db.walletDao().update(wallet.copy(currentAmount = newBalance))
        }
        return PocketLedgerResult.Ok(Unit)
    }

    private fun transactionDelta(type: TransactionType, amount: Double): Double =
        when (type) {
            TransactionType.INCOME, TransactionType.INTEREST -> amount
            TransactionType.EXPENSE -> -amount
        }

    suspend fun createWallet(
        context: Context,
        userId: Long,
        name: String,
        startingAmount: Double,
        type: WalletType,
        interestRate: Double?,
        interestFrequency: com.macarambon.pocketledger.data.local.entity.InterestFrequency?,
    ): PocketLedgerResult<Long> {
        if (!BusinessRules.isValidWalletName(name)) {
            return PocketLedgerResult.Err.Validation(context.getString(R.string.error_field_required))
        }
        if (!BusinessRules.isValidStartingBalance(startingAmount)) {
            return PocketLedgerResult.Err.Validation(context.getString(R.string.error_invalid_amount))
        }

        when {
            BusinessRules.requiresWalletInterest(type) -> {
                if (interestRate == null || interestFrequency == null) {
                    return PocketLedgerResult.Err.Validation(context.getString(R.string.error_interest_required))
                }
                if (!BusinessRules.isValidInterestRate(interestRate)) {
                    return PocketLedgerResult.Err.Validation(context.getString(R.string.error_interest_rate_range))
                }
            }
            interestRate != null || interestFrequency != null -> {
                return PocketLedgerResult.Err.Validation(context.getString(R.string.error_interest_not_allowed))
            }
        }

        val wallet = WalletEntity(
            userId = userId,
            name = name.trim(),
            startingAmount = startingAmount,
            currentAmount = startingAmount,
            type = type,
            interestRate = BusinessRules.walletInterestRateOrNull(type, interestRate),
            interestFrequency = BusinessRules.walletInterestFrequencyOrNull(type, interestFrequency),
        )
        val id = db.walletDao().insert(wallet)
        return PocketLedgerResult.Ok(id)
    }
}
