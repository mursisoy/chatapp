package es.unizar.mii.tmdad.chatapp.dto

import es.unizar.mii.tmdad.chatapp.dao.ChatRoomType

class NewChatRequest (
    val type: ChatRoomType,
    val contacts: List<String>,
    val name: String?
){
    override fun toString(): String {
        return "{type: ${type.toString()}, contacts: [${contacts.joinToString(",")}]}"
    }
}