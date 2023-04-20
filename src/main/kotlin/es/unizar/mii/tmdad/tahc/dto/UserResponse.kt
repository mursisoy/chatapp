package es.unizar.mii.tmdad.tahc.dto

import java.util.UUID


data class UserResponse (
    val id: UUID,
    val username: String,
    val roles: List<String>
)