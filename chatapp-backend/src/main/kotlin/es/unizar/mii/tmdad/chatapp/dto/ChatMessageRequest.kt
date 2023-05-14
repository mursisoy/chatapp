package es.unizar.mii.tmdad.chatapp.dto

data class ChatMessageRequest (
    var id: String?,
    var draftId: String?,
    var date: Long,
    val from: String,
    val to: String,
    val content: String,
    val media: String?
) {
    override fun toString(): String {
        return "{draftId: ${draftId}, date: ${date.toString()}, from: '$from', to: '$to', content: '$content'}"
    }
}