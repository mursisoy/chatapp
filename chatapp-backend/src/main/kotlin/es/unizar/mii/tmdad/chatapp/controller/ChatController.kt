package es.unizar.mii.tmdad.chatapp.controller

import es.unizar.mii.tmdad.chatapp.dao.ChatRoom
import es.unizar.mii.tmdad.chatapp.dao.ChatRoomType
import es.unizar.mii.tmdad.chatapp.dao.UserEntity
import es.unizar.mii.tmdad.chatapp.dto.*
import es.unizar.mii.tmdad.chatapp.service.ChatRoomService
import es.unizar.mii.tmdad.chatapp.service.RabbitService
import es.unizar.mii.tmdad.chatapp.service.UserService
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.messaging.handler.annotation.Header
import org.springframework.messaging.handler.annotation.MessageMapping
import org.springframework.messaging.handler.annotation.Payload
import org.springframework.messaging.simp.SimpMessageSendingOperations
import org.springframework.security.core.Authentication
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.*
import java.nio.ByteBuffer
import java.security.Principal
import java.util.*

@Controller
@RequestMapping("/api/v1/chat")
class ChatController (val simpMessageSendingOperations: SimpMessageSendingOperations,
                      private val userService: UserService,
                      private val chatRoomService: ChatRoomService,
                      private val rabbitService: RabbitService){

//    private val chatrooms = mutableListOf<ChatRoom>()
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
    fun newConversation(authentication: Authentication, @RequestBody chatRoomRequest: NewChatRequest, @Header("simpSessionId") sessionId: String): ResponseEntity<NewChatResponse> {
        val loggedInUser = authentication.principal as UserEntity
        var roomUUID: UUID = UUID.randomUUID()
        val contacts = chatRoomRequest.contacts.toSet()
        if ( chatRoomRequest.type == ChatRoomType.COUPLE) {

            if (contacts.size > 2) {
                return ResponseEntity.unprocessableEntity().build()
            }
            // Prevent users to create couple conversations with others
            if (contacts.indexOf(loggedInUser.getUsername()) == -1) {
                return ResponseEntity.unprocessableEntity().build()
            }

            roomUUID = chatRoomService.coupleChatUUID(
                UUID.fromString(contacts.elementAt(0)),
                UUID.fromString(contacts.elementAt(1))
            )
        }

        val chatRoom = ChatRoom(
            id = roomUUID,
            contacts = contacts,
            owner = loggedInUser.username,
            name = chatRoomRequest.name,
            type = chatRoomRequest.type
        )
        chatRoomService.save(chatRoom)
        rabbitService.createChat(chatRoom)

        return ResponseEntity.ok(NewChatResponse(
            id = chatRoom.id.toString(),
            contacts = chatRoom.contacts.toList(),
            type = chatRoom.type.toString(),
            owner = chatRoom.owner.toString(),
            name = chatRoom.name
        ))
    }

    @DeleteMapping("/conversation")
    fun deleteConversation(authentication: Authentication, @RequestBody infoDelete: DeleteChatRequest) {
        val loggedInUser = authentication.principal as UserEntity
        rabbitService.deleteChat(loggedInUser.getUsername(), infoDelete.id)
    }

    @PutMapping("/conversation/contacts")
    fun addConversationContacts(authentication: Authentication, @RequestBody updateConversationContactsRequest: UpdateConversationContactsRequest, @Header("simpSessionId") sessionId: String): ResponseEntity<String> {
        val loggedInUser = authentication.principal as UserEntity
        rabbitService.addConversationContacts(loggedInUser.getUsername(), updateConversationContactsRequest.id, updateConversationContactsRequest.contacts)
        return ResponseEntity.ok().build()
    }

    @DeleteMapping("/conversation/contacts")
    fun deleteConversationContacts(authentication: Authentication, @RequestBody updateConversationContactsRequest: UpdateConversationContactsRequest, @Header("simpSessionId") sessionId: String): ResponseEntity<String> {
        val loggedInUser = authentication.principal as UserEntity
        rabbitService.deleteConversationContacts(loggedInUser.getUsername(), updateConversationContactsRequest.id, updateConversationContactsRequest.contacts)
        return ResponseEntity.ok().build()
    }

    @MessageMapping("/message")
    fun message(@Payload message: ChatMessage, authentication: Authentication,  @Header("simpSessionId") sessionId: String) {
        simpMessageSendingOperations.convertAndSendToUser(message.to, "/queue/messages", message)
    }



}