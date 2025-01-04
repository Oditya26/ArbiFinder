package com.example.arbitrade

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import androidx.core.content.ContextCompat
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

        // Apply slide-in-left animation only if the item is being displayed for the first time
        if (holder.itemView.animation == null) {
            val animation = AnimationUtils.loadAnimation(holder.itemView.context, R.anim.slide_in_right)
            holder.itemView.startAnimation(animation)
        }
    }



    override fun getItemCount(): Int = tradeList.size

    inner class TradeViewHolder(private val binding: ItemRowDataTradesBinding) : RecyclerView.ViewHolder(binding.root) {

        @SuppressLint("SetTextI18n")
        fun bind(trade: TradeData) {
            binding.tvDate.text = trade.date
            binding.tvSymbol.text = trade.symbol
            binding.tvBuyPrice.text = formatPrice(trade.buyPrice)
            binding.tvSellPrice.text = formatPrice(trade.sellPrice)


            binding.tvAmount.text = trade.amount.toString()

            val pnl = trade.pnl
            if (pnl != null) {
                // Display PnL with '+' for positive and '-' for negative
                if (pnl < 0) {
                    // Negative PnL: Red color, '-' drawable at start, and percent at end
                    binding.tvPnl.text = String.format("%.2f", pnl) // Format pnl
                    binding.tvPnl.setTextColor(ContextCompat.getColor(binding.root.context, R.color.red))
                    binding.tvPnl.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.percentage_red, 0)
                } else {
                    // Positive PnL: Green color, '+' string at start, and percent at end
                    binding.tvPnl.text = "+${String.format("%.2f", pnl)}" // Add "+" to positive pnl
                    binding.tvPnl.setTextColor(ContextCompat.getColor(binding.root.context, R.color.green))
                    binding.tvPnl.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.percentage_green, 0)
                }
            }

            binding.tvCommentContent.text = "\"${trade.comment}\""

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

    @SuppressLint("NotifyDataSetChanged")
    fun updateData(newData: List<TradeData>) {
        this.tradeList.clear()
        this.tradeList.addAll(newData)
        notifyDataSetChanged()
    }

    // Fungsi untuk memformat harga

    // Fungsi untuk memformat harga
    private fun formatPrice(price: Float?): String {
        return if (price != null) {
            val formattedPrice = String.format("%.4f", price) // Format 4 desimal
            if (formattedPrice.endsWith(",0000")) {
                formattedPrice.substringBefore(",") // Ambil hanya bagian sebelum titik
            } else {
                formattedPrice.trimEnd('0').trimEnd(',') // Hilangkan trailing zeros
            }
        } else {
            "N/A" // Tampilkan nilai default jika null
        }
    }
}