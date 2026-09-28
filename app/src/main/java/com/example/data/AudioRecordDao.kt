package com.example.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface AudioRecordDao {
    @Query("SELECT * FROM audio_records ORDER BY timestamp DESC")
    fun getAllRecords(): Flow<List<AudioEnhancementRecord>>

    @Query("SELECT * FROM audio_records WHERE status = :status ORDER BY timestamp DESC")
    fun getRecordsByStatus(status: String): Flow<List<AudioEnhancementRecord>>

    @Query("SELECT * FROM audio_records WHERE id = :id LIMIT 1")
    suspend fun getRecordById(id: Long): AudioEnhancementRecord?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecord(record: AudioEnhancementRecord): Long

    @Update
    suspend fun updateRecord(record: AudioEnhancementRecord)

    @Delete
    suspend fun deleteRecord(record: AudioEnhancementRecord)

    @Query("DELETE FROM audio_records WHERE id = :id")
    suspend fun deleteRecordById(id: Long)

    @Query("DELETE FROM audio_records")
    suspend fun clearAll()
}
