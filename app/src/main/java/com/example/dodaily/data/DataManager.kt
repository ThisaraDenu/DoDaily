package com.example.dodaily.data

import android.content.Context
import android.content.SharedPreferences
import com.example.dodaily.data.local.DatabaseProvider
import com.example.dodaily.model.DateType
import com.example.dodaily.model.Habit
import com.example.dodaily.model.HabitCompletion
import com.example.dodaily.model.HabitWithCompletion
import com.example.dodaily.model.HydrationSchedule
import com.example.dodaily.model.MoodEntry
import java.util.Date

/**
 * DataManager now persists core data via Room (SQLite) and keeps
 * SharedPreferences for settings and lightweight flags.
 */
class DataManager(private val context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences("DoDailyPrefs", Context.MODE_PRIVATE)

    // Database and DAOs (sync access; DB is configured to allow main thread queries)
    private val db by lazy { DatabaseProvider.get(context) }
    private val habitDao by lazy { db.habitDao() }
    private val habitCompletionDao by lazy { db.habitCompletionDao() }
    private val moodEntryDao by lazy { db.moodEntryDao() }
    private val hydrationScheduleDao by lazy { db.hydrationScheduleDao() }

    companion object {
        // Legacy keys kept for settings; habit/mood/completion moved to Room
        private const val KEY_HABITS = "habits" // no longer used
        private const val KEY_MOOD_ENTRIES = "mood_entries" // no longer used
        private const val KEY_HABIT_COMPLETIONS = "habit_completions" // no longer used
        private const val KEY_HYDRATION_REMINDER_ENABLED = "hydration_reminder_enabled"
        private const val KEY_HYDRATION_INTERVAL = "hydration_interval"
        private const val KEY_LAST_RESET_DATE = "last_reset_date"
        private const val KEY_USER_NAME = "user_name"
    }

    // ========== HABIT MANAGEMENT (Room) ==========

    fun saveHabits(habits: List<Habit>) {
        habitDao.deleteAll()
        if (habits.isNotEmpty()) {
            habitDao.insertAll(habits)
        }
    }

    fun loadHabits(): List<Habit> {
        return habitDao.getAll()
    }

    fun addHabit(habit: Habit) {
        val newHabit = habit.copy(id = generateId())
        habitDao.insert(newHabit)
    }

    fun updateHabit(habit: Habit) {
        habitDao.update(habit)
    }

    fun deleteHabit(habitId: String) {
        habitDao.deleteById(habitId)
    }

    fun resetHabitsForNewDay() {
        val today = Date()
        val lastReset = getLastResetDate()
        if (lastReset == null || !isSameDay(today, lastReset)) {
            loadHabits().forEach { habit ->
                val reset = habit.copyResetForNewDay()
                habitDao.update(reset)
            }
            setLastResetDate(today)
        }
    }

    // ========== HABIT COMPLETION TRACKING (Room) ==========

    fun updateHabitCompletion(habitId: String, date: Date, isCompleted: Boolean) {
        val dateKey = getDateKey(date)
        val existing = habitCompletionDao.getForHabitAndDate(habitId, dateKey)
        if (existing != null) {
            val updated = existing.copy(
                isCompleted = isCompleted,
                completedCount = if (isCompleted) getHabitTargetCount(habitId) else existing.completedCount
            )
            habitCompletionDao.update(updated)
        } else if (isCompleted) {
            val newCompletion = HabitCompletion(
                habitId = habitId,
                dateKey = dateKey,
                completedCount = getHabitTargetCount(habitId),
                isCompleted = true
            )
            habitCompletionDao.insert(newCompletion)
        }
    }

    fun completeHabitForDate(habitId: String, date: Date, count: Int = 1) {
        val dateKey = getDateKey(date)
        val existing = habitCompletionDao.getForHabitAndDate(habitId, dateKey)
        if (existing != null) {
            val updated = existing.copy(
                completedCount = existing.completedCount + count,
                isCompleted = existing.completedCount + count >= getHabitTargetCount(habitId)
            )
            habitCompletionDao.update(updated)
        } else {
            val newCompletion = HabitCompletion(
                habitId = habitId,
                dateKey = dateKey,
                completedCount = count,
                isCompleted = count >= getHabitTargetCount(habitId)
            )
            habitCompletionDao.insert(newCompletion)
        }
    }

    fun getHabitCompletionsForDate(date: Date): List<HabitCompletion> {
        val dateKey = getDateKey(date)
        return habitCompletionDao.getForDate(dateKey)
    }

    fun getHabitCompletionsForDate(habitId: String, date: Date): List<HabitCompletion> {
        val dateKey = getDateKey(date)
        habitCompletionDao.getForHabitAndDate(habitId, dateKey)?.let { return listOf(it) }
        return emptyList()
    }

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

    fun getDateType(date: Date): DateType {
        val today = Date()
        return when {
            isSameDay(date, today) -> DateType.TODAY
            date.before(today) -> DateType.PAST
            else -> DateType.FUTURE
        }
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

    // ========== MOOD ENTRY MANAGEMENT (Room) ==========

    fun saveMoodEntries(moodEntries: List<MoodEntry>) {
        moodEntryDao.deleteAll()
        if (moodEntries.isNotEmpty()) {
            moodEntryDao.insertAll(moodEntries)
        }
    }

    fun loadMoodEntries(): List<MoodEntry> {
        return moodEntryDao.getAll()
    }

    fun addMoodEntry(moodEntry: MoodEntry) {
        val newEntry = moodEntry.copy(id = generateId())
        moodEntryDao.insert(newEntry)
    }

    fun updateMoodEntry(updatedMoodEntry: MoodEntry) {
        moodEntryDao.update(updatedMoodEntry)
    }

    fun getMoodEntriesForDate(date: Date): List<MoodEntry> {
        return loadMoodEntries().filter { isSameDay(it.dateTime, date) }
    }

    fun getMoodEntriesForLastWeek(): List<MoodEntry> {
        val weekAgo = Date(System.currentTimeMillis() - 7 * 24 * 60 * 60 * 1000L)
        return loadMoodEntries().filter { it.dateTime.after(weekAgo) }
    }

    // ========== HYDRATION REMINDER SETTINGS (SharedPreferences) ==========

    fun setHydrationReminderEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_HYDRATION_REMINDER_ENABLED, enabled).apply()
    }

    fun isHydrationReminderEnabled(): Boolean {
        return prefs.getBoolean(KEY_HYDRATION_REMINDER_ENABLED, false)
    }

    fun setHydrationInterval(minutes: Int) {
        prefs.edit().putInt(KEY_HYDRATION_INTERVAL, minutes).apply()
    }

    fun getHydrationInterval(): Int {
        return prefs.getInt(KEY_HYDRATION_INTERVAL, 60) // Default 1 hour
    }

    // ========== USER SETTINGS (SharedPreferences) ==========

    fun setUserName(name: String) {
        prefs.edit().putString(KEY_USER_NAME, name).apply()
    }

    fun getUserName(): String {
        return prefs.getString(KEY_USER_NAME, "User") ?: "User"
    }

    // ========== PRIVATE HELPERS ==========

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

    // ========== HYDRATION SCHEDULES (Room) ==========

    fun saveHydrationSchedules(schedules: List<HydrationSchedule>) {
        hydrationScheduleDao.deleteAll()
        if (schedules.isNotEmpty()) {
            hydrationScheduleDao.insertAll(schedules)
        }
    }

    fun getHydrationSchedules(): List<HydrationSchedule> {
        return hydrationScheduleDao.getAll()
    }

    // Additional hydration preferences stored in SharedPreferences
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
}
