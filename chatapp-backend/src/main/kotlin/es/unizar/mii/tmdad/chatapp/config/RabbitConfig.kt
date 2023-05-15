package es.unizar.mii.tmdad.chatapp.config

import com.rabbitmq.client.Channel
import com.rabbitmq.client.ConnectionFactory
import com.rabbitmq.http.client.Client
import es.unizar.mii.tmdad.chatapp.dao.ChatRoom
import es.unizar.mii.tmdad.chatapp.dao.ChatRoomType
import es.unizar.mii.tmdad.chatapp.service.ChatRoomService
import es.unizar.mii.tmdad.chatapp.service.RabbitNamingService
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.net.ConnectException

@Configuration
class RabbitConfig (
    private val rns: RabbitNamingService,
    private val chatRoomService: ChatRoomService
){
    private val logger = LoggerFactory.getLogger(javaClass)

    @Bean
    fun channel() : Channel {
        val factory = ConnectionFactory()
        // "guest"/"guest" by default, limited to localhost connections
        // "guest"/"guest" by default, limited to localhost connections
        //factory.setUsername(userName) -> guest
        //factory.setPassword(password) -> guest
        //factory.setVirtualHost(virtualHost) -> /
        //factory.username = "guest"
        //factory.password = "guest"
        //factory.host = "rabbitmq" //-> localhost
        //factory.setPort(portNumber) -> 5672
        factory.isAutomaticRecoveryEnabled = true
        factory.setUri("amqps://zhscdaby:iqbwJ92kaHjF7QjpvM2zfP5Lh50zF9GX@rat.rmq2.cloudamqp.com/zhscdaby")



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
                val cliente = Client("https://zhscdaby:iqbwJ92kaHjF7QjpvM2zfP5Lh50zF9GX@rat.rmq2.cloudamqp.com/api")

                return cliente
            } catch (e: ConnectException) {
                Thread.sleep(5000)
                // apply retry logic
            }
        }
    }
}