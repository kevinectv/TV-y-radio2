package com.example.data.model

data class EPGProgram(
    val id: String,
    val channelId: String,
    val title: String,
    val description: String,
    val startTime: String, // 12-hour format e.g. "06:00 PM"
    val endTime: String,   // 12-hour format e.g. "07:30 PM"
    val startHourDecimal: Float, // for layout: e.g. 18.0f for 18h
    val durationHours: Float,    // for cell width: e.g. 1.5f (1.5 hours)
    val thumbnailUrl: String = "",
    val category: String = "General"
) {
    // Calculates if this program is active given a certain decimal hour (e.g., 18.5f)
    fun isActiveAt(hourDecimal: Float): Boolean {
        val end = startHourDecimal + durationHours
        return hourDecimal >= startHourDecimal && hourDecimal < end
    }
}
