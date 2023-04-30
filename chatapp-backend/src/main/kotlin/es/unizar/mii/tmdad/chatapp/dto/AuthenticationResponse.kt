package es.unizar.mii.tmdad.chatapp.dto

data class AuthenticationResponse (
    val accessToken: String,
    val expiresAt: Long,
    val type: String
)