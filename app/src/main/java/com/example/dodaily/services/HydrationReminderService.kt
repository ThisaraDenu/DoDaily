package com.example.dodaily.services

import android.app.*
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.example.dodaily.R
import com.example.dodaily.data.DataManager
import java.util.*

/**
 * Service for managing hydration reminders
 * Uses AlarmManager to schedule periodic notifications
 */
class HydrationReminderService : Service() {
    
    private lateinit var dataManager: DataManager
    private lateinit var notificationManager: NotificationManager
    private lateinit var alarmManager: AlarmManager
    
    companion object {
        private const val NOTIFICATION_ID = 1001
        private const val CHANNEL_ID = "hydration_reminder_channel"
        private const val ACTION_START_REMINDER = "start_reminder"
        private const val ACTION_STOP_REMINDER = "stop_reminder"
        
        fun startService(context: Context) {
            val intent = Intent(context, HydrationReminderService::class.java).apply {
                action = ACTION_START_REMINDER
            }
            context.startService(intent)
        }
        
        fun stopService(context: Context) {
            val intent = Intent(context, HydrationReminderService::class.java).apply {
                action = ACTION_STOP_REMINDER
            }
            context.startService(intent)
        }
    }
    
    override fun onCreate() {
        super.onCreate()
        dataManager = DataManager(this)
        notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        
        createNotificationChannel()
    }
    
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START_REMINDER -> startReminder()
            ACTION_STOP_REMINDER -> stopReminder()
        }
        return START_STICKY
    }
    
    override fun onBind(intent: Intent?): IBinder? = null
    
    private fun createNotificationChannel() {
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
    }
    
    private fun startReminder() {
        if (!dataManager.isHydrationReminderEnabled()) {
            stopSelf()
            return
        }
        
        val intervalMinutes = dataManager.getHydrationInterval()
        val intervalMillis = intervalMinutes * 60 * 1000L
        
        // Cancel existing alarms
        cancelReminder()
        
        // Schedule new alarm
        val intent = Intent(this, HydrationReminderReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            this,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        val triggerTime = System.currentTimeMillis() + intervalMillis
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                triggerTime,
                pendingIntent
            )
        } else {
            alarmManager.setExact(
                AlarmManager.RTC_WAKEUP,
                triggerTime,
                pendingIntent
            )
        }
        
        // Show initial notification
        showNotification("Hydration reminder scheduled", "You'll be reminded every $intervalMinutes minutes")
        
        // Schedule next reminder
        scheduleNextReminder()
    }
    
    private fun stopReminder() {
        cancelReminder()
        notificationManager.cancel(NOTIFICATION_ID)
        stopSelf()
    }
    
    private fun cancelReminder() {
        val intent = Intent(this, HydrationReminderReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            this,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pendingIntent)
    }
    
    private fun scheduleNextReminder() {
        if (!dataManager.isHydrationReminderEnabled()) {
            return
        }
        
        val intervalMinutes = dataManager.getHydrationInterval()
        val intervalMillis = intervalMinutes * 60 * 1000L
        
        val intent = Intent(this, HydrationReminderReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            this,
            1,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        val triggerTime = System.currentTimeMillis() + intervalMillis
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                triggerTime,
                pendingIntent
            )
        } else {
            alarmManager.setExact(
                AlarmManager.RTC_WAKEUP,
                triggerTime,
                pendingIntent
            )
        }
    }
    
    private fun showNotification(title: String, message: String) {
        val intent = Intent(this, com.example.dodaily.HomeActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(message)
            .setSmallIcon(R.drawable.ic_water_drop)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()
        
        notificationManager.notify(NOTIFICATION_ID, notification)
    }
}
