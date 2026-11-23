package com.example.aquasample.data.repository

import com.example.aquasample.data.remote.NetworkModule
import com.example.aquasample.data.remote.model.MarineForecastResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class MarineRepository {

    private val api = NetworkModule.aquaSampleApi

    suspend fun getMarineConditions(latitude: Double, longitude: Double): MarineForecastResponse? =
        withContext(Dispatchers.IO) { api.getMarineForecast(latitude, longitude) }
}
