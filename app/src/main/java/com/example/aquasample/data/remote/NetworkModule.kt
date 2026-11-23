package com.example.aquasample.data.remote

import com.example.aquasample.data.remote.model.MarineForecastResponse
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object NetworkModule {

    private const val BASE_URL = "https://marine-api.open-meteo.com/"

    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val aquaSampleApi: AquaSampleApi = retrofit.create(AquaSampleApi::class.java)

    private val windRetrofit = Retrofit.Builder()
        .baseUrl("https://api.open-meteo.com/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val windApi: WindApi = windRetrofit.create(WindApi::class.java)

    suspend fun fetchMarineForecast(latitude: Double, longitude: Double): MarineForecastResponse =
        withContext(Dispatchers.IO) { aquaSampleApi.getMarineForecast(latitude, longitude) }
}
