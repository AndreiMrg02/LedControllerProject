package com.example.ledcontrollerproject.ui.schedule.data

import android.content.Context
import android.content.SharedPreferences

object ScheduleItemIdGenerator {
    private const val PREFS_NAME = "schedule_item_id_prefs"
    private const val KEY_CURRENT_ID = "current_id"

    private lateinit var sharedPreferences: SharedPreferences

    fun initialize(context: Context) {
        sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        currentId = sharedPreferences.getInt(KEY_CURRENT_ID, 0)
    }

    private var currentId: Int
        get() = sharedPreferences.getInt(KEY_CURRENT_ID, 0)
        set(value) {
            sharedPreferences.edit().putInt(KEY_CURRENT_ID, value).apply()
        }

    fun nextId(): Int {
        return ++currentId
    }
}


data class ScheduleItem(
    val id: Int,
    val label: String,
    val time: String,
    val daysSelected: List<String>
) {
    constructor(label: String, time: String, daysSelected: List<String>) : this(ScheduleItemIdGenerator.nextId(), label, time, daysSelected)
}