package es.unizar.mii.tmdad.chatapp.controller

import com.fasterxml.uuid.Generators
import es.unizar.mii.tmdad.chatapp.dao.ChatMessage
import es.unizar.mii.tmdad.chatapp.dao.ChatRoom
import es.unizar.mii.tmdad.chatapp.dao.ChatRoomType
import es.unizar.mii.tmdad.chatapp.dao.UserEntity
import es.unizar.mii.tmdad.chatapp.dto.*
import es.unizar.mii.tmdad.chatapp.service.*
import org.apache.commons.io.IOUtils
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.messaging.handler.annotation.Header
import org.springframework.messaging.handler.annotation.MessageMapping
import org.springframework.messaging.handler.annotation.Payload
import org.springframework.messaging.simp.SimpMessageSendingOperations
import org.springframework.messaging.simp.stomp.StompCommand
import org.springframework.messaging.simp.stomp.StompHeaderAccessor
import org.springframework.security.core.Authentication
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile
import java.security.Principal
import java.util.*


@Controller
@RequestMapping("/api/v1/chat")
class ChatController(
    private val simpMessageSendingOperations: SimpMessageSendingOperations,
    private val userService: UserService,
    private val chatRoomService: ChatRoomService,
    private val rabbitService: RabbitService,
    private val rabbitManageService: RabbitManageService,
    private val minioService: MinioService
) {

    //    private val chatrooms = mutableListOf<ChatRoom>()
    private val logger = LoggerFactory.getLogger(javaClass)

    @GetMapping("contacts")
    fun getContacts(principal: Principal, authentication: Authentication): ResponseEntity<ContactListResponse> {
        val contactList = userService.getAllUsers().map {
            ContactInfoResponse(
                username = it.username,
            )
        }
        return ResponseEntity.ok(
            ContactListResponse(
                contacts = contactList
            )
        )
    }

    @GetMapping("conversations")
    fun getConversations(
        authentication: Authentication): ResponseEntity<ConversationListResponse> {
        val loggedInUser = authentication.principal as UserEntity
        val conversations = rabbitManageService.getConversationsForUser(loggedInUser)
        return ResponseEntity.ok(
            ConversationListResponse(
                conversations = conversations.map{conversation ->
                    ConversationResponse(
                        id = "${conversation.id}",
                        name = conversation.name,
                        contacts = conversation.contacts.map {
                            ContactInfoResponse(
                                userService.loadUserById(it).username
                            )},
                        owner = "${conversation.owner}",
                        type = conversation.type
                    )
                }
            )
        )

    }

    @GetMapping("conversations/{conversationId}")
    fun getConversation(
        authentication: Authentication,
        @PathVariable conversationId: String): ResponseEntity<ConversationListResponse> {
        try {
            val conversation = rabbitManageService.getConversationExchange(UUID.fromString(conversationId))
            return if (conversation != null) {
                ResponseEntity.ok(
                    ConversationListResponse(
                        conversations = listOf(
                            ConversationResponse(
                                id = "${conversation.id}",
                                name = conversation.name,
                                contacts = conversation.contacts.map { ContactInfoResponse(
                                    userService.loadUserById(it).username
                                ) },
                                owner = "${conversation.owner}",
                                type = conversation.type
                            )
                        )
                    )
                )
            } else {
                ResponseEntity.notFound().build()
            }
        } catch (e: IllegalArgumentException){
            return ResponseEntity.badRequest().build()
        }
    }



    @PostMapping("/conversations")
    fun newConversation(
        authentication: Authentication,
        @RequestBody chatRoomRequest: NewChatRequest
    ): ResponseEntity<NewChatResponse> {
        val loggedInUser = authentication.principal as UserEntity
        var roomUUID: UUID = UUID.randomUUID()
        val contactSet = chatRoomRequest.contacts.toSet()
        val contacts = contactSet.map { userService.loadUserByUsername(it).id }.toSet()
        if (chatRoomRequest.type == ChatRoomType.COUPLE) {
            if (contacts.size != 2) {
                return ResponseEntity.unprocessableEntity().build()
            }
            // Prevent users to create couple conversations with others
            if (contacts.indexOf(loggedInUser.id) == -1) {
                return ResponseEntity.unprocessableEntity().build()
            }
            roomUUID = chatRoomService.coupleChatUUID(
                contacts.elementAt(0),
                contacts.elementAt(1)
            )
        }

        val chatRoom = ChatRoom(
            id = roomUUID,
            contacts = contacts,
            owner = loggedInUser.id,
            name = chatRoomRequest.name,
            type = chatRoomRequest.type
        )
        chatRoomService.save(chatRoom)
        rabbitService.createChat(chatRoom)

        return ResponseEntity.ok(
            NewChatResponse(
                id = chatRoom.id.toString(),
                contacts = contactSet.map { ContactInfoResponse(username = it) },
                type = chatRoom.type.toString(),
                owner = chatRoom.owner.toString(),
                name = chatRoom.name
            )
        )
    }

    @DeleteMapping("/conversation")
    fun deleteConversation(authentication: Authentication, @RequestBody infoDelete: DeleteChatRequest) {
        val loggedInUser = authentication.principal as UserEntity
        rabbitService.deleteChat(loggedInUser.getUsername(), infoDelete.id)
    }

    @PutMapping("/conversation/contacts")
    fun addConversationContacts(
        authentication: Authentication,
        @RequestBody updateConversationContactsRequest: UpdateConversationContactsRequest
    ): ResponseEntity<String> {
        val loggedInUser = authentication.principal as UserEntity
        rabbitService.addConversationContacts(
            loggedInUser.getUsername(),
            updateConversationContactsRequest.id,
            updateConversationContactsRequest.contacts
        )
        return ResponseEntity.ok().build()
    }

    @DeleteMapping("/conversation/contacts")
    fun deleteConversationContacts(
        authentication: Authentication,
        @RequestBody updateConversationContactsRequest:
        UpdateConversationContactsRequest
    ): ResponseEntity<String> {
        val loggedInUser = authentication.principal as UserEntity
        rabbitService.deleteConversationContacts(
            loggedInUser.getUsername(),
            updateConversationContactsRequest.id,
            updateConversationContactsRequest.contacts
        )
        return ResponseEntity.ok().build()
    }

    @MessageMapping("/message")
    fun message(
        @Payload draftMessage: ChatMessageRequest,
        authentication: Authentication,
        @Header(StompHeaderAccessor.STOMP_RECEIPT_HEADER) receiptId: String
    ) {
        val loggedInUser = authentication.principal as UserEntity
        val newMessage = ChatMessage(
            id = Generators.timeBasedGenerator().generate(),
            date = draftMessage.date,
            from = UUID.fromString(draftMessage.from),
            to = UUID.fromString(draftMessage.to),
            content =  draftMessage.content,
            media = draftMessage.media
        )

        val receipt = StompHeaderAccessor.create(StompCommand.MESSAGE)
        receipt.receiptId = receiptId
        if (rabbitService.sendMessage(newMessage) ) {
            simpMessageSendingOperations.convertAndSendToUser(
                loggedInUser.username, "/queue/messages",
                newMessage,receipt.messageHeaders)
        } else {
            simpMessageSendingOperations.convertAndSendToUser(
                loggedInUser.username, "/queue/messages",
                intArrayOf(0),receipt.messageHeaders)
        }
    }

    @PostMapping("/conversation/{conversationId}/files")
    fun conversationFileUpload(
        authentication: Authentication,
        @PathVariable conversationId: String,
        @RequestParam("file") file: MultipartFile
    ): ResponseEntity<String> {
        val loggedInUser = authentication.principal as UserEntity
        return if ( rabbitManageService.authorizeSendToGroup(conversationId, loggedInUser.id.toString()) ) {
            minioService.uploadFile(conversationId, file)
            return ResponseEntity.ok().build()
        } else {
            ResponseEntity.status(HttpStatus.FORBIDDEN).build()
        }
    }

    @GetMapping("/conversation/{conversationId}/files/{filename:.+}")
    @ResponseBody
    fun serveFile(
        authentication: Authentication,
        @PathVariable conversationId: String,
        @PathVariable filename: String
    ): ResponseEntity<ByteArray> {
        val loggedInUser = authentication.principal as UserEntity
        return if ( rabbitManageService.authorizeSendToGroup(conversationId, loggedInUser.id.toString()) ) {
            ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(IOUtils.toByteArray(minioService.downloadFile(conversationId, filename)))
        } else{
            ResponseEntity.notFound().build()
        }
    }



}