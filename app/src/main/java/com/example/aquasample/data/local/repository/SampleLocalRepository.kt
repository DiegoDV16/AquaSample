package com.example.aquasample.data.local.repository

import com.example.aquasample.data.local.dao.MuestraDao
import com.example.aquasample.data.local.entity.MuestraEntity
import com.example.aquasample.data.local.entity.SampleEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext

class SampleLocalRepository(private val dao: MuestraDao) {
    val allSamples: Flow<List<MuestraEntity>> = dao.getAllSamples().flowOn(Dispatchers.IO)

    suspend fun insertSample(sample: SampleEntity) = withContext(Dispatchers.IO) {
        dao.insertSample(sample)
    }

    suspend fun getLastSample(): SampleEntity? = withContext(Dispatchers.IO) {
        dao.getLastSample()
    }
}
