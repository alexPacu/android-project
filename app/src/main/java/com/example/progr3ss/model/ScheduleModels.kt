package com.example.progr3ss.model

import com.google.gson.annotations.SerializedName

data class ScheduleResponseDto(
    val id: Int,
    @SerializedName("start_time")
    val startTime: String,
    @SerializedName("end_time")
    val endTime: String?,
    val status: String,
    val date: String,
    @SerializedName("is_custom")
    val isCustom: Boolean,
    @SerializedName("created_at")
    val createdAt: String?,
    @SerializedName("updated_at")
    val updatedAt: String?,
    val type: String?,
    @SerializedName("duration_minutes")
    val durationMinutes: Int?,
    val notes: String?,
    val participants: List<Any>?,
    val habit: HabitInSchedule,
    val progress: List<Any>?,
    @SerializedName("is_participant_only")
    val isParticipantOnly: Boolean
) {
    val title: String get() = habit.name
    val description: String? get() = habit.description
    val scheduleStatus: ScheduleStatus
        get() = when (status) {
            "Planned" -> ScheduleStatus.PLANNED
            "Completed" -> ScheduleStatus.COMPLETED
            "Skipped" -> ScheduleStatus.SKIPPED
            else -> ScheduleStatus.PLANNED
        }
}

data class HabitInSchedule(
    val id: Int,
    val name: String,
    val description: String?,
    val category: CategoryInHabit,
    val goal: String?,
    @SerializedName("created_at")
    val createdAt: String?,
    @SerializedName("updated_at")
    val updatedAt: String?
)

data class CategoryInHabit(
    val id: Int,
    val name: String,
    val iconUrl: String
)

enum class ScheduleStatus {
    @SerializedName("Planned")
    PLANNED,
    @SerializedName("Completed")
    COMPLETED,
    @SerializedName("Skipped")
    SKIPPED
}

data class HabitResponseDto(
    val id: Int,
    val name: String,
    val description: String?,
    val category: CategoryDto,
    val goal: String?,
    @SerializedName("created_at")
    val createdAt: String?,
    @SerializedName("updated_at")
    val updatedAt: String?
)

data class CategoryDto(
    val id: Int,
    val name: String,
    val iconUrl: String
)

data class CreateHabitRequest(
    val name: String,
    val description: String? = null,
    val categoryId: Int,
    val goal: String
)

data class CreateCustomScheduleRequest(
    @SerializedName("habitId")
    val habitId: Int,
    val date: String,
    @SerializedName("start_time")
    val startTime: String,
    @SerializedName("end_time")
    val endTime: String? = null,
    @SerializedName("duration_minutes")
    val durationMinutes: Int? = null,
    @SerializedName("is_custom")
    val isCustom: Boolean = true,
    @SerializedName("participantIds")
    val participantIds: List<Int>? = null,
    val notes: String? = null
)

data class CreateRecurringScheduleRequest(
    @SerializedName("habitId")
    val habitId: Int,
    @SerializedName("start_time")
    val startTime: String,
    @SerializedName("end_time")
    val endTime: String? = null,
    @SerializedName("duration_minutes")
    val durationMinutes: Int? = null,
    @SerializedName("repeatPattern")
    val repeatPattern: String, // "none", "daily", "weekdays", "weekends"
    @SerializedName("repeatDays")
    val repeatDays: Int = 30,
    @SerializedName("is_custom")
    val isCustom: Boolean = true,
    @SerializedName("participantIds")
    val participantIds: List<Int>? = null,
    val notes: String? = null
)

data class CreateWeekdayRecurringScheduleRequest(
    @SerializedName("habitId")
    val habitId: Int,
    @SerializedName("start_time")
    val startTime: String,
    @SerializedName("daysOfWeek")
    val daysOfWeek: List<Int>, // 1=Monday ... 7=Sunday
    @SerializedName("numberOfWeeks")
    val numberOfWeeks: Int = 4,
    @SerializedName("end_time")
    val endTime: String? = null,
    @SerializedName("duration_minutes")
    val durationMinutes: Int? = null,
    @SerializedName("is_custom")
    val isCustom: Boolean = true,
    @SerializedName("participantIds")
    val participantIds: List<Int>? = null,
    val notes: String? = null
)

