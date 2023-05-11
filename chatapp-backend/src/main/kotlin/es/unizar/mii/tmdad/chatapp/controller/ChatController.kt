package es.unizar.mii.tmdad.chatapp.controller

import es.unizar.mii.tmdad.chatapp.dao.ChatRoom
import es.unizar.mii.tmdad.chatapp.dao.ChatRoomType
import es.unizar.mii.tmdad.chatapp.dao.UserEntity
import es.unizar.mii.tmdad.chatapp.dto.*
import es.unizar.mii.tmdad.chatapp.service.ChatService
import es.unizar.mii.tmdad.chatapp.service.UserService
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.messaging.handler.annotation.Header
import org.springframework.messaging.handler.annotation.MessageMapping
import org.springframework.messaging.handler.annotation.Payload
import org.springframework.messaging.simp.SimpMessageSendingOperations
import org.springframework.security.core.Authentication
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import java.nio.ByteBuffer
import java.security.Principal
import java.util.*

@Controller
@RequestMapping("/api/v1/chat")
class ChatController ( val simpMessageSendingOperations: SimpMessageSendingOperations,
    private val userService: UserService,
    private val chatService: ChatService){

    private val chatrooms = mutableListOf<ChatRoom>()
    private val logger = LoggerFactory.getLogger(javaClass)

    @GetMapping("contacts")
    fun getContacts(principal: Principal, authentication: Authentication): ResponseEntity<ContactListResponse> {
        val loggedInUser = authentication.principal as UserEntity
        logger.info("LOGGEDIN USER: ${principal.name}")
        logger.info("LOGGEDIN USER: ${loggedInUser.username}")
        val contactList = userService.getAllUsers().map {
            ContactInfo(
                username = it.getUsername(),
                email = it.email,
                firstName = it.firstName,
                lastName = it.lastName
            )
        }
        return ResponseEntity.ok(ContactListResponse(
            contacts = contactList
        ))
    }

    private fun convertUUIDToBytes(uuid: UUID): ByteArray? {
        val bb = ByteBuffer.wrap(ByteArray(16))
        bb.putLong(uuid.mostSignificantBits)
        bb.putLong(uuid.leastSignificantBits)
        return bb.array()
    }

    @PostMapping("/conversation")
    fun newConversation(authentication: Authentication, @Payload chatRoomRequest: NewChatRequest, @Header("simpSessionId") sessionId: String): ResponseEntity<ContactListResponse> {
        val loggedInUser = authentication.principal as UserEntity
        var roomUUID: UUID = UUID.randomUUID()
        if ( chatRoomRequest.type == ChatRoomType.COUPLE) {
            val contacts = chatRoomRequest.contacts.toSet()
            if (contacts.size > 2) {
                return ResponseEntity.unprocessableEntity().build()
            }
            // Prevent users to create couple conversations with others
            if (contacts.indexOf(loggedInUser.getUsername()) == -1) {
                return ResponseEntity.unprocessableEntity().build()
            }

            roomUUID = chatService.coupleChatUUID(
                UUID.fromString(contacts.elementAt(0)),
                UUID.fromString(contacts.elementAt(1))
            )
        }
//            val chatRoom = ChatRoom(
//                id = UUID(high,low),
//                contacts = message.contacts.toSet())
//            chatrooms.add(chatRoom)

        // TODO CREATE EXCHANGE BINDING
        //rabbitService.createChat(chatRoom.id, chatRoom.contacts)
//        simpMessageSendingOperations.convertAndSendToUser(loggedInUser.id.toString(), "/queue/messages", message)
    }

    @MessageMapping("/message")
    fun message(@Payload message: ChatMessage, authentication: Authentication,  @Header("simpSessionId") sessionId: String) {
        simpMessageSendingOperations.convertAndSendToUser(message.to, "/queue/messages", message)
    }



}