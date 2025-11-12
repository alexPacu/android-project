package com.example.progr3ss.network

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import android.content.Context
import okhttp3.OkHttpClient

object RetrofitClient {
    private const val BASE_URL = "http://10.0.2.2:8080"
    fun getInstance(context: Context): AuthApiService {
        val client = OkHttpClient.Builder()
            .addInterceptor(AuthInterceptor(context.applicationContext))
            .build()
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(AuthApiService::class.java)
    }
}