package com.example.dodaily.services

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.app.NotificationManager
import android.app.NotificationChannel
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.dodaily.R
import com.example.dodaily.data.DataManager
import java.util.*

/**
 * BroadcastReceiver for handling hydration reminder notifications
 */
class HydrationReminderReceiver : BroadcastReceiver() {
    
    companion object {
        private const val NOTIFICATION_ID = 1001
        private const val CHANNEL_ID = "hydration_reminder_channel"
    }
    
    override fun onReceive(context: Context, intent: Intent) {
        val dataManager = DataManager(context)
        
        // Check if reminders are still enabled
        if (!dataManager.isHydrationReminderEnabled()) {
            return
        }
        
        // Show notification
        showHydrationNotification(context)
        
        // Schedule next reminder
        scheduleNextReminder(context, dataManager)
    }
    
    private fun showHydrationNotification(context: Context) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        
        // Create notification channel for Android O and above
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Hydration Reminders",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Reminders to drink water"
                enableLights(true)
                enableVibration(true)
            }
            notificationManager.createNotificationChannel(channel)
        }
        
        // Create notification
        val intent = Intent(context, com.example.dodaily.HomeActivity::class.java)
        val pendingIntent = android.app.PendingIntent.getActivity(
            context,
            0,
            intent,
            android.app.PendingIntent.FLAG_UPDATE_CURRENT or android.app.PendingIntent.FLAG_IMMUTABLE
        )
        
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setContentTitle("💧 Time to Hydrate!")
            .setContentText("Don't forget to drink some water. Stay hydrated! 💧")
            .setSmallIcon(R.drawable.ic_water_drop)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .build()
        
        notificationManager.notify(NOTIFICATION_ID, notification)
    }
    
    private fun scheduleNextReminder(context: Context, dataManager: DataManager) {
        val intervalMinutes = dataManager.getHydrationInterval()
        val intervalMillis = intervalMinutes * 60 * 1000L
        
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as android.app.AlarmManager
        val intent = Intent(context, HydrationReminderReceiver::class.java)
        val pendingIntent = android.app.PendingIntent.getBroadcast(
            context,
            1,
            intent,
            android.app.PendingIntent.FLAG_UPDATE_CURRENT or android.app.PendingIntent.FLAG_IMMUTABLE
        )
        
        val triggerTime = System.currentTimeMillis() + intervalMillis
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setExactAndAllowWhileIdle(
                android.app.AlarmManager.RTC_WAKEUP,
                triggerTime,
                pendingIntent
            )
        } else {
            alarmManager.setExact(
                android.app.AlarmManager.RTC_WAKEUP,
                triggerTime,
                pendingIntent
            )
        }
    }
}
