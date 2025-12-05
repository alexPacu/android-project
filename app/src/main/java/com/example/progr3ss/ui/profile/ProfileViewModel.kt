package com.example.progr3ss.ui.profile

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.progr3ss.model.HabitResponseDto
import com.example.progr3ss.model.ProfileResponseDto
import com.example.progr3ss.repository.AuthRepository
import com.example.progr3ss.repository.ScheduleRepository
import com.example.progr3ss.utils.SessionManager
import kotlinx.coroutines.launch

class ProfileViewModel(application: Application) : AndroidViewModel(application) {
    private val authRepository = AuthRepository(application)
    private val scheduleRepository = ScheduleRepository(application)
    private val sessionManager = SessionManager(application)

    private val _profile = MutableLiveData<ProfileResponseDto?>()
    val profile: LiveData<ProfileResponseDto?> = _profile

    private val _habits = MutableLiveData<List<HabitResponseDto>>()
    val habits: LiveData<List<HabitResponseDto>> = _habits

    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean> = _loading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    private val _logoutSuccess = MutableLiveData<Boolean>()
    val logoutSuccess: LiveData<Boolean> = _logoutSuccess

    fun loadProfile() {
        viewModelScope.launch {
            _loading.value = true
            try {
                val response = authRepository.getProfile()
                if (response.isSuccessful) {
                    val body = response.body()
                    _profile.value = body
                    body?.let { loadHabitsForUser(it.id) }
                } else {
                    _error.value = "Failed to load profile: ${response.code()}"
                }
            } catch (e: Exception) {
                _error.value = "Error loading profile: ${e.message}"
            } finally {
                _loading.value = false
            }
        }
    }

    private fun loadHabitsForUser(userId: Int) {
        viewModelScope.launch {
            try {
                val response = scheduleRepository.getHabitsByUser(userId)
                if (response.isSuccessful) {
                    _habits.value = response.body() ?: emptyList()
                } else {
                    _error.value = "Failed to load habits: ${response.code()}"
                }
            } catch (e: Exception) {
                _error.value = "Error loading habits: ${e.message}"
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            _loading.value = true
            try {
                try {
                    authRepository.logout()
                } catch (_: Exception) {
                }
                sessionManager.clearAllTokens()
                _logoutSuccess.value = true
            } finally {
                _loading.value = false
            }
        }
    }
}
