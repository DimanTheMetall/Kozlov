package com.example.kozlovdvtest.retrofit

import com.example.kozlovdvtest.Constants
import retrofit2.Call
import retrofit2.http.GET

interface RetrofitService {
    @GET(Constants.RANDOM_POST)
    fun getRandomPost(): Call<JsonData>

    @GET(Constants.LATEST_POST)
    fun getLatestPost(): Call<List<JsonData>>

    @GET(Constants.BEST_POST)
    fun getBestPost(): Call<List<JsonData>>

    @GET(Constants.HOT_POST)
    fun getHotPost(): Call<List<JsonData>>

}