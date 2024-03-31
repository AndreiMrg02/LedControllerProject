package com.example.ledcontrollerproject.ui.schedule.data

import android.content.ContentValues.TAG
import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import com.google.gson.Gson
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

class ScheduleRepository(private val dataStore: DataStore<Preferences>) {
    private val SCHEDULE_ITEMS_KEY = stringPreferencesKey("schedule_items")

    // Flow to emit the list of schedule items
    val scheduleItems: Flow<List<ScheduleItem>> = dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                Log.e(TAG, "Error reading preferences.", exception)
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            // Retrieve schedule items from preferences
            val scheduleItemsJson =
                preferences[SCHEDULE_ITEMS_KEY] ?: return@map emptyList<ScheduleItem>()
            val scheduleItems = Gson().fromJson(scheduleItemsJson, Array<ScheduleItem>::class.java)
            scheduleItems.toList()
        }

    // Function to save schedule items
    suspend fun saveScheduleItems(scheduleItems: List<ScheduleItem>) {
        // Convert schedule items to JSON
        val scheduleItemsJson = Gson().toJson(scheduleItems)
        // Save JSON string to preferences
        dataStore.edit { preferences ->
            preferences[SCHEDULE_ITEMS_KEY] = scheduleItemsJson
        }
    }
}