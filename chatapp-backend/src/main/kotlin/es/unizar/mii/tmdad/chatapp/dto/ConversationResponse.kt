package es.unizar.mii.tmdad.chatapp.dto

import es.unizar.mii.tmdad.chatapp.dao.ChatRoomType

data class ConversationResponse (
    val id: String,
    val owner: String,
    val name: String?,
    val type: ChatRoomType,
    val contacts: List<ContactInfoResponse>
){
    override fun toString(): String {
        return "{type: ${type.toString()}, contacts: [${contacts.joinToString(",")}]}"
    }
}