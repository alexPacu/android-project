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

    fun loadSchedules(date: String? = null) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val response = repository.getSchedulesByDay(date)
                if (response.isSuccessful && response.body() != null) {
                    val sortedSchedules = response.body()!!.sortedBy { it.startTime }
                    _schedules.value = sortedSchedules
                } else {
                    _errorMessage.value = "Failed to load schedules: ${response.message()}"
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

