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
        loadSchedules(date)
    }

    fun loadSchedules(date: String? = null) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val currentDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
                val queryDate = date ?: currentDate

                val response = repository.getSchedulesByDay(queryDate)

                if (response.isSuccessful && response.body() != null) {
                    val allSchedules = response.body()!!

                    val calendar = Calendar.getInstance()
                    val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                    calendar.time = dateFormat.parse(queryDate) ?: Date()
                    val dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)
                    val isWeekend = dayOfWeek == Calendar.SATURDAY || dayOfWeek == Calendar.SUNDAY

                    val filteredSchedules = allSchedules.filter { schedule ->
                        val notes = schedule.notes ?: ""
                        when {
                            notes.startsWith("repeat:weekdays") -> !isWeekend
                            notes.startsWith("repeat:weekends") -> isWeekend
                            else -> true
                        }
                    }

                    val sortedSchedules = filteredSchedules.sortedBy { it.startTime }
                    _schedules.value = sortedSchedules
                } else {
                    _errorMessage.value = "Failed to load schedules: ${response.code()}"
                    _schedules.value = emptyList()
                }
            } catch (e: Exception) {
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

