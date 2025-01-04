package com.example.arbitrade

import android.content.Context
import android.content.SharedPreferences
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class TradeViewModel : ViewModel() {

    private val _tradeList = MutableLiveData<MutableList<TradeData>>()
    val tradeList: LiveData<MutableList<TradeData>> = _tradeList

    private lateinit var sharedPreferences: SharedPreferences

    // Initialize SharedPreferences
    fun initSharedPreferences(context: Context) {
        sharedPreferences = context.getSharedPreferences("TradeDataPrefs", Context.MODE_PRIVATE)
        loadTradeList() // Load the saved trade list when the ViewModel is initialized
    }

    // Load trade list from SharedPreferences
    private fun loadTradeList() {
        val gson = Gson()
        val json = sharedPreferences.getString("tradeList", "[]")
        val type = object : TypeToken<MutableList<TradeData>>() {}.type
        val loadedList: MutableList<TradeData> = gson.fromJson(json, type)
        _tradeList.value = loadedList
    }

    // Save the trade list to SharedPreferences
    private fun saveTradeList() {
        val gson = Gson()
        val json = gson.toJson(_tradeList.value)
        sharedPreferences.edit().putString("tradeList", json).apply()
    }

    // Add a new trade to the list
    fun addTrade(trade: TradeData) {
        val currentList = _tradeList.value ?: mutableListOf() // Ensure list is never null
        currentList.add(trade)
        _tradeList.value = currentList
        saveTradeList()
    }

    // Delete a trade from the list immediately
    fun deleteTrade(trade: TradeData) {
        val currentList = _tradeList.value ?: mutableListOf() // Ensure list is never null
        currentList.remove(trade)
        _tradeList.value = currentList // Update LiveData to notify observers
        saveTradeList() // Immediately save the updated list
    }

    // Update an existing trade
    fun updateTrade(position: Int, updatedTrade: TradeData) {
        val currentList = _tradeList.value ?: mutableListOf() // Ensure list is never null
        currentList[position] = updatedTrade
        _tradeList.value = currentList
        saveTradeList()
    }
}

