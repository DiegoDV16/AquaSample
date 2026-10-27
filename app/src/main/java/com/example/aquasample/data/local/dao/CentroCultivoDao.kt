package com.example.aquasample.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.aquasample.data.local.entity.CentroCultivoEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CentroCultivoDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCentro(centro: CentroCultivoEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCentros(centros: List<CentroCultivoEntity>): List<Long>

    @Query("SELECT * FROM centros_cultivo ORDER BY nombre ASC")
    fun getAllCentros(): Flow<List<CentroCultivoEntity>>

    @Query("SELECT * FROM centros_cultivo WHERE estado = 'ACTIVO' ORDER BY nombre ASC")
    fun getAllCentrosActivos(): Flow<List<CentroCultivoEntity>>

    @Query("SELECT * FROM centros_cultivo WHERE id = :id LIMIT 1")
    suspend fun getCentroById(id: Long): CentroCultivoEntity?

    @Query("SELECT * FROM centros_cultivo WHERE codigo = :codigo LIMIT 1")
    suspend fun getCentroByCodigo(codigo: String): CentroCultivoEntity?
}