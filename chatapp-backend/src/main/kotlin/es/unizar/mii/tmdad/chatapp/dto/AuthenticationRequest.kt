package es.unizar.mii.tmdad.chatapp.dto

data class AuthenticationRequest(
    val username: String,
    val password: String,
)