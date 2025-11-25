package com.example.progr3ss.model

import com.google.gson.annotations.SerializedName

data class ScheduleResponseDto(
    val id: Int,
    val title: String,
    val description: String?,
    @SerializedName("start_time")
    val startTime: String,
    @SerializedName("end_time")
    val endTime: String?,
    val date: String,
    val status: ScheduleStatus,
    @SerializedName("created_at")
    val createdAt: String?,
    @SerializedName("updated_at")
    val updatedAt: String?
)

enum class ScheduleStatus {
    @SerializedName("Planned")
    PLANNED,
    @SerializedName("Completed")
    COMPLETED,
    @SerializedName("Skipped")
    SKIPPED
}

