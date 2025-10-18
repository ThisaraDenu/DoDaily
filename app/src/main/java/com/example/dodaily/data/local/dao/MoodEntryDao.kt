package com.example.dodaily.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.dodaily.model.MoodEntry

@Dao
interface MoodEntryDao {
    @Query("SELECT * FROM mood_entries ORDER BY dateTime DESC")
    fun getAll(): List<MoodEntry>

    @Query("SELECT * FROM mood_entries WHERE id = :id")
    fun getById(id: String): MoodEntry?

    @Query("SELECT * FROM mood_entries WHERE dateTime BETWEEN :from AND :to ORDER BY dateTime DESC")
    fun getBetween(from: Long, to: Long): List<MoodEntry>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(entry: MoodEntry)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(entries: List<MoodEntry>)

    @Update
    fun update(entry: MoodEntry)

    @Delete
    fun delete(entry: MoodEntry)

    @Query("DELETE FROM mood_entries")
    fun deleteAll()
}
