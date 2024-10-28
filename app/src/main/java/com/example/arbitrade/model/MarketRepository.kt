package com.example.arbitrade.model

interface MarketRepository {
    fun getMarket(marketId : Long) : MarketDataModel
    fun getAllMarket(): List<MarketDataModel>
}