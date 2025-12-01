package com.example.progr3ss.ui.home

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.progr3ss.model.ScheduleResponseDto
import com.example.progr3ss.repository.ScheduleRepository
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class HomeViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = ScheduleRepository(application)

    private val _schedules = MutableLiveData<List<ScheduleResponseDto>>()
    val schedules: LiveData<List<ScheduleResponseDto>> = _schedules

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> = _errorMessage

    init {
        loadTodaySchedules()
    }

    fun loadTodaySchedules() {
        loadSchedules(null)
    }

    fun loadSchedulesForDate(date: String) {
        android.util.Log.d("HomeViewModel", "Loading schedules for specific date: $date")
        loadSchedules(date)
    }

    fun loadSchedules(date: String? = null) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val currentDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
                android.util.Log.d("HomeViewModel", "===========================================")
                android.util.Log.d("HomeViewModel", "LOADING SCHEDULES")
                android.util.Log.d("HomeViewModel", "Current date: $currentDate")
                android.util.Log.d("HomeViewModel", "Requested date: ${date ?: "null (defaults to today)"}")
                android.util.Log.d("HomeViewModel", "Full URL will be: http://10.0.2.2:8080/schedule/day${if (date != null) "?date=$date" else ""}")

                val response = repository.getSchedulesByDay(date)

                android.util.Log.d("HomeViewModel", "-------------------------------------------")
                android.util.Log.d("HomeViewModel", "RESPONSE RECEIVED")
                android.util.Log.d("HomeViewModel", "Response code: ${response.code()}")
                android.util.Log.d("HomeViewModel", "Is successful: ${response.isSuccessful}")
                android.util.Log.d("HomeViewModel", "Response message: ${response.message()}")

                if (response.isSuccessful && response.body() != null) {
                    val schedules = response.body()!!
                    android.util.Log.d("HomeViewModel", "-------------------------------------------")
                    android.util.Log.d("HomeViewModel", "SCHEDULES RECEIVED: ${schedules.size}")

                    if (schedules.isEmpty()) {
                        android.util.Log.w("HomeViewModel", "⚠️ EMPTY LIST - No schedules returned from API")
                        android.util.Log.w("HomeViewModel", "This could mean:")
                        android.util.Log.w("HomeViewModel", "1. No schedules exist for date: ${date ?: currentDate}")
                        android.util.Log.w("HomeViewModel", "2. Check if schedules exist in backend for this date")
                    } else {
                        schedules.forEachIndexed { index, schedule ->
                            android.util.Log.d("HomeViewModel", "-------------------------------------------")
                            android.util.Log.d("HomeViewModel", "Schedule #${index + 1}:")
                            android.util.Log.d("HomeViewModel", "  ID: ${schedule.id}")
                            android.util.Log.d("HomeViewModel", "  Habit Name: ${schedule.title}")
                            android.util.Log.d("HomeViewModel", "  Description: ${schedule.description ?: "none"}")
                            android.util.Log.d("HomeViewModel", "  Date: ${schedule.date}")
                            android.util.Log.d("HomeViewModel", "  Start Time: ${schedule.startTime}")
                            android.util.Log.d("HomeViewModel", "  End Time: ${schedule.endTime ?: "none"}")
                            android.util.Log.d("HomeViewModel", "  Status: ${schedule.status} -> ${schedule.scheduleStatus}")
                            android.util.Log.d("HomeViewModel", "  Duration: ${schedule.durationMinutes ?: "none"} minutes")
                            android.util.Log.d("HomeViewModel", "  Type: ${schedule.type ?: "none"}")
                        }
                    }

                    val sortedSchedules = schedules.sortedBy { it.startTime }
                    _schedules.value = sortedSchedules
                    android.util.Log.d("HomeViewModel", "-------------------------------------------")
                    android.util.Log.d("HomeViewModel", "✅ Schedules sorted and posted to LiveData")
                    android.util.Log.d("HomeViewModel", "===========================================")
                } else {
                    val errorBody = response.errorBody()?.string()
                    android.util.Log.e("HomeViewModel", "-------------------------------------------")
                    android.util.Log.e("HomeViewModel", "❌ FAILED TO LOAD SCHEDULES")
                    android.util.Log.e("HomeViewModel", "Response code: ${response.code()}")
                    android.util.Log.e("HomeViewModel", "Response message: ${response.message()}")
                    android.util.Log.e("HomeViewModel", "Error body: $errorBody")
                    android.util.Log.e("HomeViewModel", "===========================================")
                    _errorMessage.value = "Failed to load schedules: ${response.code()}"
                    _schedules.value = emptyList()
                }
            } catch (e: Exception) {
                android.util.Log.e("HomeViewModel", "-------------------------------------------")
                android.util.Log.e("HomeViewModel", "❌ EXCEPTION OCCURRED")
                android.util.Log.e("HomeViewModel", "Exception type: ${e.javaClass.simpleName}")
                android.util.Log.e("HomeViewModel", "Exception message: ${e.message}")
                android.util.Log.e("HomeViewModel", "Stack trace:", e)
                android.util.Log.e("HomeViewModel", "===========================================")
                _errorMessage.value = "Error: ${e.message}"
                _schedules.value = emptyList()
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun getCurrentDateFormatted(): String {
        val sdf = SimpleDateFormat("EEEE, MMMM d, yyyy", Locale.getDefault())
        return sdf.format(Date())
    }
}

