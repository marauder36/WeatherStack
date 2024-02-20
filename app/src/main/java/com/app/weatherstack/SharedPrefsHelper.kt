package com.app.weatherstack

import android.content.SharedPreferences

object SharedPrefsHelper {
    private lateinit var sharedPreferences: SharedPreferences

    fun init(prefs: SharedPreferences) {
        sharedPreferences = prefs
    }

    fun getSelectedUnitType(): String {
        return sharedPreferences.getString("LastTempUnit", "C") ?: "C"
    }
}
