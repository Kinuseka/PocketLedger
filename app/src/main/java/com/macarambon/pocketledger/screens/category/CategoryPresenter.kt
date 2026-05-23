package com.macarambon.pocketledger.screens.category

import android.content.Context
import com.macarambon.pocketledger.R
import com.macarambon.pocketledger.data.notifyErrorIfNotOk
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class CategoryPresenter(
    private val view: CategoryContract.View,
    private val model: CategoryModel,
    private val scope: CoroutineScope,
    private val context: Context,
) : CategoryContract.Presenter {

    private var isAuthenticated = false
    private var lastQuery: String = ""

    override fun loadCategories() {
        scope.launch {
            val id = withContext(Dispatchers.IO) { model.getCurrentUserId(context) }
            if (id == null) {
                view.showToast(context.getString(R.string.error_session_expired))
                return@launch
            }
            isAuthenticated = true
            refreshCategories()
        }
    }

    override fun onSearchQueryChanged(query: String) {
        lastQuery = query
        refreshCategories()
    }

    override fun onAddCategoryClicked(name: String) {
        if (name.isBlank()) {
            view.showToast(context.getString(R.string.error_field_required))
            return
        }
        if (!isAuthenticated) return
        scope.launch {
            val result = withContext(Dispatchers.IO) { model.addCategory(context, name) }
            withContext(Dispatchers.Main) {
                when (result) {
                    is com.macarambon.pocketledger.data.PocketLedgerResult.Ok -> {
                        view.showToast(context.getString(R.string.success_category_added))
                        view.clearCategoryNameField()
                        refreshCategories()
                    }
                    else -> result.notifyErrorIfNotOk { view.showToast(it) }
                }
            }
        }
    }

    private fun refreshCategories() {
        if (!isAuthenticated) return
        scope.launch {
            val categories = withContext(Dispatchers.IO) {
                model.searchCategories(lastQuery)
            }
            view.showCategories(categories)
        }
    }
}
