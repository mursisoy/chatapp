//package es.unizar.mii.tmdad.chatapp.controller
//
//
//import es.unizar.mii.tmdad.chatapp.dto.*
//import es.unizar.mii.tmdad.chatapp.service.RabbitService
////import org.slf4j.LoggerFactory
////import org.springframework.stereotype.Controller
//import org.springframework.web.bind.annotation.PostMapping
//import org.springframework.web.bind.annotation.RequestBody
//import org.springframework.web.bind.annotation.RestController
//
//
//
//@RestController
//class RabbitController(private val rabbitService: RabbitService) {
//
//    //private val logger = LoggerFactory.getLogger(Controller::class.java)
//
//    @PostMapping("/newChat")
//    fun createNewChat(@RequestBody infoChat: NewChatRequest) {
//        rabbitService.createChat(infoChat.idSala, infoChat.userOfGroup)
//    }
//
//    @PostMapping("/updateChat")
//    fun updateChat(@RequestBody infoUpdate: UpdateChatRequest) {
//        rabbitService.updateChat(infoUpdate.origin, infoUpdate.idSala, infoUpdate.action, infoUpdate.usersAffected)
//    }
//
//    @PostMapping("/deleteChat")
//    fun deleteChat(@RequestBody  infoDelete: DeleteChatRequest) {
//        rabbitService.deleteChat(infoDelete.origin, infoDelete.idSala)
//    }
//}