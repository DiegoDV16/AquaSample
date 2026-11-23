package com.example.aquasample.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.aquasample.data.local.entity.UsuarioConRol
import com.example.aquasample.data.local.entity.UsuarioEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UsuarioDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UsuarioEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUsers(users: List<UsuarioEntity>): List<Long>

    @Update
    suspend fun updateUser(user: UsuarioEntity): Int

    @Delete
    suspend fun deleteUser(user: UsuarioEntity): Int

    @Query("DELETE FROM usuarios WHERE id = :userId")
    suspend fun deleteUserById(userId: String): Int

    @Query("SELECT * FROM usuarios WHERE LOWER(email) = LOWER(:email) AND passwordHash = :passwordHash AND estado = 'ACTIVO' LIMIT 1")
    suspend fun login(email: String, passwordHash: String): UsuarioEntity?

    @Query("SELECT * FROM usuarios WHERE LOWER(email) = LOWER(:email) LIMIT 1")
    suspend fun getByEmail(email: String): UsuarioEntity?

    @Query("SELECT * FROM usuarios WHERE id = :id LIMIT 1")
    suspend fun getById(id: String): UsuarioEntity?

    @Query("UPDATE usuarios SET estado = :nuevoEstado WHERE id = :userId")
    suspend fun updateEstado(userId: String, nuevoEstado: String): Int

    @Query("SELECT * FROM usuarios ORDER BY nombres ASC")
    fun getAllUsers(): Flow<List<UsuarioEntity>>

    @Transaction
    @Query("SELECT * FROM usuarios ORDER BY nombres ASC")
    fun getAllUsersWithRole(): Flow<List<UsuarioConRol>>
}
