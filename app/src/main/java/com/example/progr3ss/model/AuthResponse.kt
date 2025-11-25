package com.example.progr3ss.model

data class Profile(
    val id: Int,
    val email: String,
    val username: String,
    val description: String?,
    val profileImageUrl: String?,
    val coverImageUrl: String?,
    val fcmToken: String?,
    val preferences: Any?,
    val created_at: String,
    val updated_at: String
)

data class User(
    val id: Int,
    val email: String,
    val auth_provider: String,
    val profile: Profile
)

data class Tokens(
    val accessToken: String,
    val refreshToken: String
)

data class AuthResponse(
    val message: String,
    val user: User,
    val tokens: Tokens
)

data class RefreshTokenResponse(
    val accessToken: String,
    val refreshToken: String
)
