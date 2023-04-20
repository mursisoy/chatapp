package es.unizar.mii.tmdad.tahc.dto

data class AuthenticationResponse (
    val accessToken: String,
    val expiresAt: Long,
    val type: String
)