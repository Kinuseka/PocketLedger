package com.macarambon.pocketledger.data.local

import com.macarambon.pocketledger.data.local.entity.TransactionType

data class LedgerEntry(
    val transactionId: Long,
    val walletName: String,
    val categoryName: String,
    val amount: Double,
    val date: String,
    val type: TransactionType,
)
