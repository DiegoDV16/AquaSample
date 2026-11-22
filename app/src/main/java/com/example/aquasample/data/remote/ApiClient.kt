package com.example.aquasample.data.remote

import java.util.concurrent.TimeUnit
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

/**
 * Cliente Retrofit de la app.
 *
 * Se apunta a `127.0.0.1` y se usa `adb reverse tcp:8080 tcp:8080` para que el
 * emulador (o un teléfono por USB) llegue al backend que corre en el PC:
 *
 *     adb reverse tcp:8080 tcp:8080
 *
 * Sin ese túnel hay que usar la IP de la máquina en la red local, por ejemplo
 * `http://192.168.1.50:8080/`, y volver a compilar.
 */
object ApiClient {

    private const val BASE_URL = "http://127.0.0.1:8080/"

    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(20, TimeUnit.SECONDS)
        .writeTimeout(20, TimeUnit.SECONDS)
        .build()

    val api: AquaSampleApi by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(httpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(AquaSampleApi::class.java)
    }
}
