package com.macarambon.pocketledger.screens.dashboard

import com.macarambon.pocketledger.data.local.LedgerEntry
import com.macarambon.pocketledger.data.local.entity.CategoryEntity
import com.macarambon.pocketledger.data.local.entity.WalletEntity

interface DashboardContract {
    interface View {
        fun showToast(message: String)
        fun showUserHeader(firstName: String, email: String, initials: String)
        fun showBalanceSummary(summary: BalanceSummary)
        fun showWalletFilterOptions(wallets: List<WalletEntity>, selectedIndex: Int)
        fun showTransactionFormOptions(wallets: List<WalletEntity>, categories: List<CategoryEntity>)
        fun showLedgerPreview(entries: List<LedgerEntry>)
        fun setAppliedInterestVisible(visible: Boolean)
        fun clearTransactionForm()
        fun clearCategoryNameField()
        fun navigateToLogin()
        fun navigateToWalletManagement()
        fun navigateToTransactionHistory()
        fun navigateToProfile()
    }

    interface Presenter {
        fun loadDashboard()
        fun onWalletFilterSelected(index: Int)
        fun onTransactionTypeSelected(typeIndex: Int)
        fun onAddTransactionClicked(
            walletIndex: Int,
            categoryIndex: Int,
            amount: String,
            date: String,
            typeIndex: Int,
            appliedInterestRate: String,
        )
        fun onAddCategoryClicked(name: String)
        fun onViewAllHistoryClicked()
        fun onRemoveTransactionClicked(transactionId: Long)
        fun onLogoutClicked()
    }
}
