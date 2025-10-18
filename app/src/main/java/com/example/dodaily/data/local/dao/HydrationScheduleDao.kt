package com.example.dodaily.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.dodaily.model.HydrationSchedule

@Dao
interface HydrationScheduleDao {
    @Query("SELECT * FROM hydration_schedules")
    fun getAll(): List<HydrationSchedule>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(schedule: HydrationSchedule)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(schedules: List<HydrationSchedule>)

    @Update
    fun update(schedule: HydrationSchedule)

    @Delete
    fun delete(schedule: HydrationSchedule)

    @Query("DELETE FROM hydration_schedules WHERE id = :id")
    fun deleteById(id: String)

    @Query("DELETE FROM hydration_schedules")
    fun deleteAll()
}
