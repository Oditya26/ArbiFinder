package com.example.arbitrade.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class PriceDataModel(
    val assetId: Long,
    val marketId: Long,
    val buyPrice: Double,
    val sellPrice: Double,
    val profitMargin: Double,
    val createAt: Long,
    val updateAt: Long
) : Parcelable
