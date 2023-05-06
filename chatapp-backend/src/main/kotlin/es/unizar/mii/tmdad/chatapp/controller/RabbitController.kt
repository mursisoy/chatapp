package es.unizar.mii.tmdad.chatapp.controller

import com.rabbitmq.client.AMQP
import com.rabbitmq.client.Channel
import com.rabbitmq.client.DefaultConsumer
import com.rabbitmq.client.Envelope
import es.unizar.mii.tmdad.chatapp.dto.*
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController
import java.io.IOException


@RestController
class RabbitController(private val channel: Channel) {

    private val logger = LoggerFactory.getLogger(Controller::class.java)

    @PostMapping("/logout")
    fun logout(
        @RequestBody authenticationRequest: AuthenticationRequest
    ) {
        val consumerTag=authenticationRequest.username
        channel.basicCancel(consumerTag);
    }

    @PostMapping("/send")
    fun sendMessage() {
        val exchangeName="exchange"
        //realmente seria idUser-exchange
        val routingKey=null

        val messageBodyBytes = "Hello, world!".toByteArray()
        channel.basicPublish(exchangeName, routingKey, null, messageBodyBytes)
    }


    @PostMapping("/newChat")
    fun createNewChat(@RequestBody infoChat: NewChatRequest) {
        //crear exchange con idSala
        channel.exchangeDeclare(infoChat.idSala, "fanout", true);
            //el idSala sera igual a un numeor aleatorioa concatenado con : al id del
            // usuario que creo la sala(admin) y con el tipo de sala

        //crear binding entre el exchange de la sala y el de los usuarios pertenecientes a esta
        for (user in infoChat.userOfGroup) {
            channel.exchangeBind(user + "Exchange", infoChat.idSala, null)
        }

    }

    @PostMapping("/updateChat")
    fun updateChat(@RequestBody infoUpdate: UpdateChatRequest) {
        if (infoUpdate.origin == infoUpdate.idSala.split(":")[1]) {
            if (infoUpdate.idSala.split(":")[2] == "grupo") {
                if (infoUpdate.action == "delete") {
                    for (user in infoUpdate.usersAffected) {
                        //unbindings
                        channel.exchangeUnbind(user + "Exchange", infoUpdate.idSala, null)
                    }
                }
                if (infoUpdate.action == "add") {
                    for (user in infoUpdate.usersAffected) {
                        channel.exchangeBind(user+ "Exchange", infoUpdate.idSala, null)
                    }
                }
            }
        }
    }

    @PostMapping("/deleteChat")
    fun deleteChat(@RequestBody  infoDelete: DeleteChatRequest) {
        //borrar exchange de la sala si eres el propietario
        if (infoDelete.idSala.split(":")[2] == "grupo") {
            if (infoDelete.origin == infoDelete.idSala.split(":")[1]) {
                //delete biding
                channel.exchangeDelete(infoDelete.idSala)
            }
        } else {
            channel.exchangeDelete(infoDelete.idSala)
        }

    }
}