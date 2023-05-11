package es.unizar.mii.tmdad.chatapp.service

import org.springframework.stereotype.Service
import es.unizar.mii.tmdad.chatapp.dao.UserEntity
import es.unizar.mii.tmdad.chatapp.exception.UserNotFoundException
import es.unizar.mii.tmdad.chatapp.repository.UserRepository

@Service
class UserService(
    private val userRepository: UserRepository,
//    private val channel: Channel
)  {
    fun register(user: UserEntity): UserEntity {
        // Insert the user
        val newUser = userRepository.save(user)

        val exchangeName=newUser.username+"Exchange"
        val queueName=newUser.username+"Queue"
        val routingKey="*"

//        channel.exchangeDeclare(exchangeName, "direct", true)
//        channel.queueDeclare(queueName, true, false, false, null)
//        channel.queueBind(queueName, exchangeName, routingKey)
//
//        //Bind con el exchange broadcast (solo podran enviar mensajes los usuarios con ROLE=superuser)
//        channel.exchangeBind(exchangeName, "broadcast", "*")

        return newUser
    }

    fun getAllUsers(): List<UserEntity>{
        return  userRepository.findAll()
    }
    fun login(user: UserEntity) {
        val queueName=user.username+"Queue"

        val autoAck = false

        val consumerTag=user.username

//        channel.basicConsume(queueName, autoAck, consumerTag,
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
    }

    fun loadUserByUsername(username: String): UserEntity{
        return userRepository.findByUsername(username) ?: throw UserNotFoundException("User nor found")
    }

}