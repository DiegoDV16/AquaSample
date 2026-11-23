package com.example.aquasample.data.remote

import com.example.aquasample.data.remote.model.MarineForecastResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

/** Endpoints del backend Spring Boot (ver backend/README.md). */
interface AquaSampleApi {

    @GET("api/muestras")
    suspend fun list(): List<MuestraDto>

    @POST("api/muestras")
    suspend fun create(@Body muestra: MuestraDto): Response<MuestraDto>

    @PUT("api/muestras/{id}")
    suspend fun update(
        @Path("id") id: String,
        @Body muestra: MuestraDto
    ): Response<MuestraDto>

    @PATCH("api/muestras/{id}/estado")
    suspend fun updateEstado(
        @Path("id") id: String,
        @Query("estado") estado: String
    ): Response<MuestraDto>

    @GET("v1/marine")
    suspend fun getMarineForecast(
        @Query("latitude") latitude: Double,
        @Query("longitude") longitude: Double,
        @Query("current") current: String = "wave_height,sea_surface_temperature,wind_speed_10m"
    ): MarineForecastResponse
}
