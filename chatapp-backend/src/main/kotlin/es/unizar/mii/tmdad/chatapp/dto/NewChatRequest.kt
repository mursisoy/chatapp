package es.unizar.mii.tmdad.chatapp.dto

import es.unizar.mii.tmdad.chatapp.dao.ChatRoomType
import java.util.*

class NewChatRequest (
    val type: ChatRoomType,
    val contacts: List<UUID>,
    val name: String?
){
    override fun toString(): String {
        return "{type: ${type.toString()}, contacts: [${contacts.joinToString(",")}]}"
    }
}