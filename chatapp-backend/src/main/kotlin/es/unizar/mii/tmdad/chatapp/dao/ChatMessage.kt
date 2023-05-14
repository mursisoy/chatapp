package es.unizar.mii.tmdad.chatapp.dao

import com.fasterxml.uuid.Generators
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Temporal
import jakarta.persistence.TemporalType
import java.util.UUID


@Entity
class ChatMessage (
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    val id: UUID = Generators.timeBasedGenerator().generate(),
    @Temporal(TemporalType.TIMESTAMP)
    var date: Long,
    @Column(name="_from")
    val from: UUID,
    @Column(name="_to")
    val to: UUID,
    val content: String,
    val media: String?
) {
    override fun toString(): String {
        return "{date: ${date.toString()}, from: '$from', to: '$to', content: '$content'}"
    }
}