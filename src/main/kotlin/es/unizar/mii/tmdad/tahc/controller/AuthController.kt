package es.unizar.mii.tmdad.tahc.controller

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import es.unizar.mii.tmdad.tahc.dto.AuthenticationRequest
import es.unizar.mii.tmdad.tahc.dto.AuthenticationResponse
import es.unizar.mii.tmdad.tahc.dto.RegisterRequest
import es.unizar.mii.tmdad.tahc.exception.UserNotFoundException
import es.unizar.mii.tmdad.tahc.service.AuthenticationService

@RestController
@RequestMapping("/api/v1/auth")
class AuthController(
    private val authenticationService: AuthenticationService,
    private val channel: Channel
) {

    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler(UserNotFoundException::class)
    fun handleNotFoundException(e: NoSuchElementException): ResponseEntity<Map<String, String?>> {
        return ResponseEntity(mapOf("message" to e.message), HttpStatus.NOT_FOUND)
    }

    @PostMapping("/register")
    fun register(
        @RequestBody registerRequest: RegisterRequest
    ): ResponseEntity<AuthenticationResponse> {
        //Cuando un usuario se registra en nuestro sistema le creamos el exchange

        val exchangeName=registerRequest.username+"Exchange"
        //realmente seria idUserExchange
        val queueName=registerRequest.username+"Queue"
        //sera el idUserQueue
        val routingKey=registerRequest.username
        //sera el idUser
        channel.exchangeDeclare(exchangeName, "direct", true);
        channel.queueDeclare(queueName, true, false, false, null);
        channel.queueBind(queueName, exchangeName, routingKey);

        return ResponseEntity.ok(authenticationService.register(registerRequest))
    }

    @PostMapping("/login")
    fun login(
        @RequestBody authenticationRequest: AuthenticationRequest
    ): ResponseEntity<AuthenticationResponse> {
        //Cuando un usuario se conecta al sistema se crea el topic se hace el biding y se activa el consumidor

        val queueName=authenticationRequest.username+"Queue"
        //sera el idUser

        val autoAck = false;

        val consumerTag=authenticationRequest.username
        //ser idUser-tag

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

        return ResponseEntity.ok(authenticationService.login(authenticationRequest))
    }
    
    
    @PostMapping("/logout")
    fun logout(
        @RequestBody authenticationRequest: AuthenticationRequest
    ) {
        val consumerTag=authenticationRequest.username
        //ser idUser-tag
        channel.basicCancel(consumerTag);
    }

    @PostMapping("/send")
    fun sendMessage() {
        val exchangeName="exchange"
        //realmente seria idUser-exchange
        val routingKey="foo.bar"

        val messageBodyBytes = "Hello, world!".toByteArray()
        channel.basicPublish(exchangeName, routingKey, null, messageBodyBytes)
    }

}
