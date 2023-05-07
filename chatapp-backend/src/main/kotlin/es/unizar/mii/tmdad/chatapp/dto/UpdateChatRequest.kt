package es.unizar.mii.tmdad.chatapp.dto

import java.util.*

class UpdateChatRequest (
    val idSala: String,
    val action: String,
    val usersAffected: Vector<String>,
    val origin: String,
)