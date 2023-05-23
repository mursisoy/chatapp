package es.unizar.mii.tmdad.chatapp.service

import com.rabbitmq.client.ConnectionFactory
import com.rabbitmq.http.client.Client
import com.rabbitmq.http.client.domain.BindingInfo
import com.rabbitmq.http.client.domain.ConsumerDetails
import com.rabbitmq.http.client.domain.ExchangeInfo
import es.unizar.mii.tmdad.chatapp.dao.ChatRoom
import es.unizar.mii.tmdad.chatapp.dao.UserEntity
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.util.*

@Service
class RabbitManageService (
    private val cliente: Client,
    private val rns: RabbitNamingService){
    private val logger = LoggerFactory.getLogger(javaClass)

    @Value("\${spring.rabbitmq-http.vhost}")
    private val vhost: String? = ConnectionFactory.DEFAULT_VHOST
//    fun authorizeGroup(conversationId: String, username: String ): Boolean{
//        val bindings=cliente.bindings
//        var authorize=false
//        for (b in bindings){
//            if (b.source==conversationId && b.destination==username){
//                authorize=true
//            }
//        }
//        return authorize
//    }

    fun authorizeSendToGroup(conversationExchange: String, userExchange: String ): Boolean{
        val bindingInfo = cliente.getExchangeBindingsBetween(vhost, conversationExchange, userExchange )
        return bindingInfo.isNotEmpty()
    }

    fun getBindings(): MutableList<BindingInfo>? {
        return  cliente.bindings
    }
    fun getExchanges(): MutableList<ExchangeInfo>? {
        return cliente.exchanges
    }

    fun getConversationExchange(conversationId: UUID) : ChatRoom? {
        val exInfo = cliente.getExchange(vhost, rns.getConversationExchangeName(conversationId))
        return if (exInfo != null) {
            ChatRoom(
                id = UUID.fromString(exInfo.arguments["id"].toString()),
                type = exInfo.arguments["type"].toString(),
                contacts = getContactsFromExchange(conversationId),
                owner = try{ UUID.fromString(exInfo.arguments["owner"].toString()) } catch (e: IllegalArgumentException){null},
                name = exInfo.arguments["name"].toString()
            )
        } else {
            null
        }
    }

    fun getContactsFromExchange(conversationId: UUID) : Set<UUID> {
        if ( conversationId == rns.BROADCAST_QUEUE_ID)
            return emptySet()

        val bindings = cliente.getBindingsBySource(vhost, rns.getConversationExchangeName(conversationId))
        val contactList: MutableSet<UUID> = mutableSetOf()
        for(b in bindings!!) {
            if (b.destination.toString().endsWith("-ux")) {
                try {
                        contactList.add(UUID.fromString(b.destination.substring(0, b.destination.length - 3)))
                } catch (e: IllegalArgumentException) {
                    logger.debug(b.source)
                }
            }
        }
        return contactList
    }

    fun getConversationsForUser(user: UserEntity) : List<ChatRoom> {
        val bindings = cliente.getExchangeBindingsByDestination(vhost, "${user.id}-ux")
        val conversationList: MutableList<ChatRoom> = mutableListOf()
        for(b in bindings!!) {
            try {
                val cr = getConversationExchange(UUID.fromString(b.source.substring(0, b.source.length-3)))
                if (cr != null) {
                    conversationList.add(cr)
                }
            }catch (e: IllegalArgumentException) {
                logger.debug(b.source)
            }
        }
        return conversationList
    }

    fun salas(): MutableList<ExchangeInfo>{
        val exchanges=getExchanges()
        var exchangeSalas: MutableList<ExchangeInfo> = mutableListOf()
        for (e in exchanges!!){
            if (e.arguments["type"]!=null){
                exchangeSalas.add(e)
            }
        }
        return exchangeSalas

    }
    fun users(): MutableList<ExchangeInfo>{
        val exchanges=getExchanges()
        var exchangeUsers: MutableList<ExchangeInfo> = mutableListOf()
        for (e in exchanges!!){
            if (e.arguments["type"]==null){
                exchangeUsers.add(e)
            }
        }
        return exchangeUsers

    }

    fun activeUsers(): MutableList<ConsumerDetails>? {
        return cliente.consumers
    }
}