package es.unizar.mii.tmdad.chatapp.config

import org.springframework.context.ApplicationContext
import org.springframework.context.annotation.Configuration
import org.springframework.messaging.Message
import org.springframework.messaging.handler.invocation.HandlerMethodArgumentResolver
import org.springframework.messaging.simp.config.ChannelRegistration
import org.springframework.messaging.simp.config.MessageBrokerRegistry
import org.springframework.security.authorization.AuthorizationManager
import org.springframework.security.authorization.SpringAuthorizationEventPublisher
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.core.context.SecurityContextHolderStrategy
import org.springframework.security.messaging.access.intercept.AuthorizationChannelInterceptor
import org.springframework.security.messaging.access.intercept.MessageMatcherDelegatingAuthorizationManager
import org.springframework.security.messaging.context.AuthenticationPrincipalArgumentResolver
import org.springframework.security.messaging.context.SecurityContextChannelInterceptor
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker
import org.springframework.web.socket.config.annotation.StompEndpointRegistry
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer
import org.springframework.web.socket.messaging.StompSubProtocolErrorHandler
import org.springframework.web.socket.server.support.DefaultHandshakeHandler


@Configuration
@EnableWebSocketMessageBroker
class WebSocketConfig(
    private val authenticationChannelInterceptor: AuthenticationChannelInterceptor,
    private val subscriptionChannelInterceptor: SubscriptionChannelInterceptor,
    private val applicationContext: ApplicationContext,
): WebSocketMessageBrokerConfigurer{

    val securityContextHolderStrategy: SecurityContextHolderStrategy = SecurityContextHolder
        .getContextHolderStrategy()

    val securityContextChannelInterceptor = SecurityContextChannelInterceptor()

    override fun registerStompEndpoints(registry: StompEndpointRegistry) {
        registry.addEndpoint("/ws")
            .setAllowedOrigins("*")
            .setHandshakeHandler(DefaultHandshakeHandler())
        registry.setErrorHandler(StompSubProtocolErrorHandler())
    }

    override fun configureMessageBroker(registry: MessageBrokerRegistry) {
        registry.setApplicationDestinationPrefixes("/chat")
//        registry.enableSimpleBroker("/queue/messages");
        registry.enableSimpleBroker("/queue")
//        registry.setUserDestinationPrefix("/user");
//        registry.enableSimpleBroker("cmd")
    }

    override fun addArgumentResolvers(argumentResolvers: MutableList<HandlerMethodArgumentResolver?>) {
        val resolver = AuthenticationPrincipalArgumentResolver()
        resolver.setSecurityContextHolderStrategy(securityContextHolderStrategy)
        argumentResolvers.add(resolver)
    }

    override fun configureClientInboundChannel(registration: ChannelRegistration) {
        val manager = messageAuthorizationManager(MessageMatcherDelegatingAuthorizationManager.builder())
        val interceptor = AuthorizationChannelInterceptor(manager)
        interceptor.setAuthorizationEventPublisher(SpringAuthorizationEventPublisher(applicationContext))
        interceptor.setSecurityContextHolderStrategy(securityContextHolderStrategy)
        securityContextChannelInterceptor.setSecurityContextHolderStrategy(securityContextHolderStrategy)
        registration.interceptors(
            authenticationChannelInterceptor,
            securityContextChannelInterceptor,
            interceptor,
            subscriptionChannelInterceptor)
    }


    fun messageAuthorizationManager(messages: MessageMatcherDelegatingAuthorizationManager.Builder): AuthorizationManager<Message<*>> {
        messages
            .simpMessageDestMatchers("/chat/broadcast").hasRole("ADMIN")
//            .simpSubscribeDestMatchers("/queue/**").denyAll()
            .anyMessage().authenticated()
        return messages.build()
    }

}



