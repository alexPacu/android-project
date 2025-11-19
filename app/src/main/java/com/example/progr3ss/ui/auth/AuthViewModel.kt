package com.example.progr3ss.ui.auth

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.progr3ss.model.AuthResponse
import com.example.progr3ss.model.MessageResponse
import com.example.progr3ss.repository.AuthRepository
import com.example.progr3ss.utils.SessionManager
import kotlinx.coroutines.launch
import retrofit2.Response

class AuthViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = AuthRepository(application)
    private val sessionManager = SessionManager(application.applicationContext)

    private val _authResult = MutableLiveData<Result<AuthResponse>>()
    val authResult: LiveData<Result<AuthResponse>> = _authResult

    private val _resetResult = MutableLiveData<Result<MessageResponse>>()
    val resetResult: LiveData<Result<MessageResponse>> = _resetResult

    fun login(email: String, password: String) {
        viewModelScope.launch {
            try {
                val response = repository.login(email, password)
                handleAuthResponse(response)
            } catch (e: Exception) {
                _authResult.postValue(Result.failure(e))
            }
        }
    }

    fun googleLogin(idToken: String) {
        viewModelScope.launch {
            try {
                val response = repository.googleLogin(idToken)
                handleAuthResponse(response)
            } catch (e: Exception) {
                _authResult.postValue(Result.failure(e))
            }
        }
    }

    fun resetPassword(email: String) {
        viewModelScope.launch {
            try {
                val response = repository.resetPassword(email)
                if (response.isSuccessful && response.body() != null) {
                    _resetResult.postValue(Result.success(response.body()!!))
                } else {
                    _resetResult.postValue(Result.failure(Exception("Reset failed: ${'$'}{response.code()}")))
                }
            } catch (e: Exception) {
                _resetResult.postValue(Result.failure(e))
            }
        }
    }

    private fun handleAuthResponse(response: Response<AuthResponse>) {
        if (response.isSuccessful && response.body() != null) {
            val body = response.body()!!
            sessionManager.saveAuthToken(body.tokens.accessToken)
            sessionManager.saveRefreshToken(body.tokens.refreshToken)
            _authResult.postValue(Result.success(body))
        } else {
            _authResult.postValue(Result.failure(Exception("Auth failed: ${'$'}{response.code()}")))
        }
    }
}

