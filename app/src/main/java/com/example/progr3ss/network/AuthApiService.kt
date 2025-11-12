package com.example.progr3ss.network

import com.example.progr3ss.model.AuthRequest
import com.example.progr3ss.model.AuthResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthApiService {
    @POST("/auth/local/signin")
    suspend fun login(@Body request: AuthRequest): Response<AuthResponse>
}