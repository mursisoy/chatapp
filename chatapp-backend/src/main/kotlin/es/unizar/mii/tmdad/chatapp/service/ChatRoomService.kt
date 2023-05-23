package es.unizar.mii.tmdad.chatapp.service
import org.springframework.stereotype.Service
import java.nio.ByteBuffer
import java.util.*

@Service
class ChatRoomService (){
    private fun convertUUIDToBytes(uuid: UUID): ByteArray? {
        val bb = ByteBuffer.wrap(ByteArray(16))
        bb.putLong(uuid.mostSignificantBits)
        bb.putLong(uuid.leastSignificantBits)
        return bb.array()
    }
    fun coupleChatUUID(u1: UUID, u2: UUID): UUID{
        val r1 = convertUUIDToBytes(u1)
        val r2 = convertUUIDToBytes(u2)

        val r3 = r1?.mapIndexed { i,v ->
            when {
                (i == 6) -> (((v.toInt() xor r2!![i].toInt()) and 0x0F) or 0x40).toByte()
                (i == 8) -> (((v.toInt() xor r2!![i].toInt()) and 0x3F) or 0x80).toByte()
                else ->     (v.toInt() xor r2!![i].toInt()).toByte()
            }
        }?.toByteArray()
        val byteBuffer: ByteBuffer = ByteBuffer.wrap(r3)
        val high: Long = byteBuffer.long
        val low: Long = byteBuffer.long

        return UUID(high,low)
    }

//    fun save(chatRoom: ChatRoom): ChatRoom {
//        return chatRoomRepository.save(chatRoom)
//    }

}