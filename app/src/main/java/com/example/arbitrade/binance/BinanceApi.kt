package com.example.arbitrade.binance

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface BinanceApi {
    @GET("24hr")
    fun getTickers(@Query("symbol") symbol: String): Call<TickerResponseBinance>
}