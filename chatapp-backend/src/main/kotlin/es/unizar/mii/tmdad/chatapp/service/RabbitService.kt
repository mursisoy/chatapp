//package es.unizar.mii.tmdad.chatapp.service
//
//import com.rabbitmq.client.AMQP
//import com.rabbitmq.client.Channel
//import com.rabbitmq.client.DefaultConsumer
//import com.rabbitmq.client.Envelope
//import org.springframework.stereotype.Service
//import java.io.IOException
//import java.util.Vector
//
//@Service
//class RabbitService (private val channel: Channel){
//
//    fun registRabbit(username: String ){
//        val exchangeName=username+"Exchange"
//        val queueName=username+"Queue"
//        val routingKey="*"
//
//        channel.exchangeDeclare(exchangeName, "direct", true)
//        channel.queueDeclare(queueName, true, false, false, null)
//        channel.queueBind(queueName, exchangeName, routingKey)
//
//        //Bind con el exchange broadcast (solo podran enviar mensajes los usuarios con ROLE=superuser)
//        channel.exchangeBind(exchangeName, "broadcast", "*")
//
//    }
//
//    fun activeConsumer(username: String){
//
//        val queueName=username+"Queue"
//
//        val consumerTag=username
//
//        channel.basicConsume(queueName, false, consumerTag,
//            object : DefaultConsumer(channel) {
//                @Throws(IOException::class)
//                override fun handleDelivery(
//                    consumerTag: String?,
//                    envelope: Envelope,
//                    properties: AMQP.BasicProperties,
//                    body: ByteArray?
//                ) {
//                    val routingKey = envelope.routingKey
//                    val contentType = properties.contentType
//                    val deliveryTag = envelope.deliveryTag
//                    // (process the message components here ...)
//                    channel.basicAck(deliveryTag, false)
//
//                }
//            })
//    }
//
//    fun desactiveConsumer(username: String){
//        channel.basicCancel(username);
//    }
//
//    fun send(exchangeName: String, message:String){
//        channel.basicPublish(exchangeName, "*", null, message.toByteArray())
//    }
//
//    fun createChat(idSala: String, userOfGroup: Vector<String>){
//        //crear exchange con idSala
//        channel.exchangeDeclare(idSala, "fanout", true);
//        //el idSala sera igual a un numeor aleatorioa concatenado con : al id del
//        // usuario que creo la sala(admin) y con el tipo de sal
//
//        //crear binding entre el exchange de la sala y el de los usuarios pertenecientes a esta
//        for (user in userOfGroup) {
//            channel.exchangeBind(user + "Exchange", idSala, "*")
//        }
//    }
//
//    fun updateChat(origin: String, idSala: String, action:String, usersAffected: Vector<String>){
//        if (origin == idSala.split(":")[1]) {
//            if (idSala.split(":")[2] == "grupo") {
//                if (action == "delete") {
//                    for (user in usersAffected) {
//                        //unbindings
//                        channel.exchangeUnbind(user + "Exchange", idSala, "*")
//                    }
//                }
//                if (action == "add") {
//                    for (user in usersAffected) {
//                        channel.exchangeBind(user+ "Exchange", idSala, "*")
//                    }
//                }
//            }
//        }
//
//    }
//
//
//    fun deleteChat(origin: String, idSala: String){
//        //borrar exchange de la sala si eres el propietario
//        if (idSala.split(":")[2] == "grupo") {
//            if (origin == idSala.split(":")[1]) {
//                //delete biding
//                channel.exchangeDelete(idSala)
//            }
//        } else {
//            channel.exchangeDelete(idSala)
//        }
//
//    }
//
//}