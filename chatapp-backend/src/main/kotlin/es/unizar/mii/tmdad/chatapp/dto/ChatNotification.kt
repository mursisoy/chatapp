package es.unizar.mii.tmdad.chatapp.dto

data class ChatNotification(
    val type: ChatNotificationType,
    val content: Any?,
)

enum class ChatNotificationType {
    ERROR, GROUP
}