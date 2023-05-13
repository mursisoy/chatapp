package es.unizar.mii.tmdad.chatapp.dto

import es.unizar.mii.tmdad.chatapp.dao.ChatRoomType
import java.util.Vector

data class UpdateConversationContactsRequest (
    val id: String,
    val contacts: Vector<String>
)