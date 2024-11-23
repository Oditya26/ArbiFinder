package com.example.arbitrade.kucoin

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface KucoinApi {
    @GET("stats")
    fun getTickers(@Query("symbol") symbol: String): Call<TickerResponseKucoin>
}
