package com.macarambon.pocketledger.screens.category

import com.macarambon.pocketledger.data.local.entity.CategoryEntity

interface CategoryContract {
    interface View {
        fun showToast(message: String)
        fun showCategories(categories: List<CategoryEntity>)
        fun clearCategoryNameField()
    }

    interface Presenter {
        fun loadCategories()
        fun onSearchQueryChanged(query: String)
        fun onAddCategoryClicked(name: String)
    }
}
