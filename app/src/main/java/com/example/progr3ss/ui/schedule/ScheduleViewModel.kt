package com.example.progr3ss.ui.schedule

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.progr3ss.model.*
import com.example.progr3ss.repository.ScheduleRepository
import kotlinx.coroutines.launch

class ScheduleViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = ScheduleRepository(application)

    private val _habits = MutableLiveData<List<HabitResponseDto>>()
    val habits: LiveData<List<HabitResponseDto>> = _habits

    private val _categories = MutableLiveData<List<CategoryDto>>()
    val categories: LiveData<List<CategoryDto>> = _categories

    private val _schedules = MutableLiveData<List<ScheduleResponseDto>>()
    val schedules: LiveData<List<ScheduleResponseDto>> = _schedules

    private val _selectedSchedule = MutableLiveData<ScheduleResponseDto?>()
    val selectedSchedule: LiveData<ScheduleResponseDto?> = _selectedSchedule

    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean> = _loading

    private val _error = MutableLiveData<String>()
    val error: LiveData<String> = _error

    private val _scheduleCreated = MutableLiveData<Boolean>()
    val scheduleCreated: LiveData<Boolean> = _scheduleCreated

    private val _progressCreated = MutableLiveData<Boolean>()
    val progressCreated: LiveData<Boolean> = _progressCreated

    fun loadHabits() {
        viewModelScope.launch {
            _loading.value = true
            try {
                val response = repository.getHabits()
                if (response.isSuccessful) {
                    _habits.value = response.body() ?: emptyList()
                } else {
                    _error.value = "Failed to load habits: ${response.message()}"
                }
            } catch (e: Exception) {
                _error.value = "Error loading habits: ${e.message}"
            } finally {
                _loading.value = false
            }
        }
    }

    fun loadCategories() {
        viewModelScope.launch {
            try {
                val response = repository.getCategories()
                if (response.isSuccessful) {
                    val categories = response.body() ?: emptyList()
                    _categories.value = categories
                }
            } catch (e: Exception) {
            }
        }
    }

    fun loadSchedulesByDay(date: String? = null) {
        viewModelScope.launch {
            _loading.value = true
            try {
                val response = repository.getSchedulesByDay(date)
                if (response.isSuccessful) {
                    _schedules.value = response.body() ?: emptyList()
                } else {
                    _error.value = "Failed to load schedules: ${response.message()}"
                }
            } catch (e: Exception) {
                _error.value = "Error loading schedules: ${e.message}"
            } finally {
                _loading.value = false
            }
        }
    }

    fun loadScheduleById(id: Int) {
        viewModelScope.launch {
            _loading.value = true
            try {
                val response = repository.getScheduleById(id)
                if (response.isSuccessful) {
                    _selectedSchedule.value = response.body()
                } else {
                    _error.value = "Failed to load schedule: ${response.code()}"
                }
            } catch (e: Exception) {
                _error.value = "Error loading schedule: ${e.message}"
            } finally {
                _loading.value = false
            }
        }
    }

    fun createHabit(name: String, description: String? = null, categoryId: Int, goal: String) {
        viewModelScope.launch {
            _loading.value = true
            try {
                val request = CreateHabitRequest(name, description, categoryId, goal)
                val response = repository.createHabit(request)
                if (response.isSuccessful) {
                    loadHabits()
                } else {
                    _error.value = "Failed to create habit: ${response.code()}"
                }
            } catch (e: Exception) {
                _error.value = "Error creating habit: ${e.message}"
            } finally {
                _loading.value = false
            }
        }
    }

    fun createCustomSchedule(request: CreateCustomScheduleRequest) {
        viewModelScope.launch {
            _loading.value = true
            try {
                val response = repository.createCustomSchedule(request)
                if (response.isSuccessful) {
                    _scheduleCreated.value = true
                } else {
                    _error.value = "Failed to create schedule: ${response.code()}"
                    _scheduleCreated.value = false
                }
            } catch (e: Exception) {
                _error.value = "Error creating schedule: ${e.message}"
                _scheduleCreated.value = false
            } finally {
                _loading.value = false
            }
        }
    }

    fun createRecurringSchedule(request: CreateRecurringScheduleRequest) {
        viewModelScope.launch {
            _loading.value = true
            try {
                val response = repository.createRecurringSchedule(request)
                if (response.isSuccessful) {
                    _scheduleCreated.value = true
                } else {
                    _error.value = "Failed to create recurring schedule: ${response.code()}"
                    _scheduleCreated.value = false
                }
            } catch (e: Exception) {
                _error.value = "Error creating recurring schedule: ${e.message}"
                _scheduleCreated.value = false
            } finally {
                _loading.value = false
            }
        }
    }

    fun createWeekdayRecurringSchedule(request: CreateWeekdayRecurringScheduleRequest) {
        viewModelScope.launch {
            _loading.value = true
            try {
                val response = repository.createWeekdayRecurringSchedule(request)
                if (response.isSuccessful) {
                    _scheduleCreated.value = true
                } else {
                    _error.value = "Failed to create weekday schedule: ${response.code()}"
                    _scheduleCreated.value = false
                }
            } catch (e: Exception) {
                _error.value = "Error creating weekday schedule: ${e.message}"
                _scheduleCreated.value = false
            } finally {
                _loading.value = false
            }
        }
    }

    fun updateSchedule(
        id: Int,
        startTime: String? = null,
        endTime: String? = null,
        durationMinutes: Int? = null,
        status: String? = null,
        date: String? = null,
        isCustom: Boolean? = null,
        participantIds: List<Int>? = null,
        notes: String? = null
    ) {
        viewModelScope.launch {
            _loading.value = true
            try {
                val body = UpdateScheduleRequest(
                    startTime = startTime,
                    endTime = endTime,
                    durationMinutes = durationMinutes,
                    status = status,
                    date = date,
                    isCustom = isCustom,
                    participantIds = participantIds,
                    notes = notes
                )
                val response = repository.updateSchedule(id, body)
                if (response.isSuccessful) {
                    _selectedSchedule.value = response.body()
                } else {
                    _error.value = "Failed to update schedule: ${response.code()}"
                }
            } catch (e: Exception) {
                _error.value = "Error updating schedule: ${e.message}"
            } finally {
                _loading.value = false
            }
        }
    }

    fun createProgress(
        scheduleId: Int,
        date: String,
        loggedTime: Int? = null,
        notes: String? = null,
        isCompleted: Boolean? = null
    ) {
        viewModelScope.launch {
            _loading.value = true
            try {
                val request = CreateProgressRequest(
                    scheduleId = scheduleId,
                    date = date,
                    loggedTime = loggedTime,
                    notes = notes,
                    isCompleted = isCompleted
                )
                val response = repository.createProgress(request)
                if (response.isSuccessful) {
                    _progressCreated.value = true
                } else {
                    _error.value = "Failed to add progress: ${response.code()}"
                    _progressCreated.value = false
                }
            } catch (e: Exception) {
                _error.value = "Error adding progress: ${e.message}"
                _progressCreated.value = false
            } finally {
                _loading.value = false
            }
        }
    }
}
