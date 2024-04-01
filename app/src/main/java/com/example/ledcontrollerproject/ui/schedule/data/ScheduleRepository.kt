package com.example.ledcontrollerproject.ui.schedule.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "menu_item_data_store")
class ScheduleRepository(private val context: Context) {
    private val dataStore: DataStore<Preferences> = context.dataStore

    val menuItemDataFlow: Flow<List<ScheduleItem>> = dataStore.data
        .map { preferences ->
            val savedItemsJson = preferences[PreferencesKeys.MENU_ITEMS] ?: return@map emptyList<ScheduleItem>()
            val type = object : TypeToken<List<ScheduleItem>>() {}.type
            Gson().fromJson(savedItemsJson, type)
        }

    suspend fun saveMenuItemData(menuItems: List<ScheduleItem>) {
        val itemsJson = Gson().toJson(menuItems)
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.MENU_ITEMS] = itemsJson
        }
    }

    suspend fun addMenuItem(newItem: ScheduleItem) {
        val currentItems = menuItemDataFlow.firstOrNull() ?: emptyList()
        val updatedItems = currentItems.toMutableList().apply { add(newItem) }
        saveMenuItemData(updatedItems)
    }

    suspend fun updateMenuItem(updatedItem: ScheduleItem) {
        val currentItems = menuItemDataFlow.firstOrNull() ?: emptyList()
        val updatedItems = currentItems.map { item ->
            if (item.id == updatedItem.id) updatedItem else item
        }
        saveMenuItemData(updatedItems)
    }

    suspend fun removeMenuItem(itemToRemove: ScheduleItem) {
        val currentItems = menuItemDataFlow.firstOrNull() ?: emptyList()
        val updatedItems = currentItems.filter { item -> item.id != itemToRemove.id }
        saveMenuItemData(updatedItems)
    }

    private object PreferencesKeys {
        val MENU_ITEMS = stringPreferencesKey("menu_items")
    }

    suspend fun getMenuItemData(): List<ScheduleItem> {
        return menuItemDataFlow.firstOrNull() ?: emptyList()
    }
}