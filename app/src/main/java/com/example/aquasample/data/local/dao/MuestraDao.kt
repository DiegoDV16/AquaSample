package com.example.aquasample.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.aquasample.data.local.entity.MuestraConDetalle
import com.example.aquasample.data.local.entity.MuestraEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MuestraDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSample(sample: MuestraEntity): Long

    @Update
    suspend fun updateSample(sample: MuestraEntity): Int

    @Delete
    suspend fun deleteSample(sample: MuestraEntity): Int

    @Query("SELECT * FROM muestras WHERE id = :id LIMIT 1")
    suspend fun getSampleById(id: String): MuestraEntity?

    @Query("SELECT * FROM muestras ORDER BY timestamp DESC")
    fun getAllSamples(): Flow<List<MuestraEntity>>

    @Query("""
        SELECT 
            m.*,
            c.nombre AS centroNombre,
            c.codigo AS centroCodigo,
            (u.nombres || ' ' || u.apellidoPaterno || ' ' || u.apellidoMaterno) AS operadorNombreCompleto,
            u.email AS operadorEmail
        FROM muestras m
        LEFT JOIN centros_cultivo c ON m.centroId = c.id
        LEFT JOIN usuarios u ON m.operadorId = u.id
        ORDER BY m.timestamp DESC
    """)
    fun getAllMuestrasConDetalle(): Flow<List<MuestraConDetalle>>

    @Query("""
        SELECT 
            m.*,
            c.nombre AS centroNombre,
            c.codigo AS centroCodigo,
            (u.nombres || ' ' || u.apellidoPaterno || ' ' || u.apellidoMaterno) AS operadorNombreCompleto,
            u.email AS operadorEmail
        FROM muestras m
        LEFT JOIN centros_cultivo c ON m.centroId = c.id
        LEFT JOIN usuarios u ON m.operadorId = u.id
        WHERE (:filtroText IS NULL OR :filtroText = '' OR m.id LIKE '%' || :filtroText || '%' OR m.linea LIKE '%' || :filtroText || '%' OR m.concesion LIKE '%' || :filtroText || '%')
          AND (:filtroEstado IS NULL OR :filtroEstado = '' OR m.estado = :filtroEstado)
        ORDER BY m.timestamp DESC
    """)
    fun getMuestrasFiltradas(filtroText: String?, filtroEstado: String?): Flow<List<MuestraConDetalle>>

    @Query("SELECT * FROM muestras ORDER BY timestamp DESC LIMIT 1")
    suspend fun getLastSample(): MuestraEntity?

    @Query("UPDATE muestras SET sincronizado = :sincronizado, estado = :nuevoEstado WHERE id = :id")
    suspend fun updateEstadoSincronizacion(id: String, sincronizado: Boolean, nuevoEstado: String): Int
}