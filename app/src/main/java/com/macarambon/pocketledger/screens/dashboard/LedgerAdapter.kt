package com.macarambon.pocketledger.screens.dashboard

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.macarambon.pocketledger.R
import com.macarambon.pocketledger.data.local.LedgerEntry
import com.macarambon.pocketledger.data.local.entity.TransactionType
import com.macarambon.pocketledger.data.rules.BusinessRules

class LedgerAdapter(
    private var showWalletName: Boolean = true,
    private val onRemove: ((LedgerEntry) -> Unit)? = null,
) : RecyclerView.Adapter<LedgerAdapter.ViewHolder>() {

    private val items = mutableListOf<LedgerEntry>()

    fun setShowWalletName(show: Boolean) {
        showWalletName = show
        notifyDataSetChanged()
    }

    fun submitList(entries: List<LedgerEntry>) {
        items.clear()
        items.addAll(entries)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_ledger_entry, parent, false)
        return ViewHolder(view as ViewGroup, onRemove)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position], showWalletName)
    }

    override fun getItemCount(): Int = items.size

    class ViewHolder(
        private val root: ViewGroup,
        private val onRemove: ((LedgerEntry) -> Unit)?,
    ) : RecyclerView.ViewHolder(root) {
        private val textCategory: TextView = root.findViewById(R.id.textviewCategoryName)
        private val textDate: TextView = root.findViewById(R.id.textviewDate)
        private val textAmount: TextView = root.findViewById(R.id.textviewAmount)
        private val textType: TextView = root.findViewById(R.id.textviewType)
        private val textWalletBadge: TextView = root.findViewById(R.id.textviewWalletBadge)
        private val buttonRemove: MaterialButton = root.findViewById(R.id.buttonRemoveTransaction)

        fun bind(entry: LedgerEntry, showWallet: Boolean) {
            textCategory.text = entry.categoryName
            textDate.text = BusinessRules.formatTransactionDateTime(entry.date)
            textType.text = entry.type.name
            val isExpense = entry.type == TransactionType.EXPENSE
            val sign = if (isExpense) "-" else "+"
            val color = if (isExpense) R.color.negative else R.color.positive
            textAmount.text = root.context.getString(
                R.string.currency_format,
                "$sign${String.format("%.2f", entry.amount)}",
            )
            textAmount.setTextColor(ContextCompat.getColor(root.context, color))
            textWalletBadge.visibility = if (showWallet) View.VISIBLE else View.GONE
            textWalletBadge.text = entry.walletName
            val removeCallback = onRemove
            if (removeCallback != null) {
                buttonRemove.visibility = View.VISIBLE
                buttonRemove.setOnClickListener { removeCallback(entry) }
            } else {
                buttonRemove.visibility = View.GONE
            }
        }
    }
}
