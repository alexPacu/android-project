package com.example.progr3ss.repository

import android.content.Context
import com.example.progr3ss.model.AuthRequest
import com.example.progr3ss.network.RetrofitClient
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody

class AuthRepository(context: Context) {
    private val api = RetrofitClient.getInstance(context)

    suspend fun login(email: String, password: String) =
        api.login(AuthRequest(email = email, password = password))

    suspend fun refreshTokens(refreshToken: String) =
        api.refresh("Bearer $refreshToken")

    suspend fun register(username: String, email: String, password: String) =
        api.register(
            username = username.toRequestBody(),
            email = email.toRequestBody(),
            password = password.toRequestBody(),
            profileImage = null
        )

    suspend fun googleAuth(idToken: String) =
        api.googleAuth(mapOf("idToken" to idToken))

    suspend fun googleLogin(idToken: String) = api.googleAuth(mapOf("idToken" to idToken))
    suspend fun resetPassword(email: String) = api.resetPassword(mapOf("email" to email))

    private fun String.toRequestBody(): RequestBody =
        this.toRequestBody("text/plain".toMediaType())
}
