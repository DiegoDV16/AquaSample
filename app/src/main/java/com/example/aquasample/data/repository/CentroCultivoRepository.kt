package com.example.aquasample.data.repository

import com.example.aquasample.data.local.dao.CentroCultivoDao
import com.example.aquasample.data.local.entity.CentroCultivoEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext

class CentroCultivoRepository(private val centroCultivoDao: CentroCultivoDao) {

    val allCentros: Flow<List<CentroCultivoEntity>> = centroCultivoDao.getAllCentros().flowOn(Dispatchers.IO)
    val centrosActivos: Flow<List<CentroCultivoEntity>> = centroCultivoDao.getAllCentrosActivos().flowOn(Dispatchers.IO)

    suspend fun insertCentro(centro: CentroCultivoEntity): Long = withContext(Dispatchers.IO) {
        centroCultivoDao.insertCentro(centro)
    }

    suspend fun insertCentros(centros: List<CentroCultivoEntity>) = withContext(Dispatchers.IO) {
        centroCultivoDao.insertCentros(centros)
    }

    suspend fun getCentroById(id: Long): CentroCultivoEntity? = withContext(Dispatchers.IO) {
        centroCultivoDao.getCentroById(id)
    }

    suspend fun getCentroByCodigo(codigo: String): CentroCultivoEntity? = withContext(Dispatchers.IO) {
        centroCultivoDao.getCentroByCodigo(codigo)
    }
}
