package es.unizar.mii.tmdad.chatapp.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.rabbitmq.client.AMQP
import com.rabbitmq.client.Channel
import com.rabbitmq.client.DefaultConsumer
import com.rabbitmq.client.Envelope
import com.rabbitmq.http.client.Client
import es.unizar.mii.tmdad.chatapp.dao.ChatMessage
import es.unizar.mii.tmdad.chatapp.dao.ChatRoom
import es.unizar.mii.tmdad.chatapp.dao.ChatRoomType
import es.unizar.mii.tmdad.chatapp.dao.UserEntity
import es.unizar.mii.tmdad.chatapp.exception.ChatAuthorizationException
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Lazy
import org.springframework.messaging.simp.SimpMessageSendingOperations
import org.springframework.stereotype.Service
import java.io.IOException
import java.util.*


@Service
class RabbitService (private val channel: Channel,
                     private val rabbitManageService: RabbitManageService,
                     private val cliente: Client){
    private val logger = LoggerFactory.getLogger(javaClass)

    @Autowired
    @Lazy
    private lateinit var simpMessageSendingOperations: SimpMessageSendingOperations

    fun registRabbit(username: String ){
        val exchangeName= "$username-ux"
        val queueName= "$username-uq"
        val routingKey="*"

        channel.exchangeDeclare(exchangeName, "direct", true)
        channel.queueDeclare(queueName, true, false, false, null)
        channel.queueBind(queueName, exchangeName, routingKey)

        //Bind con el exchange broadcast (solo podran enviar mensajes los usuarios con ROLE=superuser)
        channel.exchangeBind(exchangeName, "broadcast", "*")

    }

    fun activeConsumer(user: UserEntity, sessionId: String){

        val queueName="${user.id}-uq"

        val consumerTag = channel.basicConsume(queueName, false, sessionId,
            object: DefaultConsumer(channel) {
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
        logger.debug(consumerTag)
        logger.debug(rabbitManageService.activeUsers()?.map { it.consumerTag }.toString())
    }

    fun desactiveConsumer(username: String){
        try {
            channel.basicCancel(username)
        } catch (e: IOException) {
            logger.error(e.message)
        }
    }

    fun sendMessage(message: ChatMessage): Boolean{
        val mapper = ObjectMapper()
        return if ( rabbitManageService.authorizeSendToGroup(
                "${message.to}-cx",
                "${message.from}-ux"
            ) ) {
            channel.basicPublish("${message.to}-cx", "*", null, mapper.writeValueAsBytes(message))
            true
        } else {
            false
        }
    }

    fun createChat(chatRoom: ChatRoom){
        //argumentos asociados al exchange de sala
        val args=mutableMapOf<String, Any>()
        if(chatRoom.contacts.size==2){
            args["tipo"]=ChatRoomType.COUPLE.toString()
        }
        else{
            args["tipo"]=ChatRoomType.GROUP.toString()
        }

        if (chatRoom.owner != null) {
            args["admin"] = chatRoom.owner.toString()
        }

        //crear exchange con idSala
        channel.exchangeDeclare("${chatRoom.id}-cx", "fanout", true, false, args)
        //durable para que sobreviva reinicios y no autodelete para que no se borre si no se usa

        //crear binding entre el exchange de la sala y el de los usuarios pertenecientes a esta
        for (user in chatRoom.contacts) {
            channel.exchangeBind("${user}-ux", "${chatRoom.id}-cx", "*")
        }
    }

    fun addConversationContacts(origin: String, idSala: String, usersAffected: Vector<String>){
        //obtención de los argumentos del exchange
        val exchange= cliente.getExchange("/", idSala)
        val exchArgs=exchange.arguments
        if (origin == exchArgs["admin"].toString()) {
            if (ChatRoomType.valueOf(exchArgs["tipo"] as String) == ChatRoomType.GROUP) {
                for (user in usersAffected) {
                    channel.exchangeBind("$user-ux", idSala, "*")
                }
            } else {
                throw ChatAuthorizationException(" It is not  group ")
            }
        } else {
            throw ChatAuthorizationException(" You are not the admin of the group")
        }
    }


    fun deleteConversationContacts(origin: String, idSala: String, usersAffected: Vector<String>){
        //obtención de los argumentos del exchange
        val exchange= cliente.getExchange("/", idSala)
        val exchArgs=exchange.arguments
        if (origin == exchArgs["admin"]) {
            if (exchArgs["tipo"] == ChatRoomType.GROUP) {
                for (user in usersAffected) {
                    //unbindings
                    channel.exchangeUnbind("$user-ux", idSala, "*")
                }
            }
            else{
                throw ChatAuthorizationException(" It is not a group")
            }
        }
        else{
            throw ChatAuthorizationException(" You are not the admin of the group")
        }

    }


    fun deleteChat(origin: String, idSala: String){
        //obtenecion de los argumentos del exchange de sala
        val exchange= cliente.getExchange("/", idSala)
        val exchArgs=exchange.arguments
        //borrar exchange de la sala si eres el propietario
        if (exchArgs["tipo"] == ChatRoomType.GROUP) {
            if (origin ==  exchArgs["admin"]) {
                //delete biding
                channel.exchangeDelete(idSala)
            }
            else{
                throw ChatAuthorizationException(" You are not the admin of the group")
            }
        }
        else {
            channel.exchangeDelete(idSala)
        }

    }

}