package com.example.dodaily.data

import android.content.Context
import android.content.SharedPreferences
import com.example.dodaily.model.Habit
import com.example.dodaily.model.MoodEntry
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
            val habits = loadHabits().map { it.resetForNewDay() }
            saveHabits(habits)
            setLastResetDate(today)
        }
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
}
