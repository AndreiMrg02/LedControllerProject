package com.example.ledcontrollerproject.ui.schedule.data

data class ScheduleItem(
    val label: String,
    val time: String,
    val daysSelected: Set<String>
) {

}