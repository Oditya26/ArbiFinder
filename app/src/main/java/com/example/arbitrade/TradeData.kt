package com.example.arbitrade

data class TradeData(
    val date: String,
    val symbol: String,
    val buyPrice: Float?,
    val sellPrice: Float?,
    val amount: Float?,
    val pnl: Float?,
    val comment: String?
)


