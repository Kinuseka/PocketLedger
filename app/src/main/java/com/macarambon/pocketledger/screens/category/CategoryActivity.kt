package com.macarambon.pocketledger.screens.category

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.macarambon.pocketledger.R
import com.macarambon.pocketledger.data.local.entity.CategoryEntity
import com.macarambon.pocketledger.utils.authStore
import com.macarambon.pocketledger.utils.pocketLedgerDb
import com.macarambon.pocketledger.utils.setupPocketLedgerContent
import com.macarambon.pocketledger.utils.toast
import com.macarambon.pocketledger.utils.validateRequired
import com.macarambon.pocketledger.utils.valueText
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout

class CategoryActivity : AppCompatActivity(), CategoryContract.View {

    private lateinit var presenter: CategoryPresenter
    private lateinit var adapter: CategoryAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupPocketLedgerContent(R.layout.activity_category, R.id.root)

        adapter = CategoryAdapter()
        findViewById<RecyclerView>(R.id.recyclerviewCategories).apply {
            layoutManager = LinearLayoutManager(this@CategoryActivity)
            this.adapter = this@CategoryActivity.adapter
        }

        presenter = CategoryPresenter(
            this,
            CategoryModel(authStore(), pocketLedgerDb()),
            lifecycleScope,
            this,
        )

        findViewById<TextInputEditText>(R.id.edittextSearchCategory).addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                presenter.onSearchQueryChanged(s?.toString().orEmpty())
            }
            override fun afterTextChanged(s: Editable?) = Unit
        })

        findViewById<MaterialButton>(R.id.buttonAddCategory).setOnClickListener {
            val layout = findViewById<TextInputLayout>(R.id.textinputCategoryNameLayout)
            if (!layout.validateRequired(getString(R.string.error_field_required))) return@setOnClickListener
            presenter.onAddCategoryClicked(layout.valueText())
        }
        findViewById<MaterialButton>(R.id.buttonBack).setOnClickListener { finish() }

        presenter.loadCategories()
    }

    override fun showToast(message: String) = toast(message)

    override fun showCategories(categories: List<CategoryEntity>) {
        adapter.submitList(categories)
    }

    override fun clearCategoryNameField() {
        findViewById<TextInputLayout>(R.id.textinputCategoryNameLayout).editText?.text?.clear()
    }
}
