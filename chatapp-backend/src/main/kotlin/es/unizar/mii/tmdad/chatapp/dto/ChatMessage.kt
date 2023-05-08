package es.unizar.mii.tmdad.chatapp.dto

import org.apache.tomcat.util.http.fileupload.util.mime.MimeUtility
import org.springframework.util.MimeType
import java.util.UUID

data class ChatMessage (
    var timestamp: Long,
    val from: UUID,
    val to: UUID,
    val isGroup: Boolean?,
    val msg: String,
    val media: String?
) {
    override fun toString(): String {
        return String.format(
            "{\"from\": \"%s\", \"to\": \"%s\", \"isGroup\": \"%s\", \"msg\": \"%s\", \"media\": \"%s\"}",
            from, to, isGroup.toString(), msg, media
        )
    }
}