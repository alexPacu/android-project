package com.example.progr3ss.model
data class User(
    val id: String,
    val email: String,
    val name: String
)

data class Tokens(
    val accessToken: String,
    val refreshToken: String
)

public final data class AuthResponse(
    public final val tokens: Tokens,
    public final val user: User
)
