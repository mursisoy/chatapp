package es.unizar.mii.tmdad.chatapp.controller

import com.fasterxml.uuid.Generators
import es.unizar.mii.tmdad.chatapp.dao.ChatMessage
import es.unizar.mii.tmdad.chatapp.dao.ChatRoom
import es.unizar.mii.tmdad.chatapp.dao.ChatRoomType
import es.unizar.mii.tmdad.chatapp.dao.UserEntity
import es.unizar.mii.tmdad.chatapp.dto.*
import es.unizar.mii.tmdad.chatapp.repository.MessageRepository
import es.unizar.mii.tmdad.chatapp.service.*
import org.apache.commons.io.IOUtils
import org.slf4j.LoggerFactory
import org.springframework.http.HttpHeaders
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
    private val rns: RabbitNamingService,
    private val minioService: MinioService,
    private val messageRepository: MessageRepository
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
        @PathVariable conversationId: UUID): ResponseEntity<ConversationListResponse> {
        try {
            val conversation = rabbitManageService.getConversationExchange(conversationId)
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



    @PostMapping("conversations")
    fun newConversation(
        authentication: Authentication,
        @RequestBody chatRoomRequest: NewChatRequest
    ): ResponseEntity<NewChatResponse> {
        val loggedInUser = authentication.principal as UserEntity
        var roomUUID: UUID = UUID.randomUUID()
        var owner: UUID? = loggedInUser.id
        val contactSet = chatRoomRequest.contacts.toSet()
        val contacts = contactSet.map { userService.loadUserByUsername(it).id }.toSet()

        if (chatRoomRequest.type == ChatRoomType.BROADCAST) {
            return ResponseEntity.badRequest().build()
        } else if (chatRoomRequest.type == ChatRoomType.COUPLE) {
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
            owner = null
        }

        val chatRoom = ChatRoom(
            id = roomUUID,
            contacts = contacts,
            owner = owner,
            name = chatRoomRequest.name,
            type = chatRoomRequest.type.name
        )

        chatRoomService.save(chatRoom)
        rabbitService.createChat(chatRoom)

        return ResponseEntity.ok(
            NewChatResponse(
                id = chatRoom.id.toString(),
                contacts = contactSet.map { ContactInfoResponse(username = it) },
                type = chatRoom.type,
                owner = chatRoom.owner.toString(),
                name = chatRoom.name
            )
        )
    }

    @DeleteMapping("conversations/{conversationId}")
    fun deleteConversation(
        authentication: Authentication,
        @RequestBody infoDelete: DeleteChatRequest,
        @PathVariable conversationId: UUID) {
        val loggedInUser = authentication.principal as UserEntity
        rabbitService.deleteChat(loggedInUser.id, conversationId)
    }

    @PatchMapping("/conversation/contacts")
    fun addConversationContacts(
        authentication: Authentication,
        @RequestBody updateConversationContactsRequest: UpdateConversationContactsRequest
    ): ResponseEntity<String> {
        val loggedInUser = authentication.principal as UserEntity
        rabbitService.addConversationContacts(
            loggedInUser.id,
            updateConversationContactsRequest.id,
            updateConversationContactsRequest.addContacts
        )
        rabbitService.deleteConversationContacts(
            loggedInUser.id,
            updateConversationContactsRequest.id,
            updateConversationContactsRequest.removeContacts
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
        val conversationId = UUID.fromString(draftMessage.to)
        val newMessage = ChatMessage(
            id = Generators.timeBasedGenerator().generate(),
            date = draftMessage.date,
            from_id = loggedInUser.id,
            from = loggedInUser.username,
            to = conversationId,
            content =  draftMessage.content,
            media = draftMessage.media
        )


        if ((   conversationId != rns.BROADCAST_QUEUE_ID &&
                rabbitManageService.authorizeSendToGroup(
                    rns.getConversationExchangeName(conversationId),
                    rns.getUserExchangeName(loggedInUser.id) ) ||
                ( conversationId == rns.BROADCAST_QUEUE_ID &&
                  authentication.authorities.stream().anyMatch { a-> a.authority.equals("ADMIN")} )

        )) {
            // Use StompCommand.RECEIPT if SimpleStompBroker were compatible :(
//            val headers = StompHeaderAccessor.create(StompCommand.MESSAGE)
//            headers.receiptId = receiptId
            messageRepository.save(newMessage)
            rabbitService.sendMessage(newMessage)
//            if (rabbitService.sendMessage(newMessage)) {
//
//                simpMessageSendingOperations.convertAndSendToUser(
//                    loggedInUser.username, "/queue/notifications",
//                    newMessage, receipt.messageHeaders
//                )
//            } else {
//                simpMessageSendingOperations.convertAndSendToUser(
//                    loggedInUser.username, "/queue/notifications",
//                    intArrayOf(0), receipt.messageHeaders
//                )
//            }
        } else {
            val headers = StompHeaderAccessor.create(StompCommand.MESSAGE,
                mapOf("status-code" to listOf("401")))
            headers.receiptId = receiptId
            simpMessageSendingOperations.convertAndSendToUser(
                loggedInUser.username, "/queue/notifications",
                ChatNotification(
                    type = ChatNotificationType.ERROR,
                    content = "Authorization denied"
                ), headers.messageHeaders
            )
        }
    }

    @PostMapping("/conversation/{conversationId}/files")
    fun conversationFileUpload(
        authentication: Authentication,
        @PathVariable conversationId: UUID,
        @RequestParam("file") file: MultipartFile
    ): ResponseEntity<ConversationFileUploadResponse?> {
        val loggedInUser = authentication.principal as UserEntity
        return if ( rabbitManageService.authorizeSendToGroup(
                rns.getConversationExchangeName(conversationId),
                rns.getUserExchangeName(loggedInUser.id) ) ) {
            val fileId = minioService.uploadFile(file, conversationId.toString())
            if (fileId != null) {
                ResponseEntity.ok(
                    ConversationFileUploadResponse(
                        id = fileId,
                        name= file.originalFilename,
                        size = file.size,
                        type = file.contentType
                    )
                )
            } else {
                ResponseEntity.status(HttpStatus.FORBIDDEN).build()
            }
        } else {
            ResponseEntity.status(HttpStatus.FORBIDDEN).build()
        }
    }

    @GetMapping("/conversation/{conversationId}/files/{fileId}")
    @ResponseBody
    fun serveFile(
        authentication: Authentication,
        @PathVariable conversationId: UUID,
        @PathVariable fileId: UUID
    ): ResponseEntity<ByteArray> {
        val loggedInUser = authentication.principal as UserEntity
        return if ( rabbitManageService.authorizeSendToGroup(
                rns.getConversationExchangeName(conversationId),
                rns.getUserExchangeName(loggedInUser.id) ) ) {
            try {

                val statObject = minioService.statFile(conversationId.toString(), fileId.toString())
                val userMetadata = statObject.userMetadata()
                val filename = userMetadata["filename"]

                val response = ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                if (filename != null ) {
                    response.header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=$filename")
                }
                response.body(IOUtils.toByteArray(minioService.downloadFile(conversationId.toString(), fileId.toString())))
            } catch (e: Exception){
                logger.debug(e.stackTraceToString())
                ResponseEntity.status(HttpStatus.FORBIDDEN).build()
            }
        } else{
            ResponseEntity.status(HttpStatus.FORBIDDEN).build()
        }
    }



}