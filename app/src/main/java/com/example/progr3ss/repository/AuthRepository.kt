package com.example.progr3ss.repository

import android.content.Context
import model.AuthRequest
import network.RetrofitClient

class AuthRepository(context: Context) {
    private val api = RetrofitClient.getInstance(context)
    suspend fun login(email: String, password: String) =
        api.login(AuthRequest(email = email, password = password))
}

