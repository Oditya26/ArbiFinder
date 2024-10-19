package com.example.arbitrade

import kotlinx.parcelize.Parcelize
import android.os.Parcelable

@Parcelize
data class DataModel(
    val difference: String,
    val differenceItem: String,
    val buyFrom: String,
    val sellAt: String,
    val buyValue: Float,
    val sellValue: Float,
    val buyVolume: Float, // Drawable resource for buy icon
    val sellVolume: Float  // Drawable resource for sell icon
) : Parcelable