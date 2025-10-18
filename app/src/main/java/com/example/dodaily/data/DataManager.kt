package com.example.dodaily.data

import android.content.Context
import android.content.SharedPreferences
import com.example.dodaily.model.Habit
import com.example.dodaily.model.MoodEntry
import com.example.dodaily.model.HabitCompletion
import com.example.dodaily.model.HabitWithCompletion
import com.example.dodaily.model.DateType
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.util.Date

/**
 * DataManager handles all data persistence using SharedPreferences
 * This class manages habits, mood entries, and user settings
 */
class DataManager(private val context: Context) {
    
    private val prefs: SharedPreferences = context.getSharedPreferences("DoDailyPrefs", Context.MODE_PRIVATE)
    private val gson = Gson()
    
    companion object {
        private const val KEY_HABITS = "habits"
        private const val KEY_MOOD_ENTRIES = "mood_entries"
        private const val KEY_HABIT_COMPLETIONS = "habit_completions"
        private const val KEY_HYDRATION_REMINDER_ENABLED = "hydration_reminder_enabled"
        private const val KEY_HYDRATION_INTERVAL = "hydration_interval"
        private const val KEY_LAST_RESET_DATE = "last_reset_date"
        private const val KEY_USER_NAME = "user_name"
    }
    
    // ========== HABIT MANAGEMENT ==========
    
    /**
     * Save a list of habits to SharedPreferences
     */
    fun saveHabits(habits: List<Habit>) {
        val habitsJson = gson.toJson(habits)
        prefs.edit().putString(KEY_HABITS, habitsJson).apply()
    }
    
    /**
     * Load all habits from SharedPreferences
     */
    fun loadHabits(): List<Habit> {
        val habitsJson = prefs.getString(KEY_HABITS, null)
        return if (habitsJson != null) {
            try {
                val type = object : TypeToken<List<Habit>>() {}.type
                gson.fromJson(habitsJson, type) ?: emptyList()
            } catch (e: Exception) {
                emptyList()
            }
        } else {
            emptyList()
        }
    }
    
    /**
     * Add a new habit
     */
    fun addHabit(habit: Habit) {
        val habits = loadHabits().toMutableList()
        val newHabit = habit.copy(id = generateId())
        habits.add(newHabit)
        saveHabits(habits)
    }
    
    /**
     * Update an existing habit
     */
    fun updateHabit(habit: Habit) {
        val habits = loadHabits().toMutableList()
        val index = habits.indexOfFirst { it.id == habit.id }
        if (index != -1) {
            habits[index] = habit
            saveHabits(habits)
        }
    }
    
    /**
     * Delete a habit
     */
    fun deleteHabit(habitId: String) {
        val habits = loadHabits().toMutableList()
        habits.removeAll { it.id == habitId }
        saveHabits(habits)
    }
    
    /**
     * Reset all habits for a new day
     */
    fun resetHabitsForNewDay() {
        val today = Date()
        val lastReset = getLastResetDate()
        
        // Only reset if it's a new day
        if (lastReset == null || !isSameDay(today, lastReset)) {
            val habits = loadHabits().map { habit -> habit.copyResetForNewDay() }
            saveHabits(habits)
            setLastResetDate(today)
        }
    }
    
    // ========== HABIT COMPLETION TRACKING ==========
    
    /**
     * Update habit completion status for a specific date
     */
    fun updateHabitCompletion(habitId: String, date: Date, isCompleted: Boolean) {
        val completions = loadHabitCompletions()
        val dateKey = getDateKey(date)
        
        val existingCompletion = completions.find { it.habitId == habitId && it.dateKey == dateKey }
        if (existingCompletion != null) {
            // Update existing completion
            val updatedCompletion = existingCompletion.copy(
                isCompleted = isCompleted,
                completedCount = if (isCompleted) getHabitTargetCount(habitId) else existingCompletion.completedCount
            )
            val updatedCompletions = completions.map { if (it.habitId == habitId && it.dateKey == dateKey) updatedCompletion else it }
            saveHabitCompletions(updatedCompletions)
        } else if (isCompleted) {
            // Create new completion if marking as completed
            val newCompletion = HabitCompletion(
                habitId = habitId,
                dateKey = dateKey,
                completedCount = getHabitTargetCount(habitId),
                isCompleted = true
            )
            val updatedCompletions = completions + newCompletion
            saveHabitCompletions(updatedCompletions)
        }
    }
    
    /**
     * Track habit completion for a specific date
     */
    fun completeHabitForDate(habitId: String, date: Date, count: Int = 1) {
        val completions = loadHabitCompletions()
        val dateKey = getDateKey(date)
        
        val existingCompletion = completions.find { it.habitId == habitId && it.dateKey == dateKey }
        if (existingCompletion != null) {
            // Update existing completion
            val updatedCompletion = existingCompletion.copy(
                completedCount = existingCompletion.completedCount + count,
                isCompleted = existingCompletion.completedCount + count >= getHabitTargetCount(habitId)
            )
            val updatedCompletions = completions.map { if (it.habitId == habitId && it.dateKey == dateKey) updatedCompletion else it }
            saveHabitCompletions(updatedCompletions)
        } else {
            // Create new completion
            val newCompletion = HabitCompletion(
                habitId = habitId,
                dateKey = dateKey,
                completedCount = count,
                isCompleted = count >= getHabitTargetCount(habitId)
            )
            val updatedCompletions = completions + newCompletion
            saveHabitCompletions(updatedCompletions)
        }
    }
    
    /**
     * Get habit completions for a specific date
     */
    fun getHabitCompletionsForDate(date: Date): List<HabitCompletion> {
        val dateKey = getDateKey(date)
        return loadHabitCompletions().filter { it.dateKey == dateKey }
    }
    
    /**
     * Get habit completions for a specific habit and date
     */
    fun getHabitCompletionsForDate(habitId: String, date: Date): List<HabitCompletion> {
        val dateKey = getDateKey(date)
        return loadHabitCompletions().filter { it.habitId == habitId && it.dateKey == dateKey }
    }
    
    /**
     * Get all habits with their completion status for a specific date
     */
    fun getHabitsWithCompletionForDate(date: Date): List<HabitWithCompletion> {
        val habits = loadHabits()
        val completions = getHabitCompletionsForDate(date)
        
        return habits.map { habit ->
            val completion = completions.find { it.habitId == habit.id }
            HabitWithCompletion(
                habit = habit,
                completedCount = completion?.completedCount ?: 0,
                isCompleted = completion?.isCompleted ?: false,
                date = date
            )
        }
    }
    
    /**
     * Check if a date is in the past, present, or future
     */
    fun getDateType(date: Date): DateType {
        val today = Date()
        return when {
            isSameDay(date, today) -> DateType.TODAY
            date.before(today) -> DateType.PAST
            else -> DateType.FUTURE
        }
    }
    
    private fun loadHabitCompletions(): List<HabitCompletion> {
        val completionsJson = prefs.getString(KEY_HABIT_COMPLETIONS, null)
        return if (completionsJson != null) {
            try {
                val type = object : TypeToken<List<HabitCompletion>>() {}.type
                gson.fromJson(completionsJson, type) ?: emptyList()
            } catch (e: Exception) {
                emptyList()
            }
        } else {
            emptyList()
        }
    }
    
    private fun saveHabitCompletions(completions: List<HabitCompletion>) {
        val completionsJson = gson.toJson(completions)
        prefs.edit().putString(KEY_HABIT_COMPLETIONS, completionsJson).apply()
    }
    
    private fun getDateKey(date: Date): String {
        val calendar = java.util.Calendar.getInstance()
        calendar.time = date
        return "${calendar.get(java.util.Calendar.YEAR)}-${calendar.get(java.util.Calendar.MONTH)}-${calendar.get(java.util.Calendar.DAY_OF_MONTH)}"
    }
    
    private fun getHabitTargetCount(habitId: String): Int {
        val habit = loadHabits().find { it.id == habitId }
        return habit?.targetCount ?: 1
    }
    
    // ========== MOOD ENTRY MANAGEMENT ==========
    
    /**
     * Save a list of mood entries to SharedPreferences
     */
    fun saveMoodEntries(moodEntries: List<MoodEntry>) {
        val moodJson = gson.toJson(moodEntries)
        prefs.edit().putString(KEY_MOOD_ENTRIES, moodJson).apply()
    }
    
    /**
     * Load all mood entries from SharedPreferences
     */
    fun loadMoodEntries(): List<MoodEntry> {
        val moodJson = prefs.getString(KEY_MOOD_ENTRIES, null)
        return if (moodJson != null) {
            try {
                val type = object : TypeToken<List<MoodEntry>>() {}.type
                gson.fromJson(moodJson, type) ?: emptyList()
            } catch (e: Exception) {
                emptyList()
            }
        } else {
            emptyList()
        }
    }
    
    /**
     * Add a new mood entry
     */
    fun addMoodEntry(moodEntry: MoodEntry) {
        val moodEntries = loadMoodEntries().toMutableList()
        val newMoodEntry = moodEntry.copy(id = generateId())
        moodEntries.add(newMoodEntry)
        saveMoodEntries(moodEntries)
    }
    
    /**
     * Update an existing mood entry
     */
    fun updateMoodEntry(updatedMoodEntry: MoodEntry) {
        val moodEntries = loadMoodEntries().toMutableList()
        val index = moodEntries.indexOfFirst { it.id == updatedMoodEntry.id }
        if (index != -1) {
            moodEntries[index] = updatedMoodEntry
            saveMoodEntries(moodEntries)
        }
    }
    
    /**
     * Get mood entries for a specific date
     */
    fun getMoodEntriesForDate(date: Date): List<MoodEntry> {
        return loadMoodEntries().filter { isSameDay(it.dateTime, date) }
    }
    
    /**
     * Get mood entries for the last week
     */
    fun getMoodEntriesForLastWeek(): List<MoodEntry> {
        val weekAgo = Date(System.currentTimeMillis() - 7 * 24 * 60 * 60 * 1000L)
        return loadMoodEntries().filter { it.dateTime.after(weekAgo) }
    }
    
    // ========== HYDRATION REMINDER SETTINGS ==========
    
    /**
     * Set hydration reminder enabled/disabled
     */
    fun setHydrationReminderEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_HYDRATION_REMINDER_ENABLED, enabled).apply()
    }
    
    /**
     * Check if hydration reminder is enabled
     */
    fun isHydrationReminderEnabled(): Boolean {
        return prefs.getBoolean(KEY_HYDRATION_REMINDER_ENABLED, false)
    }
    
    /**
     * Set hydration reminder interval in minutes
     */
    fun setHydrationInterval(minutes: Int) {
        prefs.edit().putInt(KEY_HYDRATION_INTERVAL, minutes).apply()
    }
    
    /**
     * Get hydration reminder interval in minutes
     */
    fun getHydrationInterval(): Int {
        return prefs.getInt(KEY_HYDRATION_INTERVAL, 60) // Default 1 hour
    }
    
    // ========== USER SETTINGS ==========
    
    /**
     * Set user name
     */
    fun setUserName(name: String) {
        prefs.edit().putString(KEY_USER_NAME, name).apply()
    }
    
    /**
     * Get user name
     */
    fun getUserName(): String {
        return prefs.getString(KEY_USER_NAME, "User") ?: "User"
    }
    
    // ========== PRIVATE HELPER METHODS ==========
    
    private fun generateId(): String {
        return System.currentTimeMillis().toString()
    }
    
    private fun getLastResetDate(): Date? {
        val timestamp = prefs.getLong(KEY_LAST_RESET_DATE, -1)
        return if (timestamp != -1L) Date(timestamp) else null
    }
    
    private fun setLastResetDate(date: Date) {
        prefs.edit().putLong(KEY_LAST_RESET_DATE, date.time).apply()
    }
    
    private fun isSameDay(date1: Date, date2: Date): Boolean {
        val cal1 = java.util.Calendar.getInstance()
        val cal2 = java.util.Calendar.getInstance()
        cal1.time = date1
        cal2.time = date2
        return cal1.get(java.util.Calendar.YEAR) == cal2.get(java.util.Calendar.YEAR) &&
                cal1.get(java.util.Calendar.DAY_OF_YEAR) == cal2.get(java.util.Calendar.DAY_OF_YEAR)
    }
    
    // Hydration Settings Methods
    fun saveDailyHydrationGoal(goal: Int) {
        prefs.edit().putInt("daily_hydration_goal", goal).apply()
    }
    
    fun getDailyHydrationGoal(): Int {
        return prefs.getInt("daily_hydration_goal", 2500) // Default 2500ml
    }
    
    fun saveHydrationRemindersEnabled(enabled: Boolean) {
        prefs.edit().putBoolean("hydration_reminders_enabled", enabled).apply()
    }
    
    fun areHydrationRemindersEnabled(): Boolean {
        return prefs.getBoolean("hydration_reminders_enabled", true)
    }
    
    fun saveHydrationReminderSound(sound: String) {
        prefs.edit().putString("hydration_reminder_sound", sound).apply()
    }
    
    fun getHydrationReminderSound(): String {
        return prefs.getString("hydration_reminder_sound", "Default") ?: "Default"
    }
    
    fun saveHydrationReminders(reminders: List<String>) {
        val remindersJson = reminders.joinToString(",")
        prefs.edit().putString("hydration_reminders", remindersJson).apply()
    }
    
    fun getHydrationReminders(): List<String> {
        val remindersJson = prefs.getString("hydration_reminders", "") ?: ""
        return if (remindersJson.isEmpty()) {
            listOf("8:0:AM", "12:0:PM", "4:0:PM") // Default reminders
        } else {
            remindersJson.split(",")
        }
    }
    
    // Hydration Schedule Methods
    fun saveHydrationSchedules(schedules: List<com.example.dodaily.model.HydrationSchedule>) {
        val schedulesJson = schedules.joinToString("|") { schedule ->
            "${schedule.id},${schedule.time},${schedule.description},${schedule.isCompleted}"
        }
        prefs.edit().putString("hydration_schedules", schedulesJson).apply()
    }
    
    fun getHydrationSchedules(): List<com.example.dodaily.model.HydrationSchedule> {
        val schedulesJson = prefs.getString("hydration_schedules", "") ?: ""
        return if (schedulesJson.isEmpty()) {
            emptyList()
        } else {
            schedulesJson.split("|").mapNotNull { scheduleString ->
                val parts = scheduleString.split(",")
                if (parts.size == 4) {
                    com.example.dodaily.model.HydrationSchedule(
                        id = parts[0],
                        time = parts[1],
                        description = parts[2],
                        isCompleted = parts[3].toBoolean()
                    )
                } else null
            }
        }
    }
}
