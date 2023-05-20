package es.unizar.mii.tmdad.chatapp.dto

data class ChatMessageResponse(
    var id: String,
    var date: Long,
    val to: String,
    val content: String,
    val media: ConversationFileUploadResponse?
)