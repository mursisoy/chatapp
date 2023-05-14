package es.unizar.mii.tmdad.chatapp.config

import es.unizar.mii.tmdad.chatapp.service.RabbitService
import jakarta.annotation.PreDestroy
import org.slf4j.LoggerFactory
import org.springframework.context.event.EventListener
import org.springframework.messaging.simp.SimpMessageHeaderAccessor
import org.springframework.messaging.simp.user.SimpUserRegistry
import org.springframework.stereotype.Component
import org.springframework.web.socket.messaging.SessionDisconnectEvent


@Component
class HandleWebSocketDisconnectListener(
    private val rabbitService: RabbitService,
    private val simpUserRegistry: SimpUserRegistry
) {

    private val logger = LoggerFactory.getLogger(javaClass)

    @EventListener
    fun bye(event: SessionDisconnectEvent) {

        val headerAccessor: SimpMessageHeaderAccessor = SimpMessageHeaderAccessor.wrap(event.message)
        val id = headerAccessor.sessionId as String
        rabbitService.desactiveConsumer(id)
    }

    @PreDestroy
    fun destroy() {
        for (user in simpUserRegistry.users){
            for (session in user.sessions) {
                rabbitService.desactiveConsumer(session.id)
            }
        }
        logger.debug(
            "Callback triggered - @PreDestroy."
        )

    }

}

