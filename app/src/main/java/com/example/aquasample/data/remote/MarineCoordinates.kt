package com.example.aquasample.data.remote

/** Coordenadas aproximadas de los centros de cultivo de demostración. */
object MarineCoordinates {

    fun forCentro(centroId: Long?): Pair<Double, Double> = when (centroId) {
        1L -> -42.38 to -73.65 // Dalcahue
        2L -> -42.60 to -73.65 // Lemuy / Puqueldón
        3L -> -41.87 to -73.83 // Ancud / Castro
        else -> -42.48 to -73.76
    }
}
