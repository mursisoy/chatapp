package es.unizar.mii.tmdad.chatapp.config

import es.unizar.mii.tmdad.chatapp.dao.UserEntity
import es.unizar.mii.tmdad.chatapp.service.JwtService
import es.unizar.mii.tmdad.chatapp.service.RabbitService
import org.slf4j.LoggerFactory
import org.springframework.messaging.Message
import org.springframework.messaging.MessageChannel
import org.springframework.messaging.simp.SimpMessageHeaderAccessor
import org.springframework.messaging.simp.SimpMessageType
import org.springframework.messaging.simp.stomp.StompHeaderAccessor
import org.springframework.messaging.support.ChannelInterceptor
import org.springframework.messaging.support.MessageBuilder
import org.springframework.messaging.support.MessageHeaderAccessor
import org.springframework.security.authentication.AbstractAuthenticationToken
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContext
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.stereotype.Component
import kotlin.math.log


@Component
class SubscriptionChannelInterceptor(private val rabbitService: RabbitService) : ChannelInterceptor {
    private val logger = LoggerFactory.getLogger(javaClass)
    override fun preSend(message: Message<*>, channel: MessageChannel): Message<*>? {
        val headerAccessor = MessageHeaderAccessor.getAccessor(message) as StompHeaderAccessor
        SimpMessageHeaderAccessor.getSessionAttributes(message.headers)
        if (headerAccessor.messageType?.equals(SimpMessageType.SUBSCRIBE) == true) {
            logger.debug(headerAccessor.destination)
            if ( headerAccessor.destination == "/user/queue/messages" ) {
                var authentication = headerAccessor.user as AbstractAuthenticationToken
                var loggedInUser = authentication.principal as UserEntity
                rabbitService.activeConsumer(loggedInUser.username.toString(), headerAccessor.sessionId!!)
            }

        }
        return MessageBuilder.createMessage(message.payload, headerAccessor.messageHeaders)
    }
}