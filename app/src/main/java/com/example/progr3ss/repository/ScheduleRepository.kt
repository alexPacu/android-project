package com.example.progr3ss.repository

import android.content.Context
import com.example.progr3ss.model.*
import com.example.progr3ss.network.RetrofitClient
import com.example.progr3ss.network.ScheduleApiService

class ScheduleRepository(private val context: Context) {
    private val apiService: ScheduleApiService = RetrofitClient.getScheduleApiService(context)

    suspend fun getSchedulesByDay(date: String? = null) = apiService.getSchedulesByDay(date)

    suspend fun getScheduleById(id: Int) = apiService.getScheduleById(id)

    suspend fun updateSchedule(id: Int, body: UpdateScheduleRequest) = apiService.updateSchedule(id, body)

    suspend fun getHabits() = apiService.getHabits()

    suspend fun getHabitsByUser(userId: Int) = apiService.getHabitsByUser(userId)

    suspend fun createHabit(request: CreateHabitRequest) = apiService.createHabit(request)

    suspend fun getCategories() = apiService.getCategories()

    suspend fun createCustomSchedule(request: CreateCustomScheduleRequest) =
        apiService.createCustomSchedule(request)

    suspend fun createRecurringSchedule(request: CreateRecurringScheduleRequest) =
        apiService.createRecurringSchedule(request)

    suspend fun createWeekdayRecurringSchedule(request: CreateWeekdayRecurringScheduleRequest) =
        apiService.createWeekdayRecurringSchedule(request)

    suspend fun createProgress(request: CreateProgressRequest) =
        apiService.createProgress(request)

    suspend fun deleteSchedule(id: Int) = apiService.deleteSchedule(id)
}
