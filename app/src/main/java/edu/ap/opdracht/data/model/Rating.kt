package edu.ap.opdracht.data.model

data class Rating(
    val value: Double = 0.0, // double zodat we bv. 4.5 sterren kunnen geven
    val ratedByUid: String = ""
)
