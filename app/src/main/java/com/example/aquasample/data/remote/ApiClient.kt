package com.example.aquasample.data.remote

import java.util.concurrent.TimeUnit
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

/**
 * Cliente Retrofit de la app.
 *
 * La URL se toma de [ServerConfig]: por defecto `127.0.0.1` para el emulador o
 * un teléfono por USB con `adb reverse tcp:8080 tcp:8080`. En un teléfono por
 * WiFi hay que configurar la IP del PC (por ejemplo `http://192.168.1.50:8080/`)
 * o una URL pública desde Perfil → Servidor.
 */
object ApiClient {

    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(20, TimeUnit.SECONDS)
        .writeTimeout(20, TimeUnit.SECONDS)
        .build()

    @Volatile
    private var cachedUrl: String? = null

    @Volatile
    private var cachedApi: AquaSampleApi? = null

    val api: AquaSampleApi
        get() = synchronized(this) {
            val url = ServerConfig.baseUrl
            if (cachedApi == null || cachedUrl != url) {
                cachedApi = Retrofit.Builder()
                    .baseUrl(url)
                    .client(httpClient)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build()
                    .create(AquaSampleApi::class.java)
                cachedUrl = url
            }
            cachedApi!!
        }
}
