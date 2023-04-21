package es.unizar.mii.tmdad.tahc.config
import org.apache.catalina.core.ApplicationContext
import org.springframework.context.annotation.Configuration
import org.springframework.messaging.Message
import org.springframework.messaging.simp.SimpMessageType
import org.springframework.messaging.simp.config.ChannelRegistration
import org.springframework.messaging.simp.config.MessageBrokerRegistry
import org.springframework.security.authorization.AuthorizationManager
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.messaging.access.intercept.AuthorizationChannelInterceptor
import org.springframework.security.messaging.access.intercept.MessageMatcherDelegatingAuthorizationManager
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker
import org.springframework.web.socket.config.annotation.StompEndpointRegistry
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer
import org.springframework.web.socket.server.support.DefaultHandshakeHandler


@Configuration
@EnableWebSocketMessageBroker
class WebSocketConfig(private val chatPreHandler: ChatPreHandler): WebSocketMessageBrokerConfigurer {

    private val securityContextHolderStrategy = SecurityContextHolder
        .getContextHolderStrategy()

    override fun registerStompEndpoints(registry: StompEndpointRegistry) {
        registry.addEndpoint("/ws")
            .setAllowedOrigins("*")
            .setHandshakeHandler(DefaultHandshakeHandler())
    }

    override fun configureMessageBroker(registry: MessageBrokerRegistry) {
        registry.enableSimpleBroker("/canuto")
    }


    override fun configureClientInboundChannel(registration: ChannelRegistration) {
        val myAuthorizationRules = messageAuthorizationManager(MessageMatcherDelegatingAuthorizationManager.Builder())
        val authz = AuthorizationChannelInterceptor(myAuthorizationRules)
        registration.interceptors(chatPreHandler,authz)
        super.configureClientInboundChannel(registration);
    }

    fun messageAuthorizationManager(messages: MessageMatcherDelegatingAuthorizationManager.Builder): AuthorizationManager<Message<*>> {
        messages
            .nullDestMatcher().permitAll()
            .simpTypeMatchers(SimpMessageType.CONNECT,SimpMessageType.DISCONNECT, SimpMessageType.OTHER,SimpMessageType.HEARTBEAT,
                SimpMessageType.UNSUBSCRIBE).permitAll()
            .simpSubscribeDestMatchers("/canuto").authenticated()
            .anyMessage().permitAll()
        return messages.build();
    }

}