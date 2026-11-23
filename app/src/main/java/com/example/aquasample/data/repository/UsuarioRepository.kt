package com.example.aquasample.data.repository

import com.example.aquasample.data.local.dao.UsuarioDao
import com.example.aquasample.data.local.entity.UsuarioConRol
import com.example.aquasample.data.local.entity.UsuarioEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext

class UsuarioRepository(private val usuarioDao: UsuarioDao) {

    val allUsers: Flow<List<UsuarioEntity>> = usuarioDao.getAllUsers().flowOn(Dispatchers.IO)
    val allUsersWithRole: Flow<List<UsuarioConRol>> = usuarioDao.getAllUsersWithRole().flowOn(Dispatchers.IO)

    suspend fun insertUser(user: UsuarioEntity) = withContext(Dispatchers.IO) {
        usuarioDao.insertUser(user)
    }

    suspend fun insertUsers(users: List<UsuarioEntity>) = withContext(Dispatchers.IO) {
        usuarioDao.insertUsers(users)
    }

    suspend fun updateUser(user: UsuarioEntity) = withContext(Dispatchers.IO) {
        usuarioDao.updateUser(user)
    }

    suspend fun deleteUser(user: UsuarioEntity) = withContext(Dispatchers.IO) {
        usuarioDao.deleteUser(user)
    }

    suspend fun deleteUserById(userId: String) = withContext(Dispatchers.IO) {
        usuarioDao.deleteUserById(userId)
    }

    suspend fun login(email: String, passwordHash: String): UsuarioEntity? = withContext(Dispatchers.IO) {
        usuarioDao.login(email, passwordHash)
    }

    suspend fun getByEmail(email: String): UsuarioEntity? = withContext(Dispatchers.IO) {
        usuarioDao.getByEmail(email)
    }

    suspend fun getById(id: String): UsuarioEntity? = withContext(Dispatchers.IO) {
        usuarioDao.getById(id)
    }

    suspend fun updateEstado(userId: String, nuevoEstado: String) = withContext(Dispatchers.IO) {
        usuarioDao.updateEstado(userId, nuevoEstado)
    }
}