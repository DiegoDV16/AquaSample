package com.example.aquasample.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.aquasample.data.local.entity.SampleEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SampleDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSample(sample: SampleEntity)

    @Query("SELECT * FROM samples ORDER BY fechaHora DESC")
    fun getAllSamples(): Flow<List<SampleEntity>>

    @Query("SELECT * FROM samples ORDER BY fechaHora DESC LIMIT 1")
    suspend fun getLastSample(): SampleEntity?
}
