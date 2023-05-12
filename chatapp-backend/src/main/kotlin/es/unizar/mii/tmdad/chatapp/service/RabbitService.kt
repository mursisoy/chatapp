package es.unizar.mii.tmdad.chatapp.service

import com.rabbitmq.client.AMQP
import com.rabbitmq.client.Channel
import com.rabbitmq.client.DefaultConsumer
import com.rabbitmq.client.Envelope
import org.springframework.stereotype.Service
import java.io.IOException
import java.util.Vector

import com.rabbitmq.http.client.Client;

@Service
class RabbitService (private val channel: Channel,
    private val cliente: Client){

    fun registRabbit(username: String ){
        val exchangeName=username+"Exchange"
        val queueName=username+"Queue"
        val routingKey="*"

        channel.exchangeDeclare(exchangeName, "direct", true)
        channel.queueDeclare(queueName, true, false, false, null)
        channel.queueBind(queueName, exchangeName, routingKey)

        //Bind con el exchange broadcast (solo podran enviar mensajes los usuarios con ROLE=superuser)
        channel.exchangeBind(exchangeName, "broadcast", "*")

    }

    fun activeConsumer(username: String){

        val queueName=username+"Queue"

        val consumerTag=username

        channel.basicConsume(queueName, false, consumerTag,
            object : DefaultConsumer(channel) {
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

                }
            })
    }

    fun desactiveConsumer(username: String){
        channel.basicCancel(username);
    }

    fun send(exchangeName: String, message:String){
        channel.basicPublish(exchangeName, "*", null, message.toByteArray())
    }

    fun createChat(idSala: String, userOfGroup: Vector<String>){
        //argumentos asociados al exchange de sala
        val args=mutableMapOf<String, Any>()
        if(userOfGroup.size==2){
            args["tipo"]="individual"
        }
        else{
            args["tipo"]="grupo"
        }
        args["admin"]=userOfGroup[0]

        //crear exchange con idSala
        channel.exchangeDeclare(idSala, "fanout", true, false, args);
        //durable para que sobreviva reinicios y no autodelete para que no se borre si no se usa

        //crear binding entre el exchange de la sala y el de los usuarios pertenecientes a esta
        for (user in userOfGroup) {
            channel.exchangeBind(user + "Exchange", idSala, "*")
        }
    }

    fun updateChat(origin: String, idSala: String, action:String, usersAffected: Vector<String>){
        //obtención de los argumentos del exchange
        val exchange= cliente.getExchange("/", idSala)
        val exchArgs=exchange.arguments

        if (origin == exchArgs["admin"]) {
            if (exchArgs["tipo"] == "grupo") {
                if (action == "delete") {
                    for (user in usersAffected) {
                        //unbindings
                        channel.exchangeUnbind(user + "Exchange", idSala, "*")
                    }
                }
                if (action == "add") {
                    for (user in usersAffected) {
                        channel.exchangeBind(user+ "Exchange", idSala, "*")
                    }
                }
            }
        }

    }


    fun deleteChat(origin: String, idSala: String){
        //obtenecion de los argumentos del exchange de sala
        val exchange= cliente.getExchange("/", idSala)
        val exchArgs=exchange.arguments
        //borrar exchange de la sala si eres el propietario
        if (exchArgs["tipo"] == "grupo") {
            if (origin ==  exchArgs["admin"]) {
                //delete biding
                channel.exchangeDelete(idSala)
            }
        } else {
            channel.exchangeDelete(idSala)
        }

    }

}