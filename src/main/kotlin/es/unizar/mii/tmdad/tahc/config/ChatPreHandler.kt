package es.unizar.mii.tmdad.tahc.config

import org.slf4j.LoggerFactory
import org.springframework.messaging.Message
import org.springframework.messaging.MessageChannel
import org.springframework.messaging.simp.SimpMessageType
import org.springframework.messaging.simp.stomp.StompHeaderAccessor
import org.springframework.messaging.support.ChannelInterceptor
import org.springframework.messaging.support.MessageBuilder
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.stereotype.Component


@Component
class ChatPreHandler(private val jwtService: JwtService) : ChannelInterceptor {
    private val logger = LoggerFactory.getLogger(javaClass)
    override fun preSend(message: Message<*>, channel: MessageChannel): Message<*>? {
        val headerAccessor = StompHeaderAccessor.wrap(message)
        logger.info(headerAccessor.messageType?.name)
        if (headerAccessor.messageType?.equals(SimpMessageType.CONNECT) == true) {
            val authHeader = headerAccessor.getNativeHeader("Authorization")?.get(0);
            logger.info(authHeader)
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return message
            }
            val jwt: String = authHeader.substring(7)
            val username = jwtService.extractSubject(jwt)
            logger.info(username)
            val userDetails: UserDetails? = jwtService.extractUser(jwt)
            logger.info(userDetails.toString())
            if (userDetails != null && username!!.isNotEmpty() && SecurityContextHolder.getContext().authentication == null) {
//            val userDetails: UserDetails = userDetailService.loadUserByUsername(userEmail)
                if (jwtService.isTokenValid(jwt, userDetails)) {
                    val authToken = UsernamePasswordAuthenticationToken(
                        userDetails,
                        null,
                        userDetails.authorities
                    )
                    headerAccessor.setUser { authToken.principal.toString() }
//                    getAuthentication(
//                        SecurityContextHolder.getContextHolderStrategy()
//                    )
//                    authToken.details = WebAuthenticationDetailsSource().buildDetails(request)
                    SecurityContextHolder.getContext().authentication = authToken

                }
            }
        }
        return MessageBuilder.createMessage(message.payload, headerAccessor.messageHeaders)

    }
}