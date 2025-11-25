package com.example.progr3ss.ui.auth

import android.app.Application
import android.util.Log
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
                Log.e("AuthViewModel", "Login error", e)
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
                Log.e("AuthViewModel", "Google login error", e)
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
                    val errorBody = try {
                        response.errorBody()?.string()
                    } catch (_: Exception) {
                        null
                    }
                    val msg = buildString {
                        append("Reset failed: ")
                        append(response.code())
                        if (!errorBody.isNullOrBlank()) {
                            append(" - ")
                            append(errorBody)
                        }
                    }
                    _resetResult.postValue(Result.failure(Exception(msg)))
                }
            } catch (e: Exception) {
                Log.e("AuthViewModel", "Reset password error", e)
                _resetResult.postValue(Result.failure(e))
            }
        }
    }

    private fun handleAuthResponse(response: Response<AuthResponse>) {
        if (response.isSuccessful && response.body() != null) {
            val body = response.body()!!
            Log.d("AuthViewModel", "Saving tokens: accessToken present=${body.tokens.accessToken.isNotEmpty()}, refreshToken present=${body.tokens.refreshToken.isNotEmpty()}")
            sessionManager.saveAuthToken(body.tokens.accessToken)
            sessionManager.saveRefreshToken(body.tokens.refreshToken)
            _authResult.postValue(Result.success(body))
        } else {
            val errorBody = try {
                response.errorBody()?.string()
            } catch (_: Exception) {
                null
            }
            val msg = buildString {
                append("Auth failed: ")
                append(response.code())
                if (!errorBody.isNullOrBlank()) {
                    append(" - ")
                    append(errorBody)
                }
            }
            Log.w("AuthViewModel", msg)
            _authResult.postValue(Result.failure(Exception(msg)))
        }
    }
}
