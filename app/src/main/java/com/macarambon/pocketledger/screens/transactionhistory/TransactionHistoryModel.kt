package com.macarambon.pocketledger.screens.transactionhistory

import com.macarambon.pocketledger.screens.dashboard.DashboardModel

class TransactionHistoryModel(
    private val dashboardModel: DashboardModel,
) {
    suspend fun getCurrentUser(context: android.content.Context) =
        dashboardModel.getCurrentUser(context)

    suspend fun getWallets(userId: Long) = dashboardModel.getWallets(userId)

    suspend fun getLedger(userId: Long, walletId: Long?) =
        dashboardModel.getLedger(userId, walletId, limit = null)

    suspend fun removeTransaction(context: android.content.Context, userId: Long, transactionId: Long) =
        dashboardModel.removeTransaction(context, userId, transactionId)
}
