package com.macarambon.pocketledger.screens.transactionhistory

import com.macarambon.pocketledger.data.local.LedgerEntry

interface TransactionHistoryContract {
    interface View {
        fun showToast(message: String)
        fun showWalletFilterOptions(walletNames: List<String>)
        fun showLedger(entries: List<LedgerEntry>)
        fun navigateToLogin()
    }

    interface Presenter {
        fun loadHistory()
        fun onWalletFilterSelected(index: Int)
        fun onRemoveTransactionClicked(transactionId: Long)
    }
}
