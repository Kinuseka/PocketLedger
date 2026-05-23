package com.macarambon.pocketledger.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.macarambon.pocketledger.data.local.LedgerEntry
import com.macarambon.pocketledger.data.local.entity.TransactionEntity
import com.macarambon.pocketledger.data.local.entity.TransactionType

@Dao
interface TransactionDao {
    @Insert
    suspend fun insert(transaction: TransactionEntity): Long

    @Query("SELECT * FROM transactions WHERE id = :transactionId LIMIT 1")
    suspend fun findById(transactionId: Long): TransactionEntity?

    @Query("DELETE FROM transactions WHERE id = :transactionId")
    suspend fun deleteById(transactionId: Long)

    @Query(
        """
        SELECT
            t.id AS transactionId,
            w.name AS walletName,
            c.name AS categoryName,
            t.amount AS amount,
            t.date AS date,
            t.type AS type
        FROM transactions t
        INNER JOIN wallets w ON t.walletId = w.id
        INNER JOIN categories c ON t.categoryId = c.id
        WHERE t.userId = :userId
        ORDER BY t.date DESC, t.id DESC
        """,
    )
    suspend fun getLedgerEntries(userId: Long): List<LedgerEntryRow>

    @Query(
        """
        SELECT
            t.id AS transactionId,
            w.name AS walletName,
            c.name AS categoryName,
            t.amount AS amount,
            t.date AS date,
            t.type AS type
        FROM transactions t
        INNER JOIN wallets w ON t.walletId = w.id
        INNER JOIN categories c ON t.categoryId = c.id
        WHERE t.userId = :userId AND t.walletId = :walletId
        ORDER BY t.date DESC, t.id DESC
        """,
    )
    suspend fun getLedgerEntriesByWallet(userId: Long, walletId: Long): List<LedgerEntryRow>
}

data class LedgerEntryRow(
    val transactionId: Long,
    val walletName: String,
    val categoryName: String,
    val amount: Double,
    val date: String,
    val type: TransactionType,
) {
    fun toLedgerEntry() = LedgerEntry(
        transactionId = transactionId,
        walletName = walletName,
        categoryName = categoryName,
        amount = amount,
        date = date,
        type = type,
    )
}
