package com.example.arbitrade.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class AssetDataModel(
    val id: Long,
    val symbol: String, // Simbol asset BTC ETH AAPL GOOGL, dll
    val name: String, // Nama asset Bitcoin, Ethereum, dll
    val type: String // Tipe asset Crytocurrency, Stock, dll
) : Parcelable
