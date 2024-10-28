package com.example.arbitrade.model

interface PriceRepository {
    fun getPrice(marketId : Long, assetId : Long) : PriceDataModel
    fun getAllPrice() : List<PriceDataModel>
}