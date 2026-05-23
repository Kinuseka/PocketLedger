package com.macarambon.pocketledger.screens.category

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.macarambon.pocketledger.R
import com.macarambon.pocketledger.data.local.entity.CategoryEntity

class CategoryAdapter : RecyclerView.Adapter<CategoryAdapter.ViewHolder>() {

    private val items = mutableListOf<CategoryEntity>()

    fun submitList(categories: List<CategoryEntity>) {
        items.clear()
        items.addAll(categories)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_category_row, parent, false)
        return ViewHolder(view as ViewGroup)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    class ViewHolder(private val root: ViewGroup) : RecyclerView.ViewHolder(root) {
        private val textName: TextView = root.findViewById(R.id.textviewCategoryName)

        fun bind(category: CategoryEntity) {
            textName.text = category.name
        }
    }
}
