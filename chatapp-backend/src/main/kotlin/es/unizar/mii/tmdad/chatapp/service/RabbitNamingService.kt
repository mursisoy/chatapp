package es.unizar.mii.tmdad.chatapp.service

import org.springframework.stereotype.Service
import java.util.UUID

@Service
class RabbitNamingService {
    final val BROADCAST_QUEUE_ID = UUID(0L, 0L)

    fun getBroadcastExchangeName(): String {
        return "${BROADCAST_QUEUE_ID}-cx"
    }
    fun getConversationExchangeName(conversationId: UUID): String {
        return "${conversationId}-cx"
    }

    fun getUserExchangeName(userId: UUID): String {
        return "${userId}-ux"
    }

    fun getUserQueueName(userId: UUID): String {
        return "${userId}-uq"
    }
}