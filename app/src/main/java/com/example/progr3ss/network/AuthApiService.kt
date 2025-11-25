package com.example.progr3ss.network

import com.example.progr3ss.model.AuthRequest
import com.example.progr3ss.model.AuthResponse
import com.example.progr3ss.model.MessageResponse
import com.example.progr3ss.model.RefreshTokenResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.*

interface AuthApiService {
    @POST("auth/local/signin")
    suspend fun login(@Body request: AuthRequest): Response<AuthResponse>

    @POST("auth/local/refresh")
    suspend fun refresh(@Header("Authorization") refreshToken: String): Response<RefreshTokenResponse>

    @Multipart
    @POST("auth/local/signup")
    suspend fun register(
        @Part("username") username: RequestBody,
        @Part("email") email: RequestBody,
        @Part("password") password: RequestBody,
        @Part profileImage: MultipartBody.Part? = null
    ): Response<AuthResponse>

    @POST("auth/google")
    suspend fun googleAuth(@Body body: Map<String, String>): Response<AuthResponse>

    @POST("auth/reset-password-via-email")
    suspend fun resetPassword(@Body body: Map<String, String>): Response<MessageResponse>
}