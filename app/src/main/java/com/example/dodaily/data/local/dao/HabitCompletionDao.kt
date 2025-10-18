package com.example.dodaily.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.dodaily.model.HabitCompletion

@Dao
interface HabitCompletionDao {
    @Query("SELECT * FROM habit_completions WHERE dateKey = :dateKey")
    fun getForDate(dateKey: String): List<HabitCompletion>

    @Query("SELECT * FROM habit_completions WHERE habitId = :habitId AND dateKey = :dateKey LIMIT 1")
    fun getForHabitAndDate(habitId: String, dateKey: String): HabitCompletion?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(completion: HabitCompletion)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(completions: List<HabitCompletion>)

    @Update
    fun update(completion: HabitCompletion)

    @Query("DELETE FROM habit_completions WHERE habitId = :habitId AND dateKey = :dateKey")
    fun deleteByHabitAndDate(habitId: String, dateKey: String)

    @Query("DELETE FROM habit_completions")
    fun deleteAll()
}
