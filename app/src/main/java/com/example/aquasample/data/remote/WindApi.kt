package com.example.aquasample.data.remote

import com.example.aquasample.data.remote.model.WindResponse
import retrofit2.http.GET
import retrofit2.http.Query

/** API atmosférica de Open-Meteo para complementar el viento que no entrega marine-api. */
interface WindApi {

    @GET("v1/forecast")
    suspend fun getWind(
        @Query("latitude") latitude: Double,
        @Query("longitude") longitude: Double,
        @Query("current") current: String = "wind_speed_10m",
        @Query("wind_speed_unit") windSpeedUnit: String = "ms"
    ): WindResponse
}
