package com.macarambon.pocketledger.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import com.macarambon.pocketledger.data.local.dao.CategoryDao
import com.macarambon.pocketledger.data.local.dao.TransactionDao
import com.macarambon.pocketledger.data.local.dao.UserDao
import com.macarambon.pocketledger.data.local.dao.WalletDao
import com.macarambon.pocketledger.data.local.entity.CategoryEntity
import com.macarambon.pocketledger.data.local.entity.InterestFrequency
import com.macarambon.pocketledger.data.local.entity.TransactionEntity
import com.macarambon.pocketledger.data.local.entity.TransactionType
import com.macarambon.pocketledger.data.local.entity.UserEntity
import com.macarambon.pocketledger.data.local.entity.WalletEntity
import com.macarambon.pocketledger.data.local.entity.WalletType

class EnumConverters {
    @TypeConverter
    fun fromWalletType(value: WalletType): String = value.name

    @TypeConverter
    fun toWalletType(value: String): WalletType = WalletType.valueOf(value)

    @TypeConverter
    fun fromInterestFrequency(value: InterestFrequency?): String? = value?.name

    @TypeConverter
    fun toInterestFrequency(value: String?): InterestFrequency? =
        value?.let { InterestFrequency.valueOf(it) }

    @TypeConverter
    fun fromTransactionType(value: TransactionType): String = value.name

    @TypeConverter
    fun toTransactionType(value: String): TransactionType = TransactionType.valueOf(value)
}

@Database(
    entities = [
        UserEntity::class,
        WalletEntity::class,
        CategoryEntity::class,
        TransactionEntity::class,
    ],
    version = 2,
    exportSchema = false,
)
@TypeConverters(EnumConverters::class)
abstract class PocketLedgerDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun walletDao(): WalletDao
    abstract fun categoryDao(): CategoryDao
    abstract fun transactionDao(): TransactionDao

    companion object {
        @Volatile
        private var instance: PocketLedgerDatabase? = null

        fun getInstance(context: Context): PocketLedgerDatabase {
            return instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    PocketLedgerDatabase::class.java,
                    "pocket_ledger.db",
                )
                    .fallbackToDestructiveMigration()
                    .build()
                    .also { instance = it }
            }
        }
    }
}
