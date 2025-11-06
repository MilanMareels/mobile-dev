package edu.ap.opdracht.data.model

data class User (
    val uid: String = "",
    val displayName: String? = null,
    val email: String? = null
){
    constructor() : this("", "", "")
}