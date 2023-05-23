package es.unizar.mii.tmdad.chatapp.controller

import com.fasterxml.uuid.Generators
import es.unizar.mii.tmdad.chatapp.dao.ChatMessage
import es.unizar.mii.tmdad.chatapp.dao.ChatRoom
import es.unizar.mii.tmdad.chatapp.dao.ChatRoomType
import es.unizar.mii.tmdad.chatapp.dao.UserEntity
import es.unizar.mii.tmdad.chatapp.dto.*
import es.unizar.mii.tmdad.chatapp.exception.ChatAuthorizationException
import es.unizar.mii.tmdad.chatapp.repository.MessageRepository
import es.unizar.mii.tmdad.chatapp.service.*
import org.apache.commons.io.IOUtils
import org.apache.tomcat.util.http.fileupload.impl.SizeLimitExceededException
import org.slf4j.LoggerFactory
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.messaging.handler.annotation.Header
import org.springframework.messaging.handler.annotation.MessageMapping
import org.springframework.messaging.handler.annotation.Payload
import org.springframework.messaging.simp.SimpMessageSendingOperations
import org.springframework.messaging.simp.stomp.StompCommand
import org.springframework.messaging.simp.stomp.StompHeaderAccessor
import org.springframework.security.core.Authentication
import org.springframework.stereotype.Controller
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.*
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException
import org.springframework.web.multipart.MultipartFile
import java.security.Principal
import java.util.*


@Controller
@ControllerAdvice
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
    @ExceptionHandler(SizeLimitExceededException::class)
    @ResponseStatus(value = HttpStatus.PAYLOAD_TOO_LARGE)
    fun handleSizeSizeLimitExceededException(e: SizeLimitExceededException): ResponseEntity<Map<String, String?>> {
        return ResponseEntity(mapOf("message" to e.message), HttpStatus.PAYLOAD_TOO_LARGE)
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleSQLException(e: MethodArgumentNotValidException): ResponseEntity<Map<String, String?>> {
        return ResponseEntity(mapOf("message" to e.message), HttpStatus.BAD_REQUEST)
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(MethodArgumentTypeMismatchException::class)
    fun handleSQLException(e: MethodArgumentTypeMismatchException): ResponseEntity<Map<String, String?>> {
        return ResponseEntity(mapOf("message" to e.message), HttpStatus.BAD_REQUEST)
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(HttpMessageNotReadableException::class)
    fun handleSQLException(e: HttpMessageNotReadableException): ResponseEntity<Map<String, String?>> {
        return ResponseEntity(mapOf("message" to e.message), HttpStatus.BAD_REQUEST)
    }


    @GetMapping("contacts")
    fun getContacts(principal: Principal, authentication: Authentication): ResponseEntity<ContactListResponse> {
        val contactList = userService.getAllUsers().map {
            ContactInfoResponse(
                id = it.id.toString(),
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
        authentication: Authentication
    ): ResponseEntity<ConversationListResponse> {
        val loggedInUser = authentication.principal as UserEntity
        val conversations = rabbitManageService.getConversationsForUser(loggedInUser)
        return ResponseEntity.ok(
            ConversationListResponse(
                conversations = conversations.map { conversation ->
                    ConversationResponse(
                        id = "${conversation.id}",
                        name = conversation.name,
                        contacts = conversation.contacts.map { contactId ->
                            userService.loadUserById(contactId).let {
                                ContactInfoResponse(
                                    username = it.username,
                                    id = it.id.toString()
                                )
                            }
                        },
                        messages = messageRepository.findByTo(conversation.id).map {
                            ChatMessageResponse(
                                id = it.id,
                                date = it.date,
                                to = it.to,
                                from = it.from,
                                content = it.content,
                                media = it.media?.let { media -> ConversationFileUploadResponse(
                                    id = media.id,
                                    size = media.size,
                                    type = media.type,
                                    name = media.name,
                                )}
                            )
                        },
                        owner = conversation.owner?.let { owner ->
                            userService.loadUserById(owner).let {
                                ContactInfoResponse(
                                    username = it.username,
                                    id = it.id.toString()
                                )
                            }
                        },
                        type = conversation.type
                    )
                }
            )
        )

    }

    @GetMapping("conversations/{conversationId}")
    fun getConversation(
        authentication: Authentication,
        @PathVariable conversationId: UUID
    ): ResponseEntity<ConversationListResponse> {
        try {
            val conversation = rabbitManageService.getConversationExchange(conversationId)
            return if (conversation != null) {
                ResponseEntity.ok(
                    ConversationListResponse(
                        conversations = listOf(
                            ConversationResponse(
                                id = "${conversation.id}",
                                name = conversation.name,
                                contacts = conversation.contacts.map { contactId ->
                                    userService.loadUserById(contactId).let {
                                        ContactInfoResponse(
                                            username = it.username,
                                            id = it.id.toString()
                                        )
                                    }
                                },
                                messages = messageRepository.findByTo(conversation.id).map {
                                    ChatMessageResponse(
                                        id = it.id,
                                        date = it.date,
                                        to = it.to,
                                        content = it.content,
                                        from = it.from,
                                        media = it.media?.let { media -> ConversationFileUploadResponse(
                                            id = media.id,
                                            size = media.size,
                                            type = media.type,
                                            name = media.name,
                                        )}
                                    )
                                },
                                owner = conversation.owner?.let { owner ->
                                    userService.loadUserById(owner).let {
                                        ContactInfoResponse(
                                            username = it.username,
                                            id = it.id.toString()
                                        )
                                    }
                                },
                                type = conversation.type
                            )
                        )
                    )
                )
            } else {
                ResponseEntity.notFound().build()
            }
        } catch (e: IllegalArgumentException) {
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
        val contacts = contactSet.map { userService.loadUserById(it) }.toSet()

        if (chatRoomRequest.type == ChatRoomType.BROADCAST) {
            return ResponseEntity.status(HttpStatus.I_AM_A_TEAPOT).build()
        } else if (chatRoomRequest.type == ChatRoomType.COUPLE) {
            if (contacts.size != 2) {
                return ResponseEntity.unprocessableEntity().build()
            }
            // Prevent users to create couple conversations with others
            if (contactSet.indexOf(loggedInUser.id) == -1) {
                return ResponseEntity.unprocessableEntity().build()
            }
            roomUUID = chatRoomService.coupleChatUUID(
                contacts.elementAt(0).id,
                contacts.elementAt(1).id
            )
            owner = null
        }

        val chatRoom = ChatRoom(
            id = roomUUID,
            contacts = contactSet,
            owner = owner,
            name = chatRoomRequest.name,
            type = chatRoomRequest.type.name
        )

//        chatRoomService.save(chatRoom)
        rabbitService.createChat(chatRoom)

        return ResponseEntity.ok(
            NewChatResponse(
                id = chatRoom.id.toString(),
                contacts = contacts.map { ContactInfoResponse(
                    id = it.id.toString(),
                    username = it.username
                ) },
                type = chatRoom.type,
                owner = chatRoom.owner?.let { ownerId ->
                    userService.loadUserById(ownerId).let {
                        ContactInfoResponse(
                            username = it.username,
                            id = it.id.toString()
                        )
                    }
                },
                name = chatRoom.name
            )
        )
    }

    @DeleteMapping("conversations/{conversationId}")
    fun deleteConversation(
        authentication: Authentication,
        @PathVariable conversationId: UUID
    ): ResponseEntity<*> {
        val loggedInUser = authentication.principal as UserEntity
        return try {
            rabbitService.deleteConversation(loggedInUser.id, conversationId)
            messageRepository.deleteByTo(conversationId)
            ResponseEntity.ok().build<String>()
        } catch (e: ChatAuthorizationException){
            ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(
                    ErrorResponse(
                        status= HttpStatus.FORBIDDEN.value(),
                        title= e.message))
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(e.message)
        }
    }

    @PatchMapping("/conversations/{conversationId}/contacts")
    fun addConversationContacts(
        authentication: Authentication,
        @PathVariable conversationId: UUID,
        @RequestBody updateConversationContactsRequest: UpdateConversationContactsRequest
    ): ResponseEntity<ConversationResponse> {
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
        val conversation = rabbitManageService.getConversationExchange(conversationId)

        return if (conversation != null) {
            ResponseEntity.ok(
                ConversationResponse(
                    id = "${conversation.id}",
                    name = conversation.name,
                    contacts = conversation.contacts.map { contactId ->
                        userService.loadUserById(contactId).let {
                            ContactInfoResponse(
                                username = it.username,
                                id = it.id.toString()
                            )
                        }
                    },
                    owner = conversation.owner?.let { owner ->
                        userService.loadUserById(owner).let {
                            ContactInfoResponse(
                                username = it.username,
                                id = it.id.toString()
                            )
                        }
                    },
                    messages = null,
                    type = conversation.type
                )
            )
        } else {
            ResponseEntity.status(HttpStatus.BAD_REQUEST).build()
        }
    }


    @MessageMapping("/message")
    fun message(
        @Payload draftMessage: ChatMessageRequest,
        authentication: Authentication,
        @Header(StompHeaderAccessor.STOMP_RECEIPT_HEADER) receiptId: String
    ) {
        val loggedInUser = authentication.principal as UserEntity
        val conversationId = draftMessage.to
        val newMessage = ChatMessage(
            id = Generators.timeBasedGenerator().generate(),
            date = draftMessage.date,
            from_id = loggedInUser.id,
            from = loggedInUser.username,
            to = conversationId,
            content = draftMessage.content,
            media = draftMessage.media
        )

        val isBroadcast = conversationId == rns.BROADCAST_QUEUE_ID
        val isAuthorized = rabbitManageService.authorizeSendToGroup(
                                rns.getConversationExchangeName(conversationId),
                                rns.getUserExchangeName(loggedInUser.id)
                            )
        val isAdmin = authentication.authorities.stream().anyMatch { a -> a.authority.equals("ADMIN") }

        if (draftMessage.content.length > 500) {
            val headers = StompHeaderAccessor.create(
                StompCommand.MESSAGE,
                mapOf("status-code" to listOf(HttpStatus.BAD_REQUEST.value().toString()))
            )
            headers.receiptId = receiptId
            simpMessageSendingOperations.convertAndSendToUser(
                loggedInUser.username, "/queue/notifications",
                ChatNotification(
                    type = ChatNotificationType.ERROR,
                    content = if (draftMessage.content.length < 2) "Message tooshort" else "Message too long"
                ), headers.messageHeaders
            )
            return
        }


        if (isBroadcast && newMessage.media != null) {
            val headers = StompHeaderAccessor.create(
                StompCommand.MESSAGE,
                mapOf("status-code" to listOf(HttpStatus.BAD_REQUEST.value().toString()))
            )
            headers.receiptId = receiptId
            simpMessageSendingOperations.convertAndSendToUser(
                loggedInUser.username, "/queue/notifications",
                ChatNotification(
                    type = ChatNotificationType.ERROR,
                    content = "Cannot send media to this group"
                ), headers.messageHeaders
            )
            return
        }

        if ((!isBroadcast && isAuthorized) ||
            (isBroadcast  && isAdmin)) {
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
            val headers = StompHeaderAccessor.create(
                StompCommand.MESSAGE,
                mapOf("status-code" to listOf("401"))
            )
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

    @PostMapping("/conversations/{conversationId}/files")
    fun conversationFileUpload(
        authentication: Authentication,
        @PathVariable conversationId: UUID,
        @RequestParam("file") file: MultipartFile
    ): ResponseEntity<ConversationFileUploadResponse?> {
        val loggedInUser = authentication.principal as UserEntity
        val isBroadcast = conversationId == rns.BROADCAST_QUEUE_ID
        return if (!isBroadcast && rabbitManageService.authorizeSendToGroup(
                rns.getConversationExchangeName(conversationId),
                rns.getUserExchangeName(loggedInUser.id)
            )
        ) {
            val fileId = minioService.uploadFile(file, conversationId.toString())
            if (fileId != null) {
                ResponseEntity.ok(
                    ConversationFileUploadResponse(
                        id = fileId,
                        name = file.originalFilename,
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

    @GetMapping("/conversations/{conversationId}/files/{fileId}")
    @ResponseBody
    fun serveFile(
        authentication: Authentication,
        @PathVariable conversationId: UUID,
        @PathVariable fileId: UUID
    ): ResponseEntity<ByteArray> {
        val loggedInUser = authentication.principal as UserEntity

        return if (rabbitManageService.authorizeSendToGroup(
                rns.getConversationExchangeName(conversationId),
                rns.getUserExchangeName(loggedInUser.id)
            )
        ) {
            try {

                val statObject = minioService.statFile(conversationId.toString(), fileId.toString())
                val userMetadata = statObject.userMetadata()
                val filename = userMetadata["filename"]

                val response = ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                if (filename != null) {
                    response.header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=$filename")
                }
                response.body(
                    IOUtils.toByteArray(
                        minioService.downloadFile(
                            conversationId.toString(),
                            fileId.toString()
                        )
                    )
                )
            } catch (e: Exception) {
                logger.debug(e.stackTraceToString())
                ResponseEntity.status(HttpStatus.FORBIDDEN).build()
            }
        } else {
            ResponseEntity.status(HttpStatus.FORBIDDEN).build()
        }
    }


}