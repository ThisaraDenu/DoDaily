package com.example.dodaily.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.dodaily.data.local.dao.HabitDao
import com.example.dodaily.data.local.dao.HabitCompletionDao
import com.example.dodaily.data.local.dao.HydrationScheduleDao
import com.example.dodaily.data.local.dao.MoodEntryDao
import com.example.dodaily.model.Habit
import com.example.dodaily.model.HabitCompletion
import com.example.dodaily.model.HydrationSchedule
import com.example.dodaily.model.MoodEntry

@Database(
    entities = [Habit::class, HabitCompletion::class, MoodEntry::class, HydrationSchedule::class],
    version = 1,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun habitDao(): HabitDao
    abstract fun habitCompletionDao(): HabitCompletionDao
    abstract fun moodEntryDao(): MoodEntryDao
    abstract fun hydrationScheduleDao(): HydrationScheduleDao
}
