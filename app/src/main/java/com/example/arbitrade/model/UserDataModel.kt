package com.example.arbitrade.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class UserDataModel(
    val id: Long,
    val name: String,
    val isVerified: Boolean
) : Parcelable
