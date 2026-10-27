package com.example.aquasample.data.repository

import com.example.aquasample.data.local.dao.RoleDao
import com.example.aquasample.data.local.entity.RoleEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext

class RoleRepository(private val roleDao: RoleDao) {

    val allRoles: Flow<List<RoleEntity>> = roleDao.getAllRoles().flowOn(Dispatchers.IO)

    suspend fun insertRole(role: RoleEntity): Long = withContext(Dispatchers.IO) {
        roleDao.insertRole(role)
    }

    suspend fun insertRoles(roles: List<RoleEntity>) = withContext(Dispatchers.IO) {
        roleDao.insertRoles(roles)
    }

    suspend fun getRoleById(id: Long): RoleEntity? = withContext(Dispatchers.IO) {
        roleDao.getRoleById(id)
    }

    suspend fun getRoleByNombre(nombre: String): RoleEntity? = withContext(Dispatchers.IO) {
        roleDao.getRoleByNombre(nombre)
    }
}
