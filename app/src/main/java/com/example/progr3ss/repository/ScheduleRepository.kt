package com.example.progr3ss.repository

import android.content.Context
import com.example.progr3ss.model.ScheduleResponseDto
import com.example.progr3ss.network.RetrofitClient
import retrofit2.Response

class ScheduleRepository(private val context: Context) {
    private val api = RetrofitClient.getScheduleApiService(context)

    suspend fun getSchedulesByDay(date: String? = null): Response<List<ScheduleResponseDto>> {
        return api.getSchedulesByDay(date)
    }
}

