package es.unizar.mii.tmdad.chatapp.dto

import java.util.*

data class ChatMessageResponse(
    val id: UUID,
    val date: Long,
    val to: UUID,
    val from: String,
    val content: String,
    val media: ConversationFileUploadResponse?
)