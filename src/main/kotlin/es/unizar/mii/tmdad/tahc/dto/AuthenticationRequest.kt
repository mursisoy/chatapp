package es.unizar.mii.tmdad.tahc.dto

data class AuthenticationRequest(
    val username: String,
    val password: String,
)