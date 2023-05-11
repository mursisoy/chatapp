package es.unizar.mii.tmdad.chatapp.repository

import org.springframework.data.jpa.repository.JpaRepository
import es.unizar.mii.tmdad.chatapp.dao.UserEntity
import java.util.UUID

interface UserRepository: JpaRepository<UserEntity, Int> {
//    fun findByEmail(email: String): UserEntity?
    fun findByEmail(email: String): UserEntity?
    fun findByUsername(username: UUID): UserEntity?
}