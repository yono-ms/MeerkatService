package com.example.meerkatservice.extensions

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

val KEY_OLD_LOCATION_SCREEN = booleanPreferencesKey("old_location_screen")

suspend fun Context.saveOldLocationScreen(value: Boolean) {
    dataStore.edit {
        it[KEY_OLD_LOCATION_SCREEN] = value
    }
}

val Context.oldLocationScreenFlow
    get() = dataStore.data.map {
        it[KEY_OLD_LOCATION_SCREEN] ?: false
    }

fun Context.openAppSettings() {
    Intent(
        Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
        Uri.fromParts("package", packageName, null)
    ).also { intent ->
        startActivity(intent)
    }
}
