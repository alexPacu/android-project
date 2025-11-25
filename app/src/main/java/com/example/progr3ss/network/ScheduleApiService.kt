package com.example.progr3ss.network

import com.example.progr3ss.model.ScheduleResponseDto
import retrofit2.Response
import retrofit2.http.*

interface ScheduleApiService {
    @GET("schedule/day")
    suspend fun getSchedulesByDay(
        @Query("date") date: String? = null
    ): Response<List<ScheduleResponseDto>>
}

