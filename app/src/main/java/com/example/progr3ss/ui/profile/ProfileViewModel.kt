package com.example.progr3ss.ui.profile

import android.app.Application
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.progr3ss.model.HabitResponseDto
import com.example.progr3ss.model.ProfileResponseDto
import com.example.progr3ss.network.RetrofitClient
import com.example.progr3ss.repository.AuthRepository
import com.example.progr3ss.repository.ScheduleRepository
import com.example.progr3ss.utils.SessionManager
import kotlinx.coroutines.launch
import java.io.File
import okhttp3.OkHttpClient
import okhttp3.Request

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

    private val _profileImageBitmap = MutableLiveData<Bitmap?>()
    val profileImageBitmap: LiveData<Bitmap?> = _profileImageBitmap

    private val _profileUpdateSuccess = MutableLiveData<Boolean?>()
    val profileUpdateSuccess: LiveData<Boolean?> = _profileUpdateSuccess

    private val _completedHabitsToday = MutableLiveData<Set<Int>>()
    val completedHabitsToday: LiveData<Set<Int>> = _completedHabitsToday

    private val _habitWeeklyProgress = MutableLiveData<Map<Int, Pair<Int, Int>>>()
    val habitWeeklyProgress: LiveData<Map<Int, Pair<Int, Int>>> = _habitWeeklyProgress

    private fun refreshProfileImageIfAvailable(profile: ProfileResponseDto?) {
        if (profile == null) {
            _profileImageBitmap.postValue(null)
            return
        }
        val base64 = profile.profileImageBase64
        if (!base64.isNullOrBlank()) {
            try {
                val bytes: ByteArray = Base64.decode(base64, Base64.DEFAULT)
                val bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                _profileImageBitmap.postValue(bmp)
                return
            } catch (_: Exception) {
            }
        }
        val urlPath = profile.profileImageUrl
        if (urlPath.isNullOrBlank()) {
            _profileImageBitmap.postValue(null)
            return
        }
        viewModelScope.launch {
            try {
                val base = java.net.URI(RetrofitClient.BASE_URL)
                val fullUrl = if (urlPath.startsWith("http")) urlPath else base.resolve(urlPath).toString()
                val token = sessionManager.fetchAuthToken()
                val client = OkHttpClient()
                val reqBuilder = Request.Builder().url(fullUrl)
                if (!token.isNullOrBlank()) reqBuilder.addHeader("Authorization", "Bearer $token")
                client.newCall(reqBuilder.build()).execute().use { resp ->
                    if (resp.isSuccessful) {
                        resp.body?.byteStream()?.use { stream ->
                            val bmp = BitmapFactory.decodeStream(stream)
                            _profileImageBitmap.postValue(bmp)
                        }
                    } else {
                        _profileImageBitmap.postValue(null)
                    }
                }
            } catch (_: Exception) {
                _profileImageBitmap.postValue(null)
            }
        }
    }

    fun loadProfile() {
        viewModelScope.launch {
            _loading.value = true
            try {
                val response = authRepository.getProfile()
                if (response.isSuccessful) {
                    val body = response.body()
                    _profile.value = body
                    refreshProfileImageIfAvailable(body)
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
                    val habits = response.body() ?: emptyList()
                    _habits.value = habits

                    loadCompletedHabitsForToday(habits)
                } else {
                    _error.value = "Failed to load habits: ${response.code()}"
                }
            } catch (e: Exception) {
                _error.value = "Error loading habits: ${e.message}"
            }
        }
    }

    private fun loadCompletedHabitsForToday(habits: List<HabitResponseDto>) {
        viewModelScope.launch {
            try {
                val calendar = java.util.Calendar.getInstance()
                val today = calendar.clone() as java.util.Calendar
                calendar.firstDayOfWeek = java.util.Calendar.MONDAY
                calendar.set(java.util.Calendar.DAY_OF_WEEK, java.util.Calendar.MONDAY)

                val weekSchedules = mutableListOf<com.example.progr3ss.model.ScheduleResponseDto>()
                val dateFormat = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())

                while (calendar.before(today) || calendar.get(java.util.Calendar.DAY_OF_YEAR) == today.get(java.util.Calendar.DAY_OF_YEAR)) {
                    val dayDate = dateFormat.format(calendar.time)
                    val schedulesRes = scheduleRepository.getSchedulesByDay(dayDate)
                    if (schedulesRes.isSuccessful) {
                        weekSchedules.addAll(schedulesRes.body() ?: emptyList())
                    }
                    calendar.add(java.util.Calendar.DAY_OF_WEEK, 1)

                    if (calendar.get(java.util.Calendar.DAY_OF_WEEK) == java.util.Calendar.MONDAY) {
                        break
                    }
                }

                val habitCompletionMap = mutableMapOf<Int, Pair<Int, Int>>()

                habits.forEach { habit ->
                    val habitSchedules = weekSchedules.filter { it.habit.id == habit.id }

                    if (habitSchedules.isEmpty()) {
                        return@forEach
                    }

                    val firstSchedule = habitSchedules.firstOrNull()
                    val notes = firstSchedule?.notes ?: ""

                    if (notes.startsWith("repeat:once")) {
                        val isCompleted = habitSchedules.any { it.status == "Completed" }
                        val completedCount = if (isCompleted) 1 else 0
                        habitCompletionMap[habit.id] = Pair(completedCount, 1)
                        return@forEach
                    }

                    val expectedDaysPerWeek = when {
                        notes.startsWith("repeat:everyday") -> 7
                        notes.startsWith("repeat:weekdays") -> 5
                        notes.startsWith("repeat:weekends") -> 2
                        notes.startsWith("repeat:custom") -> {
                            val parts = notes.split(":")
                            if (parts.size >= 3) {
                                parts[2].split(",").size
                            } else {
                                habitSchedules.map { it.date.substring(0, 10) }.toSet().size
                            }
                        }
                        else -> {
                            habitSchedules.map { it.date.substring(0, 10) }.toSet().size
                        }
                    }

                    val completedDaysSet = habitSchedules
                        .filter { it.status == "Completed" }
                        .map { it.date.substring(0, 10) }
                        .toSet()

                    val completedDays = completedDaysSet.size

                    if (expectedDaysPerWeek > 0) {
                        habitCompletionMap[habit.id] = Pair(completedDays, expectedDaysPerWeek)
                    }
                }

                _habitWeeklyProgress.value = habitCompletionMap

                val todayStr = dateFormat.format(java.util.Date())
                val todaySchedules = weekSchedules.filter { it.date.substring(0, 10) == todayStr }
                val completedHabitIds = todaySchedules
                    .filter { it.status == "Completed" }
                    .map { it.habit.id }
                    .toSet()
                _completedHabitsToday.value = completedHabitIds
            } catch (_: Exception) {
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

    fun updateProfile(username: String?, description: String?) {
        viewModelScope.launch {
            _loading.value = true
            try {
                val res = authRepository.updateProfile(username, description)
                if (res.isSuccessful) {
                    val updated = res.body()
                    _profile.value = updated
                    refreshProfileImageIfAvailable(updated)
                    _profileUpdateSuccess.value = true
                } else {
                    _error.value = "Failed to update profile: ${res.code()}"
                    _profileUpdateSuccess.value = false
                }
            } catch (e: Exception) {
                _error.value = "Error updating profile: ${e.message}"
                _profileUpdateSuccess.value = false
            } finally {
                _loading.value = false
            }
        }
    }

    fun uploadProfileImage(file: File) {
        viewModelScope.launch {
            _loading.value = true
            try {
                val res = authRepository.uploadProfileImage(file)
                if (res.isSuccessful) {
                    val updated = res.body()
                    _profile.value = updated
                    refreshProfileImageIfAvailable(updated)
                } else {
                    _error.value = "Failed to upload image: ${res.code()}"
                }
            } catch (e: Exception) {
                _error.value = "Error uploading image: ${e.message}"
            } finally {
                _loading.value = false
            }
        }
    }

    fun uploadProfileImageWithMime(file: File, mime: String?) {
        viewModelScope.launch {
            _loading.value = true
            try {
                val res = authRepository.uploadProfileImage(file, mime)
                if (res.isSuccessful) {
                    val updated = res.body()
                    _profile.value = updated
                    refreshProfileImageIfAvailable(updated)
                } else {
                    _error.value = "Failed to upload image: ${res.code()}"
                }
            } catch (e: Exception) {
                _error.value = "Error uploading image: ${e.message}"
            } finally {
                _loading.value = false
            }
        }
    }

    fun clearProfileUpdateFlag() {
        _profileUpdateSuccess.value = null
    }

    fun refreshCompletedHabitsToday() {
        val currentHabits = _habits.value ?: emptyList()
        if (currentHabits.isEmpty()) return
        loadCompletedHabitsForToday(currentHabits)
    }
}
