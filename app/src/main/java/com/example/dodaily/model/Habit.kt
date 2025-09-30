package com.example.dodaily.model

import java.util.Date

/**
 * Data class representing a daily habit
 * @param id Unique identifier for the habit
 * @param name Name of the habit (e.g., "Drink Water", "Meditate")
 * @param description Optional description of the habit
 * @param targetCount Target number of times to complete the habit per day
 * @param currentCount Current completion count for today
 * @param isCompleted Whether the habit is completed for today
 * @param createdDate When the habit was created
 * @param emoji Emoji representation of the habit
 */
data class Habit(
    val id: String = "",
    val name: String = "",
    val description: String = "",
    val targetCount: Int = 1,
    var currentCount: Int = 0,
    var isCompleted: Boolean = false,
    val createdDate: Date = Date(),
    val emoji: String = "📝"
) {
    /**
     * Calculate completion percentage for today
     */
    fun getCompletionPercentage(): Float {
        return if (targetCount > 0) {
            (currentCount.toFloat() / targetCount.toFloat() * 100).coerceAtMost(100f)
        } else 0f
    }
    
    /**
     * Check if habit is fully completed for today
     */
    fun isFullyCompleted(): Boolean {
        return currentCount >= targetCount
    }
    
    /**
     * Increment the current count
     */
    fun incrementCount() {
        currentCount++
        isCompleted = isFullyCompleted()
    }
    
    /**
     * Reset count for a new day
     */
    fun resetForNewDay() {
        currentCount = 0
        isCompleted = false
    }
}
