package es.unizar.mii.tmdad.tahc.repository

import org.springframework.data.jpa.repository.JpaRepository
import es.unizar.mii.tmdad.tahc.entity.UserEntity

interface UserRepository: JpaRepository<UserEntity, Int> {
//    fun findByEmail(email: String): UserEntity?
    fun findByUsername(username: String): UserEntity?
}