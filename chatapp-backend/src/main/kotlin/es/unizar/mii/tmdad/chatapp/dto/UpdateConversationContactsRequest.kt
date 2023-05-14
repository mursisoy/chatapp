package es.unizar.mii.tmdad.chatapp.dto

import java.util.UUID
import java.util.Vector

data class UpdateConversationContactsRequest (
    val id: UUID,
    val addContacts: Vector<UUID>,
    val removeContacts: Vector<UUID>
)