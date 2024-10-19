package com.example.arbitrade

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.arbitrade.databinding.ItemRowDataBinding

class DataAdapter(
    private var originalDataList: List<DataModel> // Store original data
) : RecyclerView.Adapter<DataAdapter.DataViewHolder>() {

    private var dataList: MutableList<DataModel> = originalDataList.toMutableList() // Current data

    inner class DataViewHolder(private val binding: ItemRowDataBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(data: DataModel) {
            with(binding) {
                // Set the text for each TextView
                tvDifference.text = data.difference
                tvDifferenceItem.text = data.differenceItem
                tvBuyFrom.text = data.buyFrom
                tvSellAt.text = data.sellAt
                tvBuyValue.text = data.buyValue.toString()
                tvSellValue.text = data.sellValue.toString()
                tvBuyVolume.text = data.buyVolume.toString()
                tvSellVolume.text = data.sellVolume.toString()
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
    }

    override fun getItemCount(): Int = dataList.size

    fun updateData(newData: List<DataModel>) {
        // Clear and add new data to the current list
        dataList.clear()
        dataList.addAll(newData)
        notifyDataSetChanged() // Notify adapter to refresh the view
    }

    fun resetData() {
        dataList.clear() // Clear current list
        dataList.addAll(originalDataList) // Restore original data
        notifyDataSetChanged() // Notify adapter to refresh the view
    }
}
