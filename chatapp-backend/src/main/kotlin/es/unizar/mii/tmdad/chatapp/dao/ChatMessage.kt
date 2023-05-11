package es.unizar.mii.tmdad.chatapp.dao

import es.unizar.mii.tmdad.chatapp.dao.ChatRoomType
import es.unizar.mii.tmdad.chatapp.dao.UserEntity
import org.apache.tomcat.util.http.fileupload.util.mime.MimeUtility
import org.springframework.util.MimeType
import java.util.UUID

data class ChatMessage (
    var date: Long,
    val from: String,
    val to: String,
    val isGroup: ChatRoomType?,
    val content: String,
    val media: String?
) {
    override fun toString(): String {
        return "{date: ${date.toString()}, from: '$from', to: '$to', isGroup: ${isGroup.toString()}, content: '$content'}"
    }
}