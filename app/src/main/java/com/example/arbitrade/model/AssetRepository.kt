package com.example.arbitrade.model

interface AssetRepository {
    fun getAsset(assetId : Long) : AssetDataModel
    fun getAllAsset(): List<AssetDataModel>
}