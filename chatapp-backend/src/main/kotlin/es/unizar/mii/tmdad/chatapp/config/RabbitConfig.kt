package es.unizar.mii.tmdad.chatapp.config

import com.rabbitmq.client.Channel
import com.rabbitmq.client.ConnectionFactory
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.net.ConnectException

import com.rabbitmq.http.client.Client;
import com.rabbitmq.http.client.ClientParameters
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
        factory.username = "guest"
        factory.password = "guest"
        factory.host = "rabbitmq" //-> localhost
        //factory.setPort(portNumber) -> 5672
        factory.isAutomaticRecoveryEnabled = true


        while (true) {
            try {
                val conn = factory.newConnection()
                val channel = conn.createChannel()

                //crear exchange broadcast
                channel.exchangeDeclare("broadcast", "fanout", true)

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
                val cliente = Client(
                    ClientParameters()
                        .url("http://rabbitmq:15672/api/")
                        .username("guest")
                        .password("guest")
                );

                return cliente
            } catch (e: ConnectException) {
                Thread.sleep(5000)
                // apply retry logic
            }
        }
    }
}