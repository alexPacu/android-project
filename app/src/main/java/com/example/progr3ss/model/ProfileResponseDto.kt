package com.example.progr3ss.model

data class ProfileResponseDto(
    val id: Int,
    val email: String,
    val username: String,
    val description: String?,
    val profileImageUrl: String?
)
