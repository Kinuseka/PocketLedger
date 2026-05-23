package com.macarambon.pocketledger.screens.category

import android.content.Context
import com.macarambon.pocketledger.data.PocketLedgerResult
import com.macarambon.pocketledger.data.local.PocketLedgerDatabase
import com.macarambon.pocketledger.data.local.entity.CategoryEntity
import com.macarambon.pocketledger.data.local.helpers.CategoryHelper
import com.macarambon.pocketledger.data.repository.AuthStore

class CategoryModel(
    private val authStore: AuthStore,
    private val db: PocketLedgerDatabase,
) {
    private val categoryHelper = CategoryHelper(db)

    suspend fun getCurrentUserId(context: Context): Long? {
        val id = authStore.getCurrentUserId(context)
        return if (id > 0L) id else null
    }

    suspend fun getCategories(): List<CategoryEntity> =
        db.categoryDao().getAll()

    suspend fun searchCategories(query: String): List<CategoryEntity> =
        if (query.isBlank()) getCategories()
        else db.categoryDao().search(query)

    suspend fun addCategory(context: Context, name: String): PocketLedgerResult<Long> =
        categoryHelper.addCategory(context, name)
}
