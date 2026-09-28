package com.example.data

import kotlinx.coroutines.flow.Flow

class AudioRecordRepository(private val dao: AudioRecordDao) {
    val allRecords: Flow<List<AudioEnhancementRecord>> = dao.getAllRecords()

    fun getRecordsByStatus(status: String): Flow<List<AudioEnhancementRecord>> =
        dao.getRecordsByStatus(status)

    suspend fun getRecordById(id: Long): AudioEnhancementRecord? = dao.getRecordById(id)

    suspend fun insert(record: AudioEnhancementRecord): Long = dao.insertRecord(record)

    suspend fun update(record: AudioEnhancementRecord) = dao.updateRecord(record)

    suspend fun delete(record: AudioEnhancementRecord) = dao.deleteRecord(record)

    suspend fun deleteById(id: Long) = dao.deleteRecordById(id)

    suspend fun clearAll() = dao.clearAll()
}
