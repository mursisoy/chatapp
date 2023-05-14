package es.unizar.mii.tmdad.chatapp.config

import com.fasterxml.jackson.databind.ObjectMapper
import com.rabbitmq.client.AMQP
import com.rabbitmq.client.AlreadyClosedException
import com.rabbitmq.client.DefaultConsumer
import com.rabbitmq.client.Envelope
import es.unizar.mii.tmdad.chatapp.dao.ChatMessage
import es.unizar.mii.tmdad.chatapp.dao.UserEntity
import es.unizar.mii.tmdad.chatapp.service.RabbitNamingService
import es.unizar.mii.tmdad.chatapp.service.RabbitService
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Lazy
import org.springframework.messaging.Message
import org.springframework.messaging.MessageChannel
import org.springframework.messaging.MessagingException
import org.springframework.messaging.simp.SimpMessageHeaderAccessor
import org.springframework.messaging.simp.SimpMessageSendingOperations
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
    private val rabbitService: RabbitService,
    private val rns: RabbitNamingService
) : ChannelInterceptor {

    private val logger = LoggerFactory.getLogger(javaClass)

    @Lazy
    @Autowired
    private lateinit var simpMessageSendingOperations: SimpMessageSendingOperations

    override fun preSend(message: Message<*>, channel: MessageChannel): Message<*>? {
        val headerAccessor = MessageHeaderAccessor.getAccessor(message) as StompHeaderAccessor
        SimpMessageHeaderAccessor.getSessionAttributes(message.headers)
        if (headerAccessor.messageType?.equals(SimpMessageType.SUBSCRIBE) == true) {
            if ( headerAccessor.destination == "/user/queue/messages" ) {
                val authentication = headerAccessor.user as AbstractAuthenticationToken
                val loggedInUser = authentication.principal as UserEntity
                try {
                    activeConsumer(loggedInUser, headerAccessor.sessionId!!)
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
    fun activeConsumer(user: UserEntity, sessionId: String){
        val queueName=rns.getUserQueueName(user.id)
        val rabbitChannel = rabbitService.getChannel()
        val consumerTag = rabbitChannel.basicConsume(queueName, false, sessionId,
            object: DefaultConsumer(rabbitChannel) {
                @Throws(IOException::class)
                override fun handleDelivery(
                    consumerTag: String?,
                    envelope: Envelope,
                    properties: AMQP.BasicProperties,
                    body: ByteArray?
                ) {
                    val routingKey = envelope.routingKey
                    val contentType = properties.contentType
                    val deliveryTag = envelope.deliveryTag
                    // (process the message components here ...)
                    channel.basicAck(deliveryTag, false)
                    val mapper = ObjectMapper()
                    simpMessageSendingOperations.convertAndSendToUser(user.username,"/queue/messages",mapper.readValue(body, ChatMessage::class.java))
                }
            })
    }
}