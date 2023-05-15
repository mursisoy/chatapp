package es.unizar.mii.tmdad.chatapp.config

import com.rabbitmq.client.Channel
import com.rabbitmq.client.ConnectionFactory
import com.rabbitmq.http.client.Client
import com.rabbitmq.http.client.ClientParameters
import es.unizar.mii.tmdad.chatapp.dao.ChatRoom
import es.unizar.mii.tmdad.chatapp.dao.ChatRoomType
import es.unizar.mii.tmdad.chatapp.service.ChatRoomService
import es.unizar.mii.tmdad.chatapp.service.RabbitNamingService
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.net.ConnectException

@Configuration
class RabbitConfig (
    private val rns: RabbitNamingService,
    private val chatRoomService: ChatRoomService
){
    private val logger = LoggerFactory.getLogger(javaClass)


    @Value("\${spring.rabbitmq-amqp.ssl}")
    private val amqp_ssl: Boolean? = null

    @Value("\${spring.rabbitmq-amqp.host}")
    private val amqp_host: String? = null

    @Value("\${spring.rabbitmq-amqp.port}")
    private val amqp_port: Int? = null

    @Value("\${spring.rabbitmq-amqp.user}")
    private val amqp_user: String? = null

    @Value("\${spring.rabbitmq-amqp.password}")
    private val amqp_password: String? = null

    @Value("\${spring.rabbitmq-amqp.vhost}")
    private val amqp_vhost: String? = ConnectionFactory.DEFAULT_VHOST

    @Value("\${spring.rabbitmq-http.ssl}")
    private val http_ssl: Boolean? = null

    @Value("\${spring.rabbitmq-http.host}")
    private val http_host: String? = null

    @Value("\${spring.rabbitmq-http.port}")
    private val http_port: Int? = null

    @Value("\${spring.rabbitmq-http.user}")
    private val http_user: String? = null

    @Value("\${spring.rabbitmq-http.password}")
    private val http_password: String? = null

    @Value("\${spring.rabbitmq-http.vhost}")
    private val http_vhost: String? = ConnectionFactory.DEFAULT_VHOST

    @Bean
    fun channel() : Channel {
        val factory = ConnectionFactory()
        // "guest"/"guest" by default, limited to localhost connections
        // "guest"/"guest" by default, limited to localhost connections
        if (amqp_port != null) {
            factory.port = amqp_port
        }
        factory.host = amqp_host
        factory.username = amqp_user
        factory.password = amqp_password
        if(amqp_vhost != ConnectionFactory.DEFAULT_VHOST)
            factory.virtualHost = amqp_vhost
        if (amqp_ssl == true) factory.useSslProtocol()
        //factory.host = "rabbitmq" //-> localhost
        //factory.setPort(portNumber) -> 5672
        factory.isAutomaticRecoveryEnabled = true

        while (true) {
            try {
                val conn = factory.newConnection()
                val channel = conn.createChannel()
                try {
                    chatRoomService.save(
                        ChatRoom(
                            id = rns.BROADCAST_QUEUE_ID,
                            type = ChatRoomType.BROADCAST.name,
                            owner = null,
                            contacts = emptySet(),
                            name = "Anuncios"
                        )
                    )
                } catch (e: Exception){
                    logger.error(e.stackTraceToString())
                }
                //crear exchange broadcast
                channel.exchangeDeclare(rns.getBroadcastExchangeName(), "fanout", true, false, false,
                    mutableMapOf(
                        "type" to ChatRoomType.BROADCAST.name,
                        "name" to "Anuncios",
                        "id" to rns.BROADCAST_QUEUE_ID.toString()
                    ) as Map<String, Any>?
                )


                return channel
            } catch (e: ConnectException) {
                Thread.sleep(5000)
                // apply retry logic
            }

        }
    }


    @Bean
    fun cliente() :Client{

        while (true) {
            try {
                val protocol = if (http_ssl == true) "https" else "http"
                val cliente = Client(
                    ClientParameters()
                        .url("${protocol}://${http_host}:${http_port}/api/")
                        .username(http_user)
                        .password(http_password))


                return cliente
            } catch (e: ConnectException) {
                Thread.sleep(5000)
                // apply retry logic
            }
        }
    }
}