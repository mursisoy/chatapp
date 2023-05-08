package es.unizar.mii.tmdad.chatapp.dto

import java.util.UUID

data class ContactInfo (
    val id: UUID,
    val email: String,
    val firstName: String,
    val lastName: String,
)