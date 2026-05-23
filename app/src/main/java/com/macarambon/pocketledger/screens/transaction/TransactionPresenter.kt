package com.macarambon.pocketledger.screens.transaction

import android.content.Context
import com.macarambon.pocketledger.R
import com.macarambon.pocketledger.data.local.entity.TransactionType
import com.macarambon.pocketledger.data.notifyErrorIfNotOk
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class TransactionPresenter(
    private val view: TransactionContract.View,
    private val model: TransactionModel,
    private val scope: CoroutineScope,
    private val context: Context,
) : TransactionContract.Presenter {

    private var wallets: List<com.macarambon.pocketledger.data.local.entity.WalletEntity> = emptyList()
    private var categories: List<com.macarambon.pocketledger.data.local.entity.CategoryEntity> = emptyList()

    override fun loadFormData() {
        scope.launch {
            val userId = withContext(Dispatchers.IO) { model.getCurrentUserId(context) }
            if (userId == null) {
                view.showToast(context.getString(R.string.error_session_expired))
                return@launch
            }
            wallets = withContext(Dispatchers.IO) { model.getWallets(userId) }
            categories = withContext(Dispatchers.IO) { model.getCategories() }
            view.showWalletOptions(wallets.map { it.name })
            view.showCategoryOptions(categories.map { it.name })
        }
    }

    override fun onTransactionTypeSelected(typeIndex: Int) {
        view.setAppliedInterestVisible(typeIndex == 2)
    }

    override fun onSubmitClicked(
        walletIndex: Int,
        categoryIndex: Int,
        amount: String,
        date: String,
        typeIndex: Int,
        appliedInterestRate: String,
    ) {
        if (wallets.isEmpty() || categories.isEmpty()) {
            view.showToast("Please create a wallet and category first.")
            return
        }
        val parsedAmount = amount.toDoubleOrNull()
        if (parsedAmount == null || parsedAmount <= 0) {
            view.showToast(context.getString(R.string.error_invalid_amount))
            return
        }
        if (date.isBlank()) {
            view.showToast(context.getString(R.string.error_field_required))
            return
        }
        val wallet = wallets.getOrNull(walletIndex) ?: return
        val category = categories.getOrNull(categoryIndex) ?: return
        val type = transactionTypes.getOrNull(typeIndex) ?: TransactionType.EXPENSE
        val rate = if (type == TransactionType.INTEREST) {
            appliedInterestRate.toDoubleOrNull()
        } else {
            null
        }

        view.setSubmitInProgress(true)
        scope.launch {
            val userId = withContext(Dispatchers.IO) { model.getCurrentUserId(context) }
            if (userId == null) {
                withContext(Dispatchers.Main) {
                    view.setSubmitInProgress(false)
                    view.showToast(context.getString(R.string.error_session_expired))
                }
                return@launch
            }
            val result = withContext(Dispatchers.IO) {
                model.addTransaction(
                    context, userId, wallet.id, category.id,
                    parsedAmount, date, type, rate,
                )
            }
            withContext(Dispatchers.Main) {
                view.setSubmitInProgress(false)
                when (result) {
                    is com.macarambon.pocketledger.data.PocketLedgerResult.Ok -> {
                        view.showToast(context.getString(R.string.success_transaction_added))
                        view.navigateToDashboard()
                    }
                    else -> result.notifyErrorIfNotOk { view.showToast(it) }
                }
            }
        }
    }

    companion object {
        val transactionTypes = listOf(
            TransactionType.INCOME,
            TransactionType.EXPENSE,
            TransactionType.INTEREST,
        )
    }
}
