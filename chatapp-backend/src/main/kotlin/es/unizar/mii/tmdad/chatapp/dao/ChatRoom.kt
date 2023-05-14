package es.unizar.mii.tmdad.chatapp.dao

import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import java.util.UUID

@Entity
class ChatRoom (
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    val id: UUID = UUID.randomUUID(),
    val owner: UUID?,
    @Transient
    val contacts: Set<UUID>,
    val name: String?,
    val type: ChatRoomType,
)