package es.unizar.mii.tmdad.chatapp.dto

import java.util.*

data class ChatMessageRequest (
    var id: String?,
    var draftId: String?,
    var date: Long,
    val to: UUID,
    val content: String,
    val media: ConversationFileUploadResponse?
) {
    override fun toString(): String {
        return "{draftId: ${draftId}, date: ${date.toString()}, to: '$to', content: '$content'}"
    }
}