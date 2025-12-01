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

    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean> = _loading

    private val _error = MutableLiveData<String>()
    val error: LiveData<String> = _error

    private val _scheduleCreated = MutableLiveData<Boolean>()
    val scheduleCreated: LiveData<Boolean> = _scheduleCreated

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
                android.util.Log.d("ScheduleViewModel", "Loading categories from habit/categories")
                val response = repository.getCategories()
                android.util.Log.d("ScheduleViewModel", "Category response: code=${response.code()}, isSuccessful=${response.isSuccessful}")
                if (response.isSuccessful) {
                    val categories = response.body() ?: emptyList()
                    android.util.Log.d("ScheduleViewModel", "Categories loaded: ${categories.size} items")
                    categories.forEach {
                        android.util.Log.d("ScheduleViewModel", "Category: id=${it.id}, name=${it.name}, iconUrl=${it.iconUrl}")
                    }
                    _categories.value = categories
                } else {
                    val errorBody = response.errorBody()?.string()
                    android.util.Log.e("ScheduleViewModel", "Failed to load categories: ${response.code()} - ${response.message()}")
                    android.util.Log.e("ScheduleViewModel", "Error body: $errorBody")
                }
            } catch (e: Exception) {
                android.util.Log.e("ScheduleViewModel", "Error loading categories: ${e.message}", e)
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

    fun createHabit(name: String, description: String? = null, categoryId: Int? = null, goal: String? = null) {
        viewModelScope.launch {
            _loading.value = true
            try {
                val request = CreateHabitRequest(name, description, categoryId, goal)
                android.util.Log.d("ScheduleViewModel", "Creating habit with name: $name, categoryId: $categoryId")
                val response = repository.createHabit(request)
                android.util.Log.d("ScheduleViewModel", "Response code: ${response.code()}")
                if (response.isSuccessful) {
                    android.util.Log.d("ScheduleViewModel", "Habit created successfully")
                    loadHabits()
                } else {
                    val errorBody = response.errorBody()?.string()
                    android.util.Log.e("ScheduleViewModel", "Failed to create habit: ${response.code()} - ${response.message()}")
                    android.util.Log.e("ScheduleViewModel", "Error body: $errorBody")
                    _error.value = "Failed to create habit: ${response.code()} - $errorBody"
                }
            } catch (e: Exception) {
                android.util.Log.e("ScheduleViewModel", "Exception creating habit", e)
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
                    _error.value = "Failed to create schedule: ${response.message()}"
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
                    _error.value = "Failed to create recurring schedule: ${response.message()}"
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
                    _error.value = "Failed to create weekday schedule: ${response.message()}"
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
}

