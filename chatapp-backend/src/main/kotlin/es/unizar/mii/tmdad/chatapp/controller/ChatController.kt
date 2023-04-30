package es.unizar.mii.tmdad.chatapp.controller

import org.slf4j.LoggerFactory
import org.springframework.messaging.handler.annotation.Header
import org.springframework.messaging.handler.annotation.MessageMapping
import org.springframework.messaging.handler.annotation.Payload
import org.springframework.messaging.simp.SimpMessageSendingOperations
import org.springframework.stereotype.Controller

@Controller
class ChatController ( val simpMessageSendingOperations: SimpMessageSendingOperations){

    private val logger = LoggerFactory.getLogger(javaClass)

    @MessageMapping("/queue/test")
    fun message(@Payload message: Any, @Header("simpSessionId") sessionId: String) {
        logger.info("WWWWWW")
    }

}