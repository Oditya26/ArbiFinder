package com.example.arbitrade.indodax

data class TickerResponseIndodax(
    val ticker: Ticker
)

data class Ticker(
    val high: Float,
    val low: Float,
    val vol_btc: Float,
    val vol_usdt: Float,
    val last: Float,
    val buy: Float,
    val sell: Float,
    val server_time: Long
)
