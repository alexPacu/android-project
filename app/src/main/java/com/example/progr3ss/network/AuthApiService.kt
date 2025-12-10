package com.example.progr3ss.network

import com.example.progr3ss.model.AuthRequest
import com.example.progr3ss.model.AuthResponse
import com.example.progr3ss.model.MessageResponse
import com.example.progr3ss.model.RefreshTokenResponse
import com.example.progr3ss.model.ProfileResponseDto
import com.example.progr3ss.model.UpdateProfileRequest
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

    @GET("profile")
    suspend fun getProfile(): Response<ProfileResponseDto>

    @PATCH("profile")
    suspend fun updateProfile(@Body body: UpdateProfileRequest): Response<ProfileResponseDto>

    @Multipart
    @POST("profile/upload-profile-image")
    suspend fun uploadProfileImage(@Part profileImage: MultipartBody.Part): Response<ProfileResponseDto>

    @POST("auth/local/logout")
    suspend fun logout(): Response<MessageResponse>
}