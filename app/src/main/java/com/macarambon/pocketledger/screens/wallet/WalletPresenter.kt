package com.macarambon.pocketledger.screens.wallet

import android.content.Context
import com.macarambon.pocketledger.R
import com.macarambon.pocketledger.data.local.entity.InterestFrequency
import com.macarambon.pocketledger.data.local.entity.WalletType
import com.macarambon.pocketledger.data.notifyErrorIfNotOk
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class WalletPresenter(
    private val view: WalletContract.View,
    private val model: WalletModel,
    private val scope: CoroutineScope,
    private val context: Context,
) : WalletContract.Presenter {

    private var userId: Long? = null

    override fun loadWallets() {
        scope.launch {
            val id = withContext(Dispatchers.IO) { model.getCurrentUserId(context) }
            if (id == null) {
                view.showToast(context.getString(R.string.error_session_expired))
                return@launch
            }
            userId = id
            val wallets = withContext(Dispatchers.IO) { model.getWallets(id) }
            view.showWallets(wallets)
        }
    }

    override fun onWalletTypeSelected(typeIndex: Int) {
        view.setInterestFieldsVisible(typeIndex == 1)
    }

    override fun onCreateWalletClicked(
        name: String,
        startingAmount: String,
        typeIndex: Int,
        interestRate: String,
        frequencyIndex: Int,
    ) {
        if (name.isBlank()) {
            view.showToast(context.getString(R.string.error_field_required))
            return
        }
        val amount = startingAmount.toDoubleOrNull()
        if (amount == null || amount < 0) {
            view.showToast(context.getString(R.string.error_invalid_amount))
            return
        }
        val type = walletTypes.getOrNull(typeIndex) ?: WalletType.CHECKING
        val rate = if (type == WalletType.SAVINGS) interestRate.toDoubleOrNull() else null
        val frequency = if (type == WalletType.SAVINGS) {
            frequencies.getOrNull(frequencyIndex)
        } else {
            null
        }

        view.setCreateInProgress(true)
        scope.launch {
            val userId = withContext(Dispatchers.IO) { model.getCurrentUserId(context) }
            if (userId == null) {
                withContext(Dispatchers.Main) {
                    view.setCreateInProgress(false)
                    view.showToast(context.getString(R.string.error_session_expired))
                }
                return@launch
            }
            val result = withContext(Dispatchers.IO) {
                model.createWallet(context, userId, name, amount, type, rate, frequency)
            }
            withContext(Dispatchers.Main) {
                view.setCreateInProgress(false)
                when (result) {
                    is com.macarambon.pocketledger.data.PocketLedgerResult.Ok -> {
                        view.showToast(context.getString(R.string.success_wallet_created))
                        view.clearCreateForm()
                        loadWallets()
                    }
                    else -> result.notifyErrorIfNotOk { view.showToast(it) }
                }
            }
        }
    }

    override fun onRemoveWalletClicked(walletId: Long) {
        val id = userId ?: return
        scope.launch {
            val result = withContext(Dispatchers.IO) {
                model.removeWallet(context, id, walletId)
            }
            withContext(Dispatchers.Main) {
                when (result) {
                    is com.macarambon.pocketledger.data.PocketLedgerResult.Ok -> {
                        view.showToast(context.getString(R.string.success_wallet_removed))
                        loadWallets()
                    }
                    else -> result.notifyErrorIfNotOk { view.showToast(it) }
                }
            }
        }
    }

    companion object {
        val walletTypes = listOf(WalletType.CHECKING, WalletType.SAVINGS, WalletType.CASH)
        val frequencies = listOf(
            InterestFrequency.DAILY,
            InterestFrequency.MONTHLY,
            InterestFrequency.YEARLY,
        )
    }
}
