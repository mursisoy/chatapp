package es.unizar.mii.tmdad.chatapp.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.rabbitmq.client.Channel
import com.rabbitmq.client.ConnectionFactory
import com.rabbitmq.http.client.Client
import es.unizar.mii.tmdad.chatapp.dao.ChatMessage
import es.unizar.mii.tmdad.chatapp.dao.ChatRoom
import es.unizar.mii.tmdad.chatapp.dao.ChatRoomType
import es.unizar.mii.tmdad.chatapp.exception.ChatAuthorizationException
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.util.*

@Service
class RabbitService (private val channel: Channel,
                     private val rabbitManageService: RabbitManageService,
                     private val cliente: Client,
    private val rns: RabbitNamingService){
    private val logger = LoggerFactory.getLogger(javaClass)

    @Value("\${spring.rabbitmq-amqp.vhost}")
    private val vhost: String? = ConnectionFactory.DEFAULT_VHOST

    fun registRabbit(userId: UUID ){
        val exchangeName= rns.getUserExchangeName(userId)
        val queueName= rns.getUserQueueName(userId)
        val routingKey="*"

        channel.exchangeDeclare(exchangeName, "direct", true)
        channel.queueDeclare(queueName, true, false, false, null)
        channel.queueBind(queueName, exchangeName, routingKey)

        //Bind con el exchange broadcast (solo podran enviar mensajes los usuarios con ROLE=superuser)
        channel.exchangeBind(exchangeName, rns.getBroadcastExchangeName(), "*")
    }

    fun getChannel(): Channel {
        return channel
    }


    fun sendMessage(message: ChatMessage): Boolean{
        val mapper = ObjectMapper()
        val conversationExchange = rns.getConversationExchangeName(message.to)
        return if ( rabbitManageService.authorizeSendToGroup(
                conversationExchange,
                rns.getUserExchangeName(message.from_id)
            ) ) {
            channel.basicPublish(
                conversationExchange,
                "*",
                null,
                mapper.writeValueAsString(message).toByteArray())
            true
        } else {
            false
        }
    }

    fun createChat(chatRoom: ChatRoom){
        //argumentos asociados al exchange de sala
        val args=mutableMapOf<String, Any>()
        if(chatRoom.contacts.size==2){
            args["type"]=ChatRoomType.COUPLE.toString()
        }
        else{
            args["type"]=ChatRoomType.GROUP.toString()
        }

        if (chatRoom.owner != null) {
            args["owner"] = chatRoom.owner.toString()
        }
        args["name"] = chatRoom.name.toString()
        args["id"] = chatRoom.id.toString()

        val conversationExchange = rns.getConversationExchangeName(chatRoom.id)
        //crear exchange con idSala
        channel.exchangeDeclare(conversationExchange, "fanout", true, false, args)
        //durable para que sobreviva reinicios y no autodelete para que no se borre si no se usa

        //crear binding entre el exchange de la sala y el de los usuarios pertenecientes a esta
        for (user in chatRoom.contacts) {
            channel.exchangeBind(rns.getUserExchangeName(user), conversationExchange, "*")
        }
    }

    fun addConversationContacts(userId: UUID, conversationId: UUID, usersAffected: Vector<UUID>){
        //obtención de los argumentos del exchange
        val exchange= cliente.getExchange(vhost, rns.getConversationExchangeName(conversationId))
        val exchArgs=exchange.arguments
        if (userId == UUID.fromString("${exchArgs["admin"]}")) {
            if (ChatRoomType.valueOf("${exchArgs["type"]}") == ChatRoomType.GROUP) {
                for (user in usersAffected) {
                    channel.exchangeBind(rns.getUserExchangeName(user), exchange.name, "*")
                }
            } else {
                throw ChatAuthorizationException(" It is not  group ")
            }
        } else {
            throw ChatAuthorizationException(" You are not the admin of the group")
        }
    }

    fun deleteConversationContacts(userId: UUID, conversationId: UUID, usersAffected: Vector<UUID>){
        //obtención de los argumentos del exchange
        val exchange= cliente.getExchange(vhost, rns.getConversationExchangeName(conversationId))
        val exchArgs=exchange.arguments
        if (userId == UUID.fromString("${exchArgs["admin"]}")) {
            if (exchArgs["type"] == ChatRoomType.GROUP) {
                for (user in usersAffected) {
                    //unbindings
                    channel.exchangeUnbind(rns.getUserExchangeName(user), exchange.name, "*")
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

    fun deleteChat(userId: UUID, conversationId: UUID,){
        //obtenecion de los argumentos del exchange de sala
        val exchange= cliente.getExchange(vhost, rns.getConversationExchangeName(conversationId))
        val exchArgs=exchange.arguments
        if (userId == UUID.fromString("${exchArgs["admin"]}")) {
            if (exchArgs["type"] == ChatRoomType.GROUP) {
                channel.exchangeDelete(exchange.name)
            } else {
                throw ChatAuthorizationException(" You are not the admin of the group")
            }
        }
    }

}