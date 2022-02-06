package com.example.kozlovdvtest.retrofit

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path

interface RetrofitService {
// Метод вызова случайного поста
//    @GET(Constants.RANDOM_POST)
//    fun getRandomPost(): Call<JsonData>

    @GET("latest/{page}?json=true")
    fun getLatestPost(@Path("page") page: String): Call<DataFromDevLife>

    @GET("top/{page}?json=true")
    fun getBestPost(@Path("page") page: String): Call<DataFromDevLife>

    @GET("hot/{page}?json=true")
    fun getHotPost(@Path("page") page: String): Call<DataFromDevLife>

}