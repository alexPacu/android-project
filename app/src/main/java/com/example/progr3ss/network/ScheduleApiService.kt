package com.example.progr3ss.network

import com.example.progr3ss.model.*
import retrofit2.Response
import retrofit2.http.*

interface ScheduleApiService {
    @GET("schedule/day")
    suspend fun getSchedulesByDay(
        @Query("date") date: String? = null
    ): Response<List<ScheduleResponseDto>>

    // Habit endpoints
    @GET("habit")
    suspend fun getHabits(): Response<List<HabitResponseDto>>

    @POST("habit")
    suspend fun createHabit(
        @Body request: CreateHabitRequest
    ): Response<HabitResponseDto>

    @GET("habit/categories")
    suspend fun getCategories(): Response<List<CategoryDto>>

    @POST("schedule/custom")
    suspend fun createCustomSchedule(
        @Body request: CreateCustomScheduleRequest
    ): Response<ScheduleResponseDto>

    @POST("schedule/recurring")
    suspend fun createRecurringSchedule(
        @Body request: CreateRecurringScheduleRequest
    ): Response<List<ScheduleResponseDto>>

    @POST("schedule/recurring/weekdays")
    suspend fun createWeekdayRecurringSchedule(
        @Body request: CreateWeekdayRecurringScheduleRequest
    ): Response<List<ScheduleResponseDto>>
}

