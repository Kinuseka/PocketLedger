package com.macarambon.pocketledger.data.local.helpers

import android.content.Context
import com.macarambon.pocketledger.R
import com.macarambon.pocketledger.data.PocketLedgerResult
import com.macarambon.pocketledger.data.local.PocketLedgerDatabase
import com.macarambon.pocketledger.data.local.entity.CategoryEntity
import com.macarambon.pocketledger.data.rules.BusinessRules

class CategoryHelper(private val db: PocketLedgerDatabase) {

    suspend fun addCategory(context: Context, name: String): PocketLedgerResult<Long> {
        val normalized = BusinessRules.normalizeCategoryName(name)
        if (normalized.isBlank()) {
            return PocketLedgerResult.Err.Validation(context.getString(R.string.error_field_required))
        }
        if (db.categoryDao().findByName(normalized) != null) {
            return PocketLedgerResult.Err.Validation(context.getString(R.string.error_category_duplicate))
        }
        return try {
            val id = db.categoryDao().insert(CategoryEntity(name = normalized))
            PocketLedgerResult.Ok(id)
        } catch (_: Exception) {
            PocketLedgerResult.Err.Validation(context.getString(R.string.error_category_duplicate))
        }
    }
}
