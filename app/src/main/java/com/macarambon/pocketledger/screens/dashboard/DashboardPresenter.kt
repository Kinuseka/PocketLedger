package com.macarambon.pocketledger.screens.dashboard

import android.content.Context
import com.macarambon.pocketledger.R
import com.macarambon.pocketledger.data.local.entity.CategoryEntity
import com.macarambon.pocketledger.data.local.entity.TransactionType
import com.macarambon.pocketledger.data.local.entity.WalletEntity
import com.macarambon.pocketledger.data.notifyErrorIfNotOk
import com.macarambon.pocketledger.data.rules.BusinessRules
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
                withContext(Dispatchers.Main) {
                    view.showToast(context.getString(R.string.error_session_expired))
                    view.navigateToLogin()
                }
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
            view.showToast(context.getString(R.string.error_wallet_category_required))
            return
        }
        val parsedAmount = amount.toDoubleOrNull()
        if (parsedAmount == null || !BusinessRules.isValidTransactionAmount(parsedAmount)) {
            view.showToast(context.getString(R.string.error_invalid_amount))
            return
        }
        if (date.isBlank()) {
            view.showToast(context.getString(R.string.error_field_required))
            return
        }
        if (BusinessRules.parseTransactionDateTime(date) == null) {
            view.showToast(context.getString(R.string.error_invalid_datetime))
            return
        }
        val wallet = wallets.getOrNull(walletIndex)
        if (wallet == null) {
            view.showToast(context.getString(R.string.error_wallet_not_found))
            return
        }
        val category = categories.getOrNull(categoryIndex)
        if (category == null) {
            view.showToast(context.getString(R.string.error_category_not_found))
            return
        }
        val type = TransactionType.formTypes.getOrNull(typeIndex) ?: TransactionType.EXPENSE
        val rate = if (type == TransactionType.INTEREST) {
            val parsedRate = appliedInterestRate.toDoubleOrNull()
            if (parsedRate == null || !BusinessRules.isValidInterestRate(parsedRate)) {
                view.showToast(context.getString(R.string.error_applied_interest_required))
                return
            }
            parsedRate
        } else {
            null
        }

        val id = userId ?: return
        scope.launch {
            val result = withContext(Dispatchers.IO) {
                model.addTransaction(context, id, wallet.id, category.id, parsedAmount, date, type, rate)
            }
            if (result is com.macarambon.pocketledger.data.PocketLedgerResult.Ok) {
                wallets = withContext(Dispatchers.IO) { model.getWallets(id) }
            }
            withContext(Dispatchers.Main) {
                when (result) {
                    is com.macarambon.pocketledger.data.PocketLedgerResult.Ok -> {
                        view.showToast(context.getString(R.string.success_transaction_added))
                        view.clearTransactionForm()
                        view.showTransactionFormOptions(wallets, categories)
                    }
                    else -> result.notifyErrorIfNotOk { view.showToast(it) }
                }
            }
            if (result is com.macarambon.pocketledger.data.PocketLedgerResult.Ok) {
                refreshDashboard()
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
            if (result is com.macarambon.pocketledger.data.PocketLedgerResult.Ok) {
                categories = withContext(Dispatchers.IO) { model.getCategories() }
            }
            withContext(Dispatchers.Main) {
                when (result) {
                    is com.macarambon.pocketledger.data.PocketLedgerResult.Ok -> {
                        view.showToast(context.getString(R.string.success_category_added))
                        view.clearCategoryNameField()
                        view.showTransactionFormOptions(wallets, categories)
                    }
                    else -> result.notifyErrorIfNotOk { view.showToast(it) }
                }
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
            if (result is com.macarambon.pocketledger.data.PocketLedgerResult.Ok) {
                wallets = withContext(Dispatchers.IO) { model.getWallets(id) }
            }
            withContext(Dispatchers.Main) {
                when (result) {
                    is com.macarambon.pocketledger.data.PocketLedgerResult.Ok -> {
                        view.showToast(context.getString(R.string.success_transaction_removed))
                        view.showTransactionFormOptions(wallets, categories)
                    }
                    else -> result.notifyErrorIfNotOk { view.showToast(it) }
                }
            }
            if (result is com.macarambon.pocketledger.data.PocketLedgerResult.Ok) {
                refreshDashboard()
            }
        }
    }

    override fun onLogoutClicked() {
        scope.launch {
            withContext(Dispatchers.IO) { model.logout(context) }
            withContext(Dispatchers.Main) { view.navigateToLogin() }
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
