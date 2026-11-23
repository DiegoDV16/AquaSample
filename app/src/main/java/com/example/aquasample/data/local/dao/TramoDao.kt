package com.example.aquasample.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.aquasample.data.local.entity.TramoEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TramoDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTramo(tramo: TramoEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTramos(tramos: List<TramoEntity>): List<Long>

    @Query("SELECT * FROM tramos ORDER BY tren ASC, linea ASC")
    fun getAllTramos(): Flow<List<TramoEntity>>

    @Query("SELECT * FROM tramos ORDER BY id ASC")
    suspend fun getAllTramosOnce(): List<TramoEntity>

    @Query("SELECT * FROM tramos WHERE estado = 'ACTIVO' ORDER BY tren ASC, linea ASC")
    fun getTramosActivos(): Flow<List<TramoEntity>>

    @Query("SELECT * FROM tramos WHERE centroId = :centroId AND estado = 'ACTIVO' ORDER BY linea ASC")
    fun getTramosActivosPorCentro(centroId: Long): Flow<List<TramoEntity>>

    @Query("SELECT * FROM tramos WHERE id = :id LIMIT 1")
    suspend fun getTramoById(id: Long): TramoEntity?

    @Query("DELETE FROM tramos WHERE id = :id")
    suspend fun deleteTramoById(id: Long)
}
