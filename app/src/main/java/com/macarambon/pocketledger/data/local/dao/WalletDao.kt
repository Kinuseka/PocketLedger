package com.macarambon.pocketledger.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.macarambon.pocketledger.data.local.entity.WalletEntity

@Dao
interface WalletDao {
    @Insert
    suspend fun insert(wallet: WalletEntity): Long

    @Update
    suspend fun update(wallet: WalletEntity)

    @Query("SELECT * FROM wallets WHERE userId = :userId ORDER BY name ASC")
    suspend fun getByUser(userId: Long): List<WalletEntity>

    @Query("SELECT * FROM wallets WHERE id = :walletId LIMIT 1")
    suspend fun findById(walletId: Long): WalletEntity?

    @Query("SELECT COALESCE(SUM(currentAmount), 0.0) FROM wallets WHERE userId = :userId")
    suspend fun getNetWorth(userId: Long): Double

    @Query("SELECT COALESCE(SUM(startingAmount), 0.0) FROM wallets WHERE userId = :userId")
    suspend fun getStartingTotal(userId: Long): Double
}
