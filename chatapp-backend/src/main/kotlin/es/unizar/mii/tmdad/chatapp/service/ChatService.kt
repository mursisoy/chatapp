//package es.unizar.mii.tmdad.chatapp.service
//
//import com.rabbitmq.client.AMQP
//import com.rabbitmq.client.Channel
//import com.rabbitmq.client.DefaultConsumer
//import com.rabbitmq.client.Envelope
//import es.unizar.mii.tmdad.chatapp.dao.UserEntity
//import es.unizar.mii.tmdad.chatapp.dto.AuthenticationRequest
//import es.unizar.mii.tmdad.chatapp.dto.AuthenticationResponse
//import org.springframework.stereotype.Service
//import java.io.IOException
//
//@Service
//class ChatService (private val channel: Channel){
//
//    fun registerUserQueue(user: UserEntity){
//        //Cuando un usuario se conecta al sistema se crea el topic se hace el biding y se activa el consumidor
//
//        val exchangeName="${user.username}-user-exchange"
//        val queueName="${user.username}.inbox"
//
//        channel.exchangeDeclare(exchangeName, "direct", true);
//        channel.queueDeclare(queueName, true, false, false, null);
//        channel.queueBind(queueName, exchangeName, null);
//
//        //Bind con el exchange broadcast (solo podran enviar mensajes los usuarios con ROLE=superuser)
//        channel.exchangeBind(exchangeName, "broadcast", null)
//    }
//
////    fun login(authenticationRequest: AuthenticationRequest): AuthenticationResponse {
////
////        val queueName=authenticationRequest.username+"Queue"
////
////        val autoAck = false;
////
////        val consumerTag=authenticationRequest.username
////
////        channel.basicConsume(queueName, autoAck, consumerTag,
////            object : DefaultConsumer(channel) {
////                @Throws(IOException::class)
////                override fun handleDelivery(
////                    consumerTag: String?,
////                    envelope: Envelope,
////                    properties: AMQP.BasicProperties,
////                    body: ByteArray?
////                ) {
////                    val routingKey = envelope.routingKey
////                    val contentType = properties.contentType
////                    val deliveryTag = envelope.deliveryTag
////                    // (process the message components here ...)
////                    channel.basicAck(deliveryTag, false)
////
////                }
////            })
////
////
////    }
//}