package es.unizar.mii.tmdad.chatapp.dto

class NewChatResponse (
    val id: String,
    val type: String,
    val contacts: List<ContactInfoResponse>,
    val name: String?,
    val owner: String,
){
    override fun toString(): String {
        return "{type: ${type}, contacts: [${contacts.joinToString(",")}]}"
    }
}