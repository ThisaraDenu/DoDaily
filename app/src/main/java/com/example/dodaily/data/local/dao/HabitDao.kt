package com.example.dodaily.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.dodaily.model.Habit

@Dao
interface HabitDao {
    @Query("SELECT * FROM habits")
    fun getAll(): List<Habit>

    @Query("SELECT * FROM habits WHERE id = :id")
    fun getById(id: String): Habit?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(habit: Habit)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(habits: List<Habit>)

    @Update
    fun update(habit: Habit)

    @Delete
    fun delete(habit: Habit)

    @Query("DELETE FROM habits WHERE id = :habitId")
    fun deleteById(habitId: String)

    @Query("DELETE FROM habits")
    fun deleteAll()
}
