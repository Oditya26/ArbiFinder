package com.example.arbitrade

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.arbitrade.databinding.ItemRowDataTradesBinding

// TradeAdapter.kt
class TradeAdapter(
    var tradeList: MutableList<TradeData>, // Make this mutable to update the list
    private val onDelete: (TradeData) -> Unit,
    private val onEdit: (TradeData) -> Unit
) : RecyclerView.Adapter<TradeAdapter.TradeViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TradeViewHolder {
        val binding = ItemRowDataTradesBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return TradeViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TradeViewHolder, position: Int) {
        val trade = tradeList[position]
        holder.bind(trade)
    }

    override fun getItemCount(): Int = tradeList.size

    inner class TradeViewHolder(private val binding: ItemRowDataTradesBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(trade: TradeData) {
            binding.tvDate.text = trade.date
            binding.tvSymbol.text = trade.symbol
            binding.tvBuyPrice.text = trade.buyPrice.toString()
            binding.tvSellPrice.text = trade.sellPrice.toString()
            binding.tvAmount.text = trade.amount.toString()
            binding.tvPnl.text = trade.pnl.toString()
            binding.tvCommentContent.text = trade.comment

            // Handle Delete button click
            binding.btnDelete.setOnClickListener {
                onDelete(trade) // Delete the trade immediately
            }

            // Handle Edit button click
            binding.btnEdit.setOnClickListener {
                onEdit(trade)
            }
        }
    }
}