package com.example.progr3ss.model

data class AuthRequest(
    val email: String,
    val password: String,
    val name: String? = null // only used in signup
)
