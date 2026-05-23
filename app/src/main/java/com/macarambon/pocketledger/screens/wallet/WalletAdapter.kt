package com.macarambon.pocketledger.screens.wallet

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.macarambon.pocketledger.R
import com.macarambon.pocketledger.data.local.entity.WalletEntity

class WalletAdapter : RecyclerView.Adapter<WalletAdapter.ViewHolder>() {

    private val items = mutableListOf<WalletEntity>()

    fun submitList(wallets: List<WalletEntity>) {
        items.clear()
        items.addAll(wallets)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_wallet_row, parent, false)
        return ViewHolder(view as ViewGroup)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    class ViewHolder(private val root: ViewGroup) : RecyclerView.ViewHolder(root) {
        private val textName: TextView = root.findViewById(R.id.textviewWalletName)
        private val textAmount: TextView = root.findViewById(R.id.textviewWalletAmount)
        private val textType: TextView = root.findViewById(R.id.textviewWalletType)

        fun bind(wallet: WalletEntity) {
            textName.text = wallet.name
            textAmount.text = root.context.getString(
                R.string.currency_format,
                String.format("%.2f", wallet.currentAmount),
            )
            textType.text = wallet.type.name
        }
    }
}
