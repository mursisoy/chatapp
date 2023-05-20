package es.unizar.mii.tmdad.chatapp.dto

data class ConversationResponse (
    val id: String,
    val owner: ContactInfoResponse?,
    val name: String?,
    val type: String,
    val messages: List<ChatMessageResponse>,
    val contacts: List<ContactInfoResponse>
){
    override fun toString(): String {
        return "{type: ${type}, contacts: [${contacts.joinToString(",")}]}"
    }
}