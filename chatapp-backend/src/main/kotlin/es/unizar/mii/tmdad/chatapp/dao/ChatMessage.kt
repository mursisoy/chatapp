package es.unizar.mii.tmdad.chatapp.dao

import com.fasterxml.uuid.Generators
import es.unizar.mii.tmdad.chatapp.dto.ConversationFileUploadResponse
import jakarta.persistence.*
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.type.SqlTypes
import java.util.*


@Entity
class ChatMessage (
    @Id
//    @GeneratedValue(strategy = GenerationType.UUID)
    val id: UUID = Generators.timeBasedGenerator().generate(),
    @Temporal(TemporalType.TIMESTAMP)
    var date: Long,
    @Column(name="_to")
    val to: UUID,
    @Column(columnDefinition="TEXT")
    val content: String,
    @JdbcTypeCode(SqlTypes.JSON)
    val media: ConversationFileUploadResponse?,
    @Column(name="_from_id")
    val from_id: UUID,
    @Column(name="_from")
    val from: String,
) {
    override fun toString(): String {
        return "{date: ${date.toString()}, from: '$from', to: '$to', content: '$content'}"
    }
}