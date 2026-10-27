package com.example.aquasample.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.aquasample.data.local.entity.RoleEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface RoleDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRole(role: RoleEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRoles(roles: List<RoleEntity>): List<Long>

    @Query("SELECT * FROM roles ORDER BY id ASC")
    fun getAllRoles(): Flow<List<RoleEntity>>

    @Query("SELECT * FROM roles WHERE id = :id LIMIT 1")
    suspend fun getRoleById(id: Long): RoleEntity?

    @Query("SELECT * FROM roles WHERE nombre = :nombre LIMIT 1")
    suspend fun getRoleByNombre(nombre: String): RoleEntity?
}
