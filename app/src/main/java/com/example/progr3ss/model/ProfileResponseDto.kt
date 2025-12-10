package com.example.progr3ss.model

data class ProfileResponseDto(
    val id: Int,
    val email: String,
    val username: String,
    val description: String?,
    val profileImageUrl: String?,
    val profileImageBase64: String?,
    val coverImageUrl: String?,
    val fcmToken: String?,
    val preferences: Map<String, Any>?,
    val created_at: String?,
    val updated_at: String?
)
