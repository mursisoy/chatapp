package es.unizar.mii.tmdad.chatapp.config

import com.rabbitmq.client.AlreadyClosedException
import es.unizar.mii.tmdad.chatapp.dao.UserEntity
import es.unizar.mii.tmdad.chatapp.service.RabbitService
import org.slf4j.LoggerFactory
import org.springframework.messaging.Message
import org.springframework.messaging.MessageChannel
import org.springframework.messaging.MessagingException
import org.springframework.messaging.simp.SimpMessageHeaderAccessor
import org.springframework.messaging.simp.SimpMessageType
import org.springframework.messaging.simp.stomp.StompHeaderAccessor
import org.springframework.messaging.support.ChannelInterceptor
import org.springframework.messaging.support.MessageBuilder
import org.springframework.messaging.support.MessageHeaderAccessor
import org.springframework.security.authentication.AbstractAuthenticationToken
import org.springframework.stereotype.Component
import java.io.IOException


@Component
class SubscriptionChannelInterceptor(
    private val rabbitService: RabbitService
) : ChannelInterceptor {
    private val logger = LoggerFactory.getLogger(javaClass)
    override fun preSend(message: Message<*>, channel: MessageChannel): Message<*>? {
        val headerAccessor = MessageHeaderAccessor.getAccessor(message) as StompHeaderAccessor
        SimpMessageHeaderAccessor.getSessionAttributes(message.headers)
        if (headerAccessor.messageType?.equals(SimpMessageType.SUBSCRIBE) == true) {
            if ( headerAccessor.destination == "/user/queue/messages" ) {
                val authentication = headerAccessor.user as AbstractAuthenticationToken
                val loggedInUser = authentication.principal as UserEntity
                try {

                    rabbitService.activeConsumer(loggedInUser, headerAccessor.sessionId!!)
                } catch (e: IOException) {
                    logger.debug(e.message)
                    throw MessagingException("Failed to subscribe to chat consumer",)
                } catch (e: AlreadyClosedException) {
                    throw MessagingException("Failed to subscribe to chat consumer")
                }
            }
        }
        return MessageBuilder.createMessage(message.payload, headerAccessor.messageHeaders)
    }
}