package com.example.dodaily.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable

@Entity(tableName = "hydration_schedules")
data class HydrationSchedule(
    @PrimaryKey val id: String,
    val time: String,
    val description: String,
    var isCompleted: Boolean
) : Serializable
