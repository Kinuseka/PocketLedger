package com.macarambon.pocketledger.screens.wallet

import com.macarambon.pocketledger.data.local.entity.WalletEntity

interface WalletContract {
    interface View {
        fun showToast(message: String)
        fun showWallets(wallets: List<WalletEntity>)
        fun setInterestFieldsVisible(visible: Boolean)
        fun clearCreateForm()
        fun setCreateInProgress(inProgress: Boolean)
    }

    interface Presenter {
        fun loadWallets()
        fun onWalletTypeSelected(typeIndex: Int)
        fun onCreateWalletClicked(
            name: String,
            startingAmount: String,
            typeIndex: Int,
            interestRate: String,
            frequencyIndex: Int,
        )
        fun onRemoveWalletClicked(walletId: Long)
    }
}
