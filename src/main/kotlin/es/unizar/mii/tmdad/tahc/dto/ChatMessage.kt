package es.unizar.mii.tmdad.tahc.dto

data class ChatMessage(
    var content: String?,
    var from: String,
    var type: MessageType,
    var to: String,
)

enum class MessageType {
    CHAT, JOIN, LEAVE, GROUP
}