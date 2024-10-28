package com.example.arbitrade.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class MarketDataModel(
    val id: Long,
    val name: String // Nama sekuritas kyk binance, coinbase, indodax, etc
) : Parcelable
