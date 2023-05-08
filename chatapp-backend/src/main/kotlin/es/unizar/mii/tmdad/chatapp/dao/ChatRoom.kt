package es.unizar.mii.tmdad.chatapp.dao

import java.util.UUID

class ChatRoom (
    val id: UUID = UUID.randomUUID(),
    val owner: UserEntity,
    val contacts: Set<UserEntity>
)