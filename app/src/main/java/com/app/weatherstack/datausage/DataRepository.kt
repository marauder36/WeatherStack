package com.app.weatherstack.datausage

import android.content.Context
import android.net.TrafficStats

class DataRepository(private val context: Context) {

    private val sharedPreferences = context.getSharedPreferences("MyAppPreferences", Context.MODE_PRIVATE)

    fun getDataUsage(): Long {
        val uid = android.os.Process.myUid()
        val rxBytes = TrafficStats.getUidRxBytes(uid)
        val txBytes = TrafficStats.getUidTxBytes(uid)
        return rxBytes + txBytes
    }

    fun getDataLimit(): Long {
        return sharedPreferences.getLong("dataLimit", Long.MAX_VALUE) // Unlimited by default
    }

    fun setDataLimit(limit: Long) {
        sharedPreferences.edit().putLong("dataLimit", limit).apply()
    }

    // Additional methods as needed
}
