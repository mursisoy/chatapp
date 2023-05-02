package es.unizar.mii.tmdad.chatapp.config

import com.rabbitmq.client.Channel
import com.rabbitmq.client.Connection
import com.rabbitmq.client.ConnectionFactory
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class RabbitConfig {

    @Bean
    fun channel() : Channel {
        val factory = ConnectionFactory()
        // "guest"/"guest" by default, limited to localhost connections
        // "guest"/"guest" by default, limited to localhost connections
        //factory.setUsername(userName) -> guest
        //factory.setPassword(password) -> guest
        //factory.setVirtualHost(virtualHost) -> /
        factory.setHost("10.1.57.31") //-> localhost
        //factory.setPort(portNumber) -> 5672

        val conn: Connection = factory.newConnection()

        val channel =conn.createChannel()

        //crear exchange broadcast
        channel.exchangeDeclare("broadcast", "fanout", true);

        return channel
    }
}