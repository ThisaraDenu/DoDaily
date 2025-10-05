package com.example.dodaily.services

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.example.dodaily.receivers.HydrationAlarmReceiver
import java.util.*

class HydrationAlarmManager(private val context: Context) {
    
    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    
    fun scheduleHydrationReminder(time: String, hour: Int, minute: Int) {
        android.util.Log.d("HydrationAlarmManager", "Scheduling reminder for $time (hour: $hour, minute: $minute)")
        val intent = Intent(context, HydrationAlarmReceiver::class.java).apply {
            putExtra("time", time)
        }
        
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            time.hashCode(), // Use time string hash as request code for uniqueness
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        // Create calendar for the scheduled time
        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            
            // If the time has already passed today, schedule for tomorrow
            if (timeInMillis <= System.currentTimeMillis()) {
                add(Calendar.DAY_OF_MONTH, 1)
            }
        }
        
        android.util.Log.d("HydrationAlarmManager", "Scheduled time: ${calendar.time}")
        android.util.Log.d("HydrationAlarmManager", "Current time: ${Date()}")
        
        // Schedule the alarm
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                calendar.timeInMillis,
                pendingIntent
            )
        } else {
            alarmManager.setExact(
                AlarmManager.RTC_WAKEUP,
                calendar.timeInMillis,
                pendingIntent
            )
        }
    }
    
    fun cancelHydrationReminder(time: String) {
        val intent = Intent(context, HydrationAlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            time.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        alarmManager.cancel(pendingIntent)
    }
    
    fun cancelAllHydrationReminders() {
        // This would need to be implemented to cancel all scheduled reminders
        // For now, we'll handle this in the fragment when schedules are deleted
    }
}
