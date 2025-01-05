package com.example.arbitrade

import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.Button
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.arbitrade.databinding.ItemRowDataTradesBinding

// TradeAdapter.kt
class TradeAdapter(
    var tradeList: MutableList<TradeData>,
    private val onDelete: (TradeData) -> Unit,
    private val onEdit: (TradeData) -> Unit
) : RecyclerView.Adapter<TradeAdapter.TradeViewHolder>() {

    private val expandedPositions = mutableSetOf<Int>() // Track expanded items

    inner class TradeViewHolder(private val binding: ItemRowDataTradesBinding) : RecyclerView.ViewHolder(binding.root) {

        @SuppressLint("SetTextI18n")
        fun bind(trade: TradeData, position: Int) {
            binding.tvDate.text = trade.date
            binding.tvSymbol.text = trade.symbol
            binding.tvBuyPrice.text = formatPrice(trade.buyPrice)
            binding.tvSellPrice.text = formatPrice(trade.sellPrice)
            binding.tvAmount.text = trade.amount.toString()

            val pnl = trade.pnl
            if (pnl != null) {
                if (pnl < 0) {
                    binding.tvPnl.text = String.format("%.2f", pnl)
                    binding.tvPnl.setTextColor(ContextCompat.getColor(binding.root.context, R.color.red))
                    binding.tvPnl.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.percentage_red, 0)
                } else {
                    binding.tvPnl.text = "+${String.format("%.2f", pnl)}"
                    binding.tvPnl.setTextColor(ContextCompat.getColor(binding.root.context, R.color.green))
                    binding.tvPnl.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.percentage_green, 0)
                }
            }

            binding.tvCommentContent.text = "\"${trade.comment}\""

            // Handle Delete and Edit button click
            binding.btnDelete.setOnClickListener {
                onDelete(trade)
            }

            binding.btnEdit.setOnClickListener {
                onEdit(trade)
            }

            // Toggle visibility of detail section
            binding.btnDetail.setOnClickListener {
                if (expandedPositions.contains(position)) {
                    expandedPositions.remove(position)
                    collapseDetail()
                } else {
                    expandedPositions.add(position)
                    expandDetail()
                }
            }

            // Update visibility based on state
            if (expandedPositions.contains(position)) {
                expandDetail()
            } else {
                collapseDetail()
            }
        }

        private fun expandDetail() {
            binding.llDetail.visibility = View.VISIBLE

            // Apply fade-in and slide from top animations
            val fadeIn = AnimationUtils.loadAnimation(binding.root.context, R.anim.fade_in)
            val slideIn = AnimationUtils.loadAnimation(binding.root.context, R.anim.slide_in_from_top)

            binding.tvCommentContent.startAnimation(fadeIn)
            binding.tvCommentContent.startAnimation(slideIn)

            binding.btnEdit.startAnimation(fadeIn)
            binding.btnEdit.startAnimation(slideIn)

            binding.btnDelete.startAnimation(fadeIn)
            binding.btnDelete.startAnimation(slideIn)

            // Apply expand animation for the llDetail container with center-top pivot
            val expandAnimation = AnimationUtils.loadAnimation(binding.root.context, R.anim.expand_animation)
            binding.llDetail.startAnimation(expandAnimation)

            morphButton(binding.btnDetail, true)
        }

        private fun collapseDetail() {
            // Apply fade-out and slide to bottom animations for comment and buttons
            val fadeOut = AnimationUtils.loadAnimation(binding.root.context, R.anim.fade_out)
            val slideOut = AnimationUtils.loadAnimation(binding.root.context, R.anim.slide_out_to_bottom)

            binding.tvCommentContent.startAnimation(fadeOut)
            binding.tvCommentContent.startAnimation(slideOut)

            binding.btnEdit.startAnimation(fadeOut)
            binding.btnEdit.startAnimation(slideOut)

            binding.btnDelete.startAnimation(fadeOut)
            binding.btnDelete.startAnimation(slideOut)

            // Apply collapse animation for the llDetail container with center-top pivot
            val collapseAnimation = AnimationUtils.loadAnimation(binding.root.context, R.anim.collapse_animation)
            binding.llDetail.startAnimation(collapseAnimation)
            binding.llDetail.visibility = View.GONE

            morphButton(binding.btnDetail, false)
        }

        @SuppressLint("ObjectAnimatorBinding")
        private fun morphButton(button: Button, expanded: Boolean) {
            val startDrawable = if (expanded) R.drawable.baseline_keyboard_arrow_up_17 else R.drawable.baseline_keyboard_arrow_down_17

            // Animate background change (fade transition between backgrounds)
            val animator = ObjectAnimator.ofObject(
                button,
                "background",
                android.animation.ArgbEvaluator(),
                ContextCompat.getColor(button.context, R.color.transparent), // Start color
                ContextCompat.getColor(button.context, R.color.transparent)  // End color
            ).apply {
                duration = 300
            }

            animator.start()

            // Set new background drawable after animation
            button.setBackgroundResource(startDrawable)
        }
    }



    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TradeViewHolder {
        val binding = ItemRowDataTradesBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return TradeViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TradeViewHolder, position: Int) {
        val trade = tradeList[position]
        holder.bind(trade, position)

        // Apply slide-in-left animation only if the item is being displayed for the first time
        if (holder.itemView.animation == null) {
            val animation = AnimationUtils.loadAnimation(holder.itemView.context, R.anim.slide_in_right)
            holder.itemView.startAnimation(animation)
        }
    }

    override fun getItemCount(): Int = tradeList.size

    @SuppressLint("NotifyDataSetChanged")
    fun updateData(newData: List<TradeData>) {
        this.tradeList.clear()
        this.tradeList.addAll(newData)
        notifyDataSetChanged()
    }

    private fun formatPrice(price: Float?): String {
        return if (price != null) {
            val formattedPrice = String.format("%.4f", price)
            if (formattedPrice.endsWith(",0000")) {
                formattedPrice.substringBefore(",")
            } else {
                formattedPrice.trimEnd('0').trimEnd(',')
            }
        } else {
            "N/A"
        }
    }
}
