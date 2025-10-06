package com.example.dodaily.widgets

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.example.dodaily.R
import com.example.dodaily.data.DataManager
import com.example.dodaily.HomeActivity
import java.text.SimpleDateFormat
import java.util.*

class HabitWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    override fun onEnabled(context: Context) {
        // Called when the first widget is created
    }

    override fun onDisabled(context: Context) {
        // Called when the last widget is deleted
    }

    companion object {
        fun updateAppWidget(
            context: Context,
            appWidgetManager: AppWidgetManager,
            appWidgetId: Int
        ) {
            val dataManager = DataManager(context)
            val habits = dataManager.loadHabits()
            val today = Calendar.getInstance()
            
            // Calculate today's habit completion
            val completedHabits = habits.count { habit ->
                val completions = dataManager.getHabitCompletionsForDate(habit.id, today.time)
                completions.isNotEmpty()
            }
            
            val totalHabits = habits.size
            val completionPercentage = if (totalHabits > 0) {
                (completedHabits * 100) / totalHabits
            } else {
                0
            }
            
            // Create intent to open the app when widget is tapped
            val intent = Intent(context, HomeActivity::class.java)
            val pendingIntent = PendingIntent.getActivity(
                context, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            
            // Get the layout for the widget
            val views = RemoteViews(context.packageName, R.layout.widget_habit_progress)
            
            // Update the widget content
            views.setTextViewText(R.id.widget_title, "Today's Habits")
            views.setTextViewText(R.id.widget_progress_text, "$completedHabits of $totalHabits habits completed")
            views.setTextViewText(R.id.widget_percentage, "$completionPercentage%")
            views.setTextViewText(R.id.widget_date, getTodayDateString())
            
            // Set motivational text
            val motivationText = when {
                completionPercentage == 100 -> "Perfect! 🎉"
                completionPercentage >= 75 -> "Almost there! 🔥"
                completionPercentage >= 50 -> "Keep going! 💪"
                completionPercentage > 0 -> "Good start! 👍"
                else -> "Let's begin! 🚀"
            }
            views.setTextViewText(R.id.widget_motivation, motivationText)
            
            // Set click listener
            views.setOnClickPendingIntent(R.id.widget_container, pendingIntent)
            
            // Update the widget
            appWidgetManager.updateAppWidget(appWidgetId, views)
        }
        
        private fun getTodayDateString(): String {
            val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
            return dateFormat.format(Date())
        }
    }
}
