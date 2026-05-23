package com.macarambon.pocketledger.data.local.entity

enum class TransactionType {
    INCOME,
    EXPENSE,
    INTEREST,
    ;

    companion object {
        /** Order matches the dashboard transaction type spinner. */
        val formTypes = listOf(INCOME, EXPENSE, INTEREST)
    }
}
