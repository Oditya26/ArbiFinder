package com.example.arbitrade.indodax

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path

interface IndodaxApi {
    @GET("{symbol}")
    fun getTickers(@Path("symbol") symbol: String): Call<TickerResponseIndodax>
}