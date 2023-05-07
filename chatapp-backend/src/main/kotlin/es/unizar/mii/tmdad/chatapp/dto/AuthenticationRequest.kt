package es.unizar.mii.tmdad.chatapp.dto

data class AuthenticationRequest(
    val email: String,
    val password: String,
)