package com.example.aquasample.data.remote.model

import com.google.gson.annotations.SerializedName

data class WindCurrent(
    @SerializedName("wind_speed_10m") val windSpeed10m: Double? = null
)

data class WindResponse(
    @SerializedName("current") val current: WindCurrent? = null
)
