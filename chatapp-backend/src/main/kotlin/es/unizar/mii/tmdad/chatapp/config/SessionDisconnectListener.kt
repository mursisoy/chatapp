package es.unizar.mii.tmdad.chatapp.config

import es.unizar.mii.tmdad.chatapp.service.RabbitService
import es.unizar.mii.tmdad.chatapp.service.UserService
import org.springframework.context.event.EventListener
import org.springframework.messaging.simp.SimpMessageHeaderAccessor
import org.springframework.stereotype.Component
import org.springframework.web.socket.messaging.SessionDisconnectEvent

@Component
class HandleWebSocketDisconnectListener(
    private val rabbitService: RabbitService
) {
    @EventListener
    fun bye(event: SessionDisconnectEvent) {

        val headerAccessor: SimpMessageHeaderAccessor = SimpMessageHeaderAccessor.wrap(event.message)
        val id = headerAccessor.sessionId as String
        rabbitService.desactiveConsumer(id)
    }
}

