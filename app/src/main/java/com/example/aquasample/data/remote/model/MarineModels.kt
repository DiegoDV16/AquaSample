package com.example.aquasample.data.remote.model

import com.google.gson.annotations.SerializedName

data class MarineCurrent(
    @SerializedName("time") val time: String? = null,
    @SerializedName("wave_height") val waveHeight: Double? = null,
    @SerializedName("sea_surface_temperature") val seaSurfaceTemperature: Double? = null,
    @SerializedName("wind_speed_10m") val windSpeed10m: Double? = null
)

data class MarineForecastResponse(
    @SerializedName("current") val current: MarineCurrent? = null,
    @SerializedName("utc_offset") val utcOffset: Int? = null,
    @SerializedName("timezone") val timezone: String? = null
)
