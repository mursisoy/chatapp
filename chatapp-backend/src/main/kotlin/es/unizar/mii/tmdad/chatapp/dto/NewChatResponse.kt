package es.unizar.mii.tmdad.chatapp.dto

import es.unizar.mii.tmdad.chatapp.dao.ChatRoomType

class NewChatResponse (
    val id: String,
    val type: String,
    val contacts: List<String>,
    val name: String?,
    val owner: String,
){
    override fun toString(): String {
        return "{type: ${type.toString()}, contacts: [${contacts.joinToString(",")}]}"
    }
}