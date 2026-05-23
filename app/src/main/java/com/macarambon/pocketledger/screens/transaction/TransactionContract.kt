package com.macarambon.pocketledger.screens.transaction

interface TransactionContract {
    interface View {
        fun showToast(message: String)
        fun setAppliedInterestVisible(visible: Boolean)
        fun showWalletOptions(names: List<String>)
        fun showCategoryOptions(names: List<String>)
        fun navigateToDashboard()
        fun setSubmitInProgress(inProgress: Boolean)
    }

    interface Presenter {
        fun loadFormData()
        fun onTransactionTypeSelected(typeIndex: Int)
        fun onSubmitClicked(
            walletIndex: Int,
            categoryIndex: Int,
            amount: String,
            date: String,
            typeIndex: Int,
            appliedInterestRate: String,
        )
    }
}
