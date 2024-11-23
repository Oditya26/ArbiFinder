package com.example.arbitrade.kucoin

data class TickerResponseKucoin(
    val code: String,
    val data: TickerData
)

data class TickerData(
    val time: Long,
    val symbol: String,
    val buy: Float,
    val sell: Float,
    val changeRate: Float,
    val changePrice: Float,
    val high: Float,
    val low: Float,
    val vol: Float,
    val volValue: Float,
    val last: Float,
    val averagePrice: Float,
    val takerFeeRate: Float,
    val makerFeeRate: Float,
    val takerCoefficient: Float,
    val makerCoefficient: Float
)
