package com.example.progr3ss.network

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import android.content.Context
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor

object RetrofitClient {
    const val BASE_URL = "http://10.0.2.2:8080/"

    private fun getRetrofitInstance(context: Context): Retrofit {
        val logging = HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BODY }
        val client = OkHttpClient.Builder()
            .addInterceptor(AuthInterceptor(context.applicationContext))
            .addInterceptor(logging)
            .build()
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    fun getInstance(context: Context): AuthApiService {
        return getRetrofitInstance(context).create(AuthApiService::class.java)
    }

    fun getScheduleApiService(context: Context): ScheduleApiService {
        return getRetrofitInstance(context).create(ScheduleApiService::class.java)
    }
}