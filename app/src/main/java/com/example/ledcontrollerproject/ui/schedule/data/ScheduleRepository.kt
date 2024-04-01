package com.example.ledcontrollerproject.ui.schedule.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "menu_item_data_store")
class ScheduleRepository(private val context: Context) {
    private val dataStore: DataStore<Preferences> = context.dataStore
    val menuItemDataFlow: Flow<ScheduleItem?> = dataStore.data
        .map { preferences ->
            val label = preferences[PreferencesKeys.LABEL] ?: "Eticheta"
            val time = preferences[PreferencesKeys.TIME] ?: "12:00"
            val daysSelected = preferences[PreferencesKeys.DAYS_SELECTED]?.split(",")?.toSet() ?: emptySet()
            ScheduleItem(label, time, daysSelected)
        }

    suspend fun saveMenuItemData(menuItemData: ScheduleItem) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.LABEL] = menuItemData.label
            preferences[PreferencesKeys.TIME] = menuItemData.time
            preferences[PreferencesKeys.DAYS_SELECTED] = menuItemData.daysSelected.joinToString(",")
        }
    }

    private object PreferencesKeys {
        val LABEL = stringPreferencesKey("label")
        val TIME = stringPreferencesKey("time")
        val DAYS_SELECTED = stringPreferencesKey("days_selected")
    }

//    // Flow to emit the list of schedule items
//    val scheduleItems: Flow<List<ScheduleItem>> = dataStore.data
//        .catch { exception ->
//            if (exception is IOException) {
//                Log.e(TAG, "Error reading preferences.", exception)
//                emit(emptyPreferences())
//            } else {
//                throw exception
//            }
//        }
//        .map { preferences ->
//            // Retrieve schedule items from preferences
//            val scheduleItemsJson =
//                preferences[SCHEDULE_ITEMS_KEY] ?: return@map emptyList<ScheduleItem>()
//            val scheduleItems = Gson().fromJson(scheduleItemsJson, Array<ScheduleItem>::class.java)
//            scheduleItems.toList()
//        }
//
//    // Function to save schedule items
//    suspend fun saveScheduleItems(scheduleItems: List<ScheduleItem>) {
//        // Convert schedule items to JSON
//        val scheduleItemsJson = Gson().toJson(scheduleItems)
//        // Save JSON string to preferences
//        dataStore.edit { preferences ->
//            preferences[SCHEDULE_ITEMS_KEY] = scheduleItemsJson
//        }
//    }
//    suspend fun loadScheduleItems(): List<ScheduleItem> {
//        return try {
//            scheduleItems.first() // Get the latest value emitted by the flow
//        } catch (e: Exception) {
//            Log.e(TAG, "Error loading schedule items.", e)
//            emptyList() // Return an empty list if an error occurs
//        }
//    }
}