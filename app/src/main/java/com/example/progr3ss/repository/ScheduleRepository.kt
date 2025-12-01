package com.example.progr3ss.repository

import android.content.Context
import com.example.progr3ss.model.*
import com.example.progr3ss.network.RetrofitClient
import com.example.progr3ss.network.ScheduleApiService

class ScheduleRepository(private val context: Context) {
    private val apiService: ScheduleApiService = RetrofitClient.getScheduleApiService(context)

    suspend fun getSchedulesByDay(date: String? = null) = apiService.getSchedulesByDay(date)

    suspend fun getHabits() = apiService.getHabits()

    suspend fun createHabit(request: CreateHabitRequest) = apiService.createHabit(request)

    suspend fun getCategories() = apiService.getCategories()

    suspend fun createCustomSchedule(request: CreateCustomScheduleRequest) =
        apiService.createCustomSchedule(request)

    suspend fun createRecurringSchedule(request: CreateRecurringScheduleRequest) =
        apiService.createRecurringSchedule(request)

    suspend fun createWeekdayRecurringSchedule(request: CreateWeekdayRecurringScheduleRequest) =
        apiService.createWeekdayRecurringSchedule(request)
}

