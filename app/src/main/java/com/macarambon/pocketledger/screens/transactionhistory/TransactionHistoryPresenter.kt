package com.macarambon.pocketledger.screens.transactionhistory

import android.content.Context
import com.macarambon.pocketledger.R
import com.macarambon.pocketledger.data.errorMessage
import com.macarambon.pocketledger.data.local.entity.WalletEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class TransactionHistoryPresenter(
    private val view: TransactionHistoryContract.View,
    private val model: TransactionHistoryModel,
    private val scope: CoroutineScope,
    private val context: Context,
) : TransactionHistoryContract.Presenter {

    private var userId: Long? = null
    private var wallets: List<WalletEntity> = emptyList()
    private var selectedWalletId: Long? = null

    override fun loadHistory() {
        scope.launch {
            val user = withContext(Dispatchers.IO) { model.getCurrentUser(context) }
            if (user == null) {
                view.showToast(context.getString(R.string.error_session_expired))
                view.navigateToLogin()
                return@launch
            }
            userId = user.id
            wallets = withContext(Dispatchers.IO) { model.getWallets(user.id) }
            withContext(Dispatchers.Main) {
                view.showWalletFilterOptions(
                    listOf(context.getString(R.string.label_all_wallets)) + wallets.map { it.name },
                )
            }
            refreshLedger()
        }
    }

    override fun onWalletFilterSelected(index: Int) {
        selectedWalletId = if (index <= 0) null else wallets.getOrNull(index - 1)?.id
        refreshLedger()
    }

    override fun onRemoveTransactionClicked(transactionId: Long) {
        val id = userId ?: return
        scope.launch {
            val result = withContext(Dispatchers.IO) {
                model.removeTransaction(context, id, transactionId)
            }
            when (result) {
                is com.macarambon.pocketledger.data.PocketLedgerResult.Ok -> {
                    view.showToast(context.getString(R.string.success_transaction_removed))
                    refreshLedger()
                }
                else -> view.showToast(result.errorMessage().orEmpty())
            }
        }
    }

    private fun refreshLedger() {
        val id = userId ?: return
        scope.launch {
            val entries = withContext(Dispatchers.IO) {
                model.getLedger(id, selectedWalletId)
            }
            view.showLedger(entries)
        }
    }
}
