package es.unizar.mii.tmdad.chatapp.controller

import es.unizar.mii.tmdad.chatapp.dao.ChatRoom
import es.unizar.mii.tmdad.chatapp.dao.UserEntity
import es.unizar.mii.tmdad.chatapp.dto.*
import es.unizar.mii.tmdad.chatapp.service.UserService
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.messaging.handler.annotation.Header
import org.springframework.messaging.handler.annotation.MessageMapping
import org.springframework.messaging.handler.annotation.Payload
import org.springframework.messaging.simp.SimpMessageHeaderAccessor
import org.springframework.messaging.simp.SimpMessageSendingOperations
import org.springframework.security.core.Authentication
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import java.security.Principal
import java.util.*

@Controller
@RequestMapping("/api/v1/chat")
class ChatController ( val simpMessageSendingOperations: SimpMessageSendingOperations,
    private val userService: UserService){

    private val chatrooms = mutableListOf<ChatRoom>()
    private val logger = LoggerFactory.getLogger(javaClass)

    @GetMapping("contacts")
    fun getContacts(principal: Principal, authentication: Authentication): ResponseEntity<ContactListResponse> {
        val loggedInUser = authentication.principal as UserEntity
        logger.info("LOGGEDIN USER: ${principal.name}")
        logger.info("LOGGEDIN USER: ${loggedInUser.id}")
        logger.info("LOGGEDIN USER: ${loggedInUser.username}")
        val contactList = userService.getAllUsers().map {
            ContactInfo(
                id = it.id,
                email = it.username,
                firstName = it.firstName,
                lastName = it.lastName
            )
        }
        return ResponseEntity.ok(ContactListResponse(
            contacts = contactList
        ))
    }

    @MessageMapping("/newConversation")
    fun newConversation(@Payload message: NewChatRequest, @Header("simpSessionId") sessionId: String) {
        logger.info("Message received: $message")
//        val loggedInUser = authentication.principal as UserEntity
        val chatRoom = ChatRoom(
            id = UUID.randomUUID(),
            contacts = message.contacts.toSet())
        chatrooms.add(chatRoom)

        // TODO CREATE EXCHANGE BINDING
//        simpMessageSendingOperations.convertAndSendToUser(loggedInUser.id.toString(), "/queue/messages", message)
    }

    @MessageMapping("/message")
    fun message(@Payload message: ChatMessage, authentication: Authentication,  @Header("simpSessionId") sessionId: String) {
        val loggedInUser = authentication.principal as UserEntity
        logger.info("LOGGEDIN USER: $loggedInUser")
//        val chatRoom = ChatRoom(
//            id = UUID.randomUUID(),
//            contacts = message.contacts.toSet())
//        chatrooms.add(chatRoom)
        // TODO CREATE EXCHANGE BINDING
        simpMessageSendingOperations.convertAndSendToUser(message.to, "/queue/messages", message)
    }



}