package com.example.aquasample.data.repository

import com.example.aquasample.data.local.dao.MuestraDao
import com.example.aquasample.data.local.entity.MuestraConDetalle
import com.example.aquasample.data.local.entity.MuestraEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext

class MuestraRepository(private val muestraDao: MuestraDao) {

    val allMuestras: Flow<List<MuestraEntity>> = muestraDao.getAllSamples().flowOn(Dispatchers.IO)
    val allMuestrasConDetalle: Flow<List<MuestraConDetalle>> = muestraDao.getAllMuestrasConDetalle().flowOn(Dispatchers.IO)

    fun getMuestrasFiltradas(filtroText: String?, filtroEstado: String?): Flow<List<MuestraConDetalle>> {
        return muestraDao.getMuestrasFiltradas(filtroText, filtroEstado).flowOn(Dispatchers.IO)
    }

    suspend fun insertSample(muestra: MuestraEntity) = withContext(Dispatchers.IO) {
        muestraDao.insertSample(muestra)
    }

    suspend fun updateSample(muestra: MuestraEntity) = withContext(Dispatchers.IO) {
        muestraDao.updateSample(muestra)
    }

    suspend fun deleteSample(muestra: MuestraEntity) = withContext(Dispatchers.IO) {
        muestraDao.deleteSample(muestra)
    }

    suspend fun getSampleById(id: String): MuestraEntity? = withContext(Dispatchers.IO) {
        muestraDao.getSampleById(id)
    }

    suspend fun getLastSample(): MuestraEntity? = withContext(Dispatchers.IO) {
        muestraDao.getLastSample()
    }

    suspend fun updateEstadoSincronizacion(id: String, sincronizado: Boolean, nuevoEstado: String) = withContext(Dispatchers.IO) {
        muestraDao.updateEstadoSincronizacion(id, sincronizado, nuevoEstado)
    }
}
