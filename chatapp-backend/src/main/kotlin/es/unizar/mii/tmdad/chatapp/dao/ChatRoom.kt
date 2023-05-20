package es.unizar.mii.tmdad.chatapp.dao

import java.util.*

//@Entity
class ChatRoom (
//    @Id
//    @GeneratedValue(strategy = GenerationType.UUID)
    val id: UUID = UUID.randomUUID(),
    val owner: UUID?,
//    @Transient
    val contacts: Set<UUID>,
    val name: String?,
    val type: String,
)