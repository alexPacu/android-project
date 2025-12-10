package com.example.progr3ss.repository

import android.content.Context
import com.example.progr3ss.model.AuthRequest
import com.example.progr3ss.model.UpdateProfileRequest
import com.example.progr3ss.network.RetrofitClient
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File

class AuthRepository(context: Context) {
    private val api = RetrofitClient.getInstance(context)

    suspend fun login(email: String, password: String) =
        api.login(AuthRequest(email = email, password = password))

    suspend fun refreshTokens(refreshToken: String): retrofit2.Response<com.example.progr3ss.model.RefreshTokenResponse> =
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

    suspend fun getProfile() = api.getProfile()

    suspend fun updateProfile(username: String?, description: String?) =
        api.updateProfile(UpdateProfileRequest(username = username, description = description))

    suspend fun uploadProfileImage(imageFile: File, mimeType: String? = null) : retrofit2.Response<com.example.progr3ss.model.ProfileResponseDto> {
        val contentType = (mimeType?.let { it.toMediaType() }) ?: "image/*".toMediaType()
        val requestFile = imageFile.asRequestBody(contentType)
        val part = MultipartBody.Part.createFormData("profileImage", imageFile.name, requestFile)
        return api.uploadProfileImage(part)
    }

    suspend fun logout() = api.logout()

    private fun String.toRequestBody(): RequestBody =
        this.toRequestBody("text/plain".toMediaType())
}
