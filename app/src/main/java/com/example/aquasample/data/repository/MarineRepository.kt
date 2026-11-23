package com.example.aquasample.data.repository

import com.example.aquasample.data.remote.NetworkModule
import com.example.aquasample.data.remote.model.MarineForecastResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class MarineRepository {

    private val marineApi = NetworkModule.aquaSampleApi
    private val windApi = NetworkModule.windApi

    suspend fun getMarineConditions(latitude: Double, longitude: Double): MarineForecastResponse? =
        withContext(Dispatchers.IO) {
            val marine = runCatching {
                marineApi.getMarineForecast(
                    latitude = latitude,
                    longitude = longitude,
                    current = "wave_height,sea_surface_temperature"
                )
            }.getOrNull() ?: return@withContext null

            val wind = runCatching {
                windApi.getWind(latitude, longitude).current?.windSpeed10m
            }.getOrNull()

            marine.copy(current = marine.current?.copy(windSpeed10m = wind))
        }
}
