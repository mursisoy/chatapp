package es.unizar.mii.tmdad.chatapp.dao

import com.fasterxml.uuid.Generators
import jakarta.persistence.*
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
    val content: String,
    val media: String?,
    @Column(name="_from_id")
    val from_id: UUID,
    @Column(name="_from")
    val from: String,
) {
    override fun toString(): String {
        return "{date: ${date.toString()}, from: '$from', to: '$to', content: '$content'}"
    }
}