package com.example.aquasample.data.remote

import com.example.aquasample.data.remote.model.MarineForecastResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface AquaSampleApi {

    @GET("v1/marine")
    suspend fun getMarineForecast(
        @Query("latitude") latitude: Double,
        @Query("longitude") longitude: Double,
        @Query("current") current: String = "wave_height,sea_surface_temperature,wind_speed_10m"
    ): MarineForecastResponse
}
