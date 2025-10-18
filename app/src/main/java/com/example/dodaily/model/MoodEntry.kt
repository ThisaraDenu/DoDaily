package com.example.dodaily.model

import java.io.Serializable
import java.util.Date

/**
 * Data class representing a mood journal entry
 * @param id Unique identifier for the mood entry
 * @param emoji Emoji representing the mood
 * @param note Optional text note about the mood
 * @param dateTime When the mood was logged
 * @param moodLevel Numeric representation of mood (1-5 scale)
 */
data class MoodEntry(
    val id: String = "",
    val emoji: String = "😊",
    val note: String = "",
    val dateTime: Date = Date(),
    val moodLevel: Int = 3 // 1 = Angry, 2 = Sad, 3 = Neutral, 4 = Excited, 5 = Happy
) : Serializable {
    /**
     * Get mood description based on level
     */
    fun getMoodDescription(): String {
        return when (moodLevel) {
            1 -> "Angry"
            2 -> "Sad"
            3 -> "Neutral"
            4 -> "Excited"
            5 -> "Happy"
            else -> "Unknown"
        }
    }
    
    /**
     * Get mood color based on level
     */
    fun getMoodColor(): String {
        return when (moodLevel) {
            1 -> "#FF6B6B" // Red (Angry)
            2 -> "#FFB347" // Orange (Sad)
            3 -> "#FFD93D" // Yellow (Neutral)
            4 -> "#6BCF7F" // Light Green (Excited)
            5 -> "#4ECDC4" // Teal (Happy)
            else -> "#CCCCCC" // Gray
        }
    }
}
