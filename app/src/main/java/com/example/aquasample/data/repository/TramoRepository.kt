package com.example.aquasample.data.repository

import com.example.aquasample.data.local.dao.TramoDao
import com.example.aquasample.data.local.entity.TramoEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext

class TramoRepository(private val tramoDao: TramoDao) {

    val allTramos: Flow<List<TramoEntity>> = tramoDao.getAllTramos().flowOn(Dispatchers.IO)
    val tramosActivos: Flow<List<TramoEntity>> = tramoDao.getTramosActivos().flowOn(Dispatchers.IO)

    fun tramosActivosPorCentro(centroId: Long): Flow<List<TramoEntity>> =
        tramoDao.getTramosActivosPorCentro(centroId).flowOn(Dispatchers.IO)

    suspend fun insertTramo(tramo: TramoEntity): Long = withContext(Dispatchers.IO) {
        tramoDao.insertTramo(tramo)
    }

    suspend fun insertTramos(tramos: List<TramoEntity>) = withContext(Dispatchers.IO) {
        tramoDao.insertTramos(tramos)
    }

    suspend fun getTramoById(id: Long): TramoEntity? = withContext(Dispatchers.IO) {
        tramoDao.getTramoById(id)
    }

    suspend fun deleteTramoById(id: Long) = withContext(Dispatchers.IO) {
        tramoDao.deleteTramoById(id)
    }
}
