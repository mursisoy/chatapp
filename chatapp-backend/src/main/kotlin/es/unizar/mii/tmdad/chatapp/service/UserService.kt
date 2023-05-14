package es.unizar.mii.tmdad.chatapp.service

import org.springframework.stereotype.Service
import es.unizar.mii.tmdad.chatapp.dao.UserEntity
import es.unizar.mii.tmdad.chatapp.exception.UserNotFoundException
import es.unizar.mii.tmdad.chatapp.repository.UserRepository
import java.util.UUID

@Service
class UserService(
    private val userRepository: UserRepository,
    private val rabbitService: RabbitService
)  {
    fun register(user: UserEntity): UserEntity {
        // Insert the user
        val newUser =  userRepository.save(user)
        rabbitService.registRabbit(newUser.id)
        return newUser
    }

    fun getAllUsers(): List<UserEntity>{
        return  userRepository.findAll()
    }

    fun loadUserByUsername(username: String): UserEntity{
        return userRepository.findByUsername(username) ?: throw UserNotFoundException("User nor found")
    }
    fun loadUserById(id: UUID): UserEntity{
        return userRepository.findById(id) ?: throw UserNotFoundException("User nor found")
    }

}