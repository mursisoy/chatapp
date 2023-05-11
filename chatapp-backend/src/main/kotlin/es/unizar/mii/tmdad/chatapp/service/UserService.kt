package es.unizar.mii.tmdad.chatapp.service

import org.springframework.stereotype.Service
import es.unizar.mii.tmdad.chatapp.dao.UserEntity
import es.unizar.mii.tmdad.chatapp.exception.UserNotFoundException
import es.unizar.mii.tmdad.chatapp.repository.UserRepository

@Service
class UserService(
    private val userRepository: UserRepository,
//    private val channel: Channel
)  {
    fun register(user: UserEntity): UserEntity {
        // Insert the user
        val newUser = userRepository.save(user)

        return newUser
    }

    fun getAllUsers(): List<UserEntity>{
        return  userRepository.findAll()
    }

    fun loadUserByUsername(username: String): UserEntity{
        return userRepository.findByUsername(username) ?: throw UserNotFoundException("User nor found")
    }

}