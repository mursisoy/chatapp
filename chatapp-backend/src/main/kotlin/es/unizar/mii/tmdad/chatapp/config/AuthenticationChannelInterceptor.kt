package es.unizar.mii.tmdad.chatapp.config

import es.unizar.mii.tmdad.chatapp.service.JwtService
import org.slf4j.LoggerFactory
import org.springframework.messaging.Message
import org.springframework.messaging.MessageChannel
import org.springframework.messaging.simp.SimpMessageHeaderAccessor
import org.springframework.messaging.simp.SimpMessageType
import org.springframework.messaging.simp.stomp.StompHeaderAccessor
import org.springframework.messaging.support.ChannelInterceptor
import org.springframework.messaging.support.MessageBuilder
import org.springframework.messaging.support.MessageHeaderAccessor
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.stereotype.Component


@Component
class AuthenticationChannelInterceptor(private val jwtService: JwtService) : ChannelInterceptor {
    private val logger = LoggerFactory.getLogger(javaClass)
    override fun preSend(message: Message<*>, channel: MessageChannel): Message<*>? {
        val headerAccessor = MessageHeaderAccessor.getAccessor(message) as StompHeaderAccessor
        SimpMessageHeaderAccessor.getSessionAttributes(message.headers)
        if (headerAccessor.messageType?.equals(SimpMessageType.CONNECT) == true) {
            val authHeader = headerAccessor.getNativeHeader("Authorization")?.get(0)
            logger.info(authHeader)
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return message
            }
            val jwt: String = authHeader.substring(7)
            val username = jwtService.extractSubject(jwt)
            val userDetails: UserDetails? = jwtService.extractUser(jwt)
            if (userDetails != null && username!!.isNotEmpty()) {
                if (jwtService.isTokenValid(jwt, userDetails)) {
                    val authToken = UsernamePasswordAuthenticationToken(
                        userDetails,
                        null,
                        userDetails.authorities,
                    )
                    headerAccessor.user =  authToken
                }
            }
        }
        return MessageBuilder.createMessage(message.payload, headerAccessor.messageHeaders)

    }
}