package es.unizar.mii.tmdad.chatapp.dto

import java.util.*

data class ConversationFileUploadResponse(
    val id: UUID = UUID.randomUUID(),
    val name: String? = "",
    val size: Long = 0,
    val type: String? = ""
)