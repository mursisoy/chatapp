package es.unizar.mii.tmdad.chatapp.dto

import es.unizar.mii.tmdad.chatapp.dao.ChatRoomType
import es.unizar.mii.tmdad.chatapp.dao.UserEntity
import java.util.*

class ChatResponse (
    val id: String,
    val owner: String,
    val type: ChatRoomType,
    val contacts: List<String>
){
    override fun toString(): String {
        return "{type: ${type.toString()}, contacts: [${contacts.joinToString(",")}]}"
    }
}