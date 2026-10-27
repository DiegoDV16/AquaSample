package com.example.aquasample.data.local.repository

import com.example.aquasample.data.local.dao.SampleDao
import com.example.aquasample.data.local.entity.SampleEntity
import kotlinx.coroutines.flow.Flow

class SampleLocalRepository(private val dao: SampleDao) {
    val allSamples: Flow<List<SampleEntity>> = dao.getAllSamples()

    suspend fun insertSample(sample: SampleEntity) {
        dao.insertSample(sample)
    }

    suspend fun getLastSample(): SampleEntity? {
        return dao.getLastSample()
    }
}
