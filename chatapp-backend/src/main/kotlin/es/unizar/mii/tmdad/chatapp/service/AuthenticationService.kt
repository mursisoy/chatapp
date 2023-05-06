package es.unizar.mii.tmdad.chatapp.service

import com.rabbitmq.client.AMQP
import com.rabbitmq.client.Channel
import com.rabbitmq.client.DefaultConsumer
import com.rabbitmq.client.Envelope
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import es.unizar.mii.tmdad.chatapp.dto.AuthenticationRequest
import es.unizar.mii.tmdad.chatapp.dto.AuthenticationResponse
import es.unizar.mii.tmdad.chatapp.dto.RegisterRequest
import es.unizar.mii.tmdad.chatapp.entity.Role
import es.unizar.mii.tmdad.chatapp.entity.UserEntity
import es.unizar.mii.tmdad.chatapp.exception.UserNotFoundException
import es.unizar.mii.tmdad.chatapp.repository.UserRepository
import io.jsonwebtoken.Claims
import java.io.IOException

@Service
class AuthenticationService(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder,
    private val jwtService: JwtService,
    private val authenticationManager: AuthenticationManager,
    private val channel: Channel
)  {
    fun register(registerRequest: RegisterRequest): AuthenticationResponse {
        val user = UserEntity(
            username = registerRequest.username,
            password = passwordEncoder.encode(registerRequest.password),
            role = Role.USER
        )

        userRepository.save(user)

        val exchangeName=registerRequest.username+"Exchange"
        val queueName=registerRequest.username+"Queue"
        val routingKey="*"

        channel.exchangeDeclare(exchangeName, "direct", true);
        channel.queueDeclare(queueName, true, false, false, null);
        channel.queueBind(queueName, exchangeName, routingKey);

        //Bind con el exchange broadcast (solo podran enviar mensajes los usuarios con ROLE=superuser)
        channel.exchangeBind(exchangeName, "broadcast", "*")

        val jwt = jwtService.generateToken(user)

        return AuthenticationResponse(
            accessToken = jwt,
            expiresAt = jwtService.extractClaim(jwt, Claims::getExpiration).time,
            type="Bearer"
        )
    }

    fun login(authenticationRequest: AuthenticationRequest): AuthenticationResponse {
        authenticationManager.authenticate(
            UsernamePasswordAuthenticationToken(
                authenticationRequest.username,
                authenticationRequest.password
            )
        )

        val user =
            userRepository.findByUsername(authenticationRequest.username) ?: throw UserNotFoundException("User nor found")

        //Cuando un usuario se conecta al sistema se crea el topic se hace el biding y se activa el consumidor

        val queueName=authenticationRequest.username+"Queue"

        val autoAck = false;

        val consumerTag=authenticationRequest.username

        channel.basicConsume(queueName, autoAck, consumerTag,
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

        val jwt = jwtService.generateToken(user)
        return AuthenticationResponse(
            accessToken = jwt,
            expiresAt = jwtService.extractClaim(jwt, Claims::getExpiration).time,
            type="Bearer")
    }
}