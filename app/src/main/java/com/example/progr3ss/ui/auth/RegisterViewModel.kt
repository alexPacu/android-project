package com.example.progr3ss.ui.auth

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.progr3ss.model.AuthResponse
import com.example.progr3ss.repository.AuthRepository
import com.example.progr3ss.utils.SessionManager
import kotlinx.coroutines.launch
import retrofit2.Response

class RegisterViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = AuthRepository(application)
    private val sessionManager = SessionManager(application.applicationContext)

    private val _registerResult = MutableLiveData<Result<AuthResponse>>()
    val registerResult: LiveData<Result<AuthResponse>> = _registerResult

    fun register(username: String, email: String, password: String) {
        viewModelScope.launch {
            try {
                val response = repository.register(username, email, password)
                handleResponse(response)
            } catch (e: Exception) {
                _registerResult.postValue(Result.failure(e))
            }
        }
    }

    private fun handleResponse(response: Response<AuthResponse>) {
        if (response.isSuccessful && response.body() != null) {
            val body = response.body()!!
            // Save tokens so user is logged in immediately
            sessionManager.saveAuthToken(body.tokens.accessToken)
            sessionManager.saveRefreshToken(body.tokens.refreshToken)
            _registerResult.postValue(Result.success(body))
        } else {
            _registerResult.postValue(Result.failure(Exception("Register failed: ${'$'}{response.code()}")))
        }
    }

    fun googleRegister(idToken: String) {
        viewModelScope.launch {
            try {
                val response = repository.googleAuth(idToken)
                if (response.isSuccessful && response.body() != null) {
                    val body = response.body()!!
                    sessionManager.saveAuthToken(body.tokens.accessToken)
                    sessionManager.saveRefreshToken(body.tokens.refreshToken)
                    _registerResult.postValue(Result.success(body))
                } else {
                    _registerResult.postValue(Result.failure(Exception("Google auth failed: ${response.code()}")))
                }
            } catch (e: Exception) {
                _registerResult.postValue(Result.failure(e))
            }
        }
    }
}
