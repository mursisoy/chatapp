package es.unizar.mii.tmdad.chatapp.dao

import java.util.UUID

class ChatRoom (
    val id: UUID = UUID.randomUUID(),
    val owner: UserEntity? = null,
    val contacts: Set<String>
)