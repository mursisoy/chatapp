package es.unizar.mii.tmdad.chatapp.repository

import es.unizar.mii.tmdad.chatapp.dao.ChatMessage
import org.springframework.data.jpa.repository.JpaRepository

interface MessageRepository: JpaRepository<ChatMessage, Int> {
}