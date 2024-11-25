package com.example.arbitrade.convertprice

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface CryptoApiService {
    @GET("price")
    fun getPrice(@Query("symbol") symbol: String): Call<CryptoResponse>
}