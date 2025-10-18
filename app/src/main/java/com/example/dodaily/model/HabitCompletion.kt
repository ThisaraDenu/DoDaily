package com.example.dodaily.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.Date

/**
 * Data class representing a habit completion for a specific date
 * @param habitId ID of the habit that was completed
 * @param dateKey String representation of the date (YYYY-MM-DD)
 * @param completedCount Number of times the habit was completed on this date
 * @param isCompleted Whether the habit target was fully met
 * @param completedAt Timestamp when the completion was recorded
 */
@Entity(
    tableName = "habit_completions",
    indices = [Index(value = ["habitId", "dateKey"], unique = true)]
)
data class HabitCompletion(
    @PrimaryKey(autoGenerate = true) val completionId: Long = 0,
    val habitId: String,
    val dateKey: String,
    val completedCount: Int = 0,
    val isCompleted: Boolean = false,
    val completedAt: Date = Date()
)

/**
 * Data class representing a habit with its completion status for a specific date
 * @param habit The habit object
 * @param completedCount Number of times completed on the specific date
 * @param isCompleted Whether fully completed on the specific date
 * @param date The date this completion status refers to
 */
data class HabitWithCompletion(
    val habit: Habit,
    val completedCount: Int,
    val isCompleted: Boolean,
    val date: Date
)

/**
 * Enum representing the type of date (past, present, future)
 */
enum class DateType {
    PAST,    // Date is in the past
    TODAY,   // Date is today
    FUTURE   // Date is in the future
}
