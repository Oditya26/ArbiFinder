package com.example.arbitrade

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import androidx.recyclerview.widget.RecyclerView
import com.example.arbitrade.databinding.ItemRowDataBinding

class DataAdapter(
    private var originalDataList: List<DataModel>, // Store original data
    private var volumeValue: Float = 0f // Default volume value
) : RecyclerView.Adapter<DataAdapter.DataViewHolder>() {


    private var dataList: MutableList<DataModel> = originalDataList.toMutableList() // Current data

    inner class DataViewHolder(private val binding: ItemRowDataBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(data: DataModel) {
            with(binding) {
                // Set the text for each TextView
                tvDifference.text = data.difference.toString()
                tvDifferenceItem.text = data.differenceItem
                tvBuyFrom.text = data.buyFrom
                tvSellAt.text = data.sellAt
                tvBuyValue.text = data.buyValue.toString()
                tvSellValue.text = data.sellValue.toString()
                tvBuyVolume.text = data.buyVolume.toString()
                tvSellVolume.text = data.sellVolume.toString()

                // Update drawableStartCompat based on volumeValue
                val drawableStart = if (volumeValue > data.buyVolume || volumeValue > data.sellVolume) {
                    R.drawable.yellow_circle // Yellow circle
                } else {
                    R.drawable.green_circle // Default green circle
                }

                tvDifference.setCompoundDrawablesRelativeWithIntrinsicBounds(
                    drawableStart,
                    0,
                    R.drawable.percentage,
                    0
                )
            }
        }
    }



    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DataViewHolder {
        // Inflate the layout using View Binding
        val binding = ItemRowDataBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return DataViewHolder(binding)
    }

    override fun onBindViewHolder(holder: DataViewHolder, position: Int) {
        // Bind the data to the ViewHolder
        holder.bind(dataList[position])

        // Apply scale-in animation to each item
        val animation = AnimationUtils.loadAnimation(holder.itemView.context, R.anim.slide_in_left)
        holder.itemView.startAnimation(animation)
    }

    override fun getItemCount(): Int = dataList.size

    @SuppressLint("NotifyDataSetChanged")
    fun updateData(newData: List<DataModel>) {
        // Clear and add new data to the current list
        dataList.clear()
        dataList.addAll(newData)
        notifyDataSetChanged() // Notify adapter to refresh the view
    }

    @SuppressLint("NotifyDataSetChanged")
    fun resetData() {
        dataList.clear() // Clear current list
        dataList.addAll(originalDataList) // Restore original data
        notifyDataSetChanged() // Notify adapter to refresh the view
    }
    fun updateVolumeValue(newVolumeValue: Float) {
        volumeValue = newVolumeValue
        notifyDataSetChanged() // Refresh all items
    }
}