package es.unizar.mii.tmdad.chatapp.repository

import es.unizar.mii.tmdad.chatapp.dao.ChatMessage
import jakarta.transaction.Transactional
import org.springframework.data.jpa.repository.JpaRepository
import java.util.*

interface MessageRepository: JpaRepository<ChatMessage, Int> {

    @Transactional
    fun deleteByTo(to: UUID)

    fun findByTo(to: UUID): List<ChatMessage>
}