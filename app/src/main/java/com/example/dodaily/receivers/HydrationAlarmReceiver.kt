package com.example.dodaily.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.dodaily.services.HydrationNotificationService

class HydrationAlarmReceiver : BroadcastReceiver() {
    
    override fun onReceive(context: Context, intent: Intent) {
        android.util.Log.d("HydrationAlarmReceiver", "Alarm received!")
        val time = intent.getStringExtra("time") ?: "Unknown time"
        android.util.Log.d("HydrationAlarmReceiver", "Time: $time")
        
        // Show notification and vibrate
        HydrationNotificationService.showHydrationNotification(context, time)
    }
}
