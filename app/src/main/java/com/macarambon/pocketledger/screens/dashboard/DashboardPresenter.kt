package com.macarambon.pocketledger.screens.dashboard

import android.content.Context
import com.macarambon.pocketledger.R
import com.macarambon.pocketledger.data.local.entity.CategoryEntity
import com.macarambon.pocketledger.data.local.entity.TransactionType
import com.macarambon.pocketledger.data.local.entity.WalletEntity
import com.macarambon.pocketledger.data.errorMessage
import com.macarambon.pocketledger.data.notifyErrorIfNotOk
import com.macarambon.pocketledger.screens.transaction.TransactionPresenter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class DashboardPresenter(
    private val view: DashboardContract.View,
    private val model: DashboardModel,
    private val scope: CoroutineScope,
    private val context: Context,
) : DashboardContract.Presenter {

    private var userId: Long? = null
    private var wallets: List<WalletEntity> = emptyList()
    private var categories: List<CategoryEntity> = emptyList()
    private var selectedWalletId: Long? = null

    override fun loadDashboard() {
        scope.launch {
            val user = withContext(Dispatchers.IO) { model.getCurrentUser(context) }
            if (user == null) {
                view.showToast(context.getString(R.string.error_session_expired))
                view.navigateToLogin()
                return@launch
            }
            userId = user.id
            wallets = withContext(Dispatchers.IO) { model.getWallets(user.id) }
            categories = withContext(Dispatchers.IO) { model.getCategories() }
            val initials = user.firstName.firstOrNull()?.uppercaseChar()?.toString().orEmpty()
            withContext(Dispatchers.Main) {
                view.showUserHeader(user.firstName, user.email, initials)
                view.showWalletFilterOptions(wallets, 0)
                view.showTransactionFormOptions(wallets, categories)
            }
            refreshDashboard()
        }
    }

    override fun onWalletFilterSelected(index: Int) {
        selectedWalletId = if (index <= 0) null else wallets.getOrNull(index - 1)?.id
        refreshDashboard()
    }

    override fun onTransactionTypeSelected(typeIndex: Int) {
        view.setAppliedInterestVisible(typeIndex == 2)
    }

    override fun onAddTransactionClicked(
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
        val type = TransactionPresenter.transactionTypes.getOrNull(typeIndex) ?: TransactionType.EXPENSE
        val rate = if (type == TransactionType.INTEREST) appliedInterestRate.toDoubleOrNull() else null

        val id = userId ?: return
        scope.launch {
            val result = withContext(Dispatchers.IO) {
                model.addTransaction(context, id, wallet.id, category.id, parsedAmount, date, type, rate)
            }
            when (result) {
                is com.macarambon.pocketledger.data.PocketLedgerResult.Ok -> {
                    wallets = withContext(Dispatchers.IO) { model.getWallets(id) }
                    view.showToast(context.getString(R.string.success_transaction_added))
                    view.clearTransactionForm()
                    view.showTransactionFormOptions(wallets, categories)
                    refreshDashboard()
                }
                else -> view.showToast(result.errorMessage().orEmpty())
            }
        }
    }

    override fun onAddCategoryClicked(name: String) {
        if (name.isBlank()) {
            view.showToast(context.getString(R.string.error_field_required))
            return
        }
        if (userId == null) return
        scope.launch {
            val result = withContext(Dispatchers.IO) { model.addCategory(context, name) }
            when (result) {
                is com.macarambon.pocketledger.data.PocketLedgerResult.Ok -> {
                    categories = withContext(Dispatchers.IO) { model.getCategories() }
                    view.showToast(context.getString(R.string.success_category_added))
                    view.clearCategoryNameField()
                    view.showTransactionFormOptions(wallets, categories)
                }
                else -> result.notifyErrorIfNotOk { view.showToast(it) }
            }
        }
    }

    override fun onViewAllHistoryClicked() = view.navigateToTransactionHistory()

    override fun onRemoveTransactionClicked(transactionId: Long) {
        val id = userId ?: return
        scope.launch {
            val result = withContext(Dispatchers.IO) {
                model.removeTransaction(context, id, transactionId)
            }
            when (result) {
                is com.macarambon.pocketledger.data.PocketLedgerResult.Ok -> {
                    wallets = withContext(Dispatchers.IO) { model.getWallets(id) }
                    view.showToast(context.getString(R.string.success_transaction_removed))
                    view.showTransactionFormOptions(wallets, categories)
                    refreshDashboard()
                }
                else -> view.showToast(result.errorMessage().orEmpty())
            }
        }
    }

    override fun onLogoutClicked() {
        scope.launch {
            withContext(Dispatchers.IO) { model.logout(context) }
            view.navigateToLogin()
        }
    }

    fun getSelectedWalletId(): Long? = selectedWalletId

    private fun refreshDashboard() {
        val id = userId ?: return
        scope.launch {
            val summary = withContext(Dispatchers.IO) { model.getBalanceSummary(id, selectedWalletId) }
            val preview = withContext(Dispatchers.IO) { model.getLedger(id, selectedWalletId, limit = 5) }
            withContext(Dispatchers.Main) {
                view.showBalanceSummary(summary)
                view.showLedgerPreview(preview)
            }
        }
    }
}
