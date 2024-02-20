package com.app.weatherstack.workers

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.net.TrafficStats
import androidx.core.app.NotificationCompat
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.app.weatherstack.R

class DataUsageWorker(appContext: Context, workerParams: WorkerParameters) :
    Worker(appContext, workerParams) {

    override fun doWork(): Result {
        // Get the stored data limit
        val sharedPreferences = applicationContext.getSharedPreferences("MyAppPreferences", Context.MODE_PRIVATE)
        val dataLimit = sharedPreferences.getLong("dataLimit", Long.MAX_VALUE) // Unlimited by default

        // Get current data usage
        val uid = android.os.Process.myUid()
        val rxBytes = TrafficStats.getUidRxBytes(uid)
        val txBytes = TrafficStats.getUidTxBytes(uid)
        val totalBytes = rxBytes + txBytes

        // Check if the data limit is exceeded
        if (totalBytes >= dataLimit) {
            // Notify the user or take action
            notifyUser()
        }

        return Result.success()
    }

    private fun notifyUser() {
        val notificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Create a notification channel for Android O and above
        val channel = NotificationChannel("dataUsageChannel", "Data Usage Notifications", NotificationManager.IMPORTANCE_DEFAULT)
        notificationManager.createNotificationChannel(channel)

        val notification = NotificationCompat.Builder(applicationContext, "dataUsageChannel")
            .setContentTitle("Data Limit Reached")
            .setContentText("You have reached your set data limit.")
            .setSmallIcon(R.drawable.round_warning) // Replace with your notification icon
            .build()

        notificationManager.notify(1, notification)
    }

}
