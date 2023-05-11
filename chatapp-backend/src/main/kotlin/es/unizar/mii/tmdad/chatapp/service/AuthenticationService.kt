package es.unizar.mii.tmdad.chatapp.service


import org.springframework.stereotype.Service
import es.unizar.mii.tmdad.chatapp.dao.UserEntity
import es.unizar.mii.tmdad.chatapp.exception.UserNotFoundException
import es.unizar.mii.tmdad.chatapp.repository.UserRepository
import java.util.UUID


@Service
class AuthenticationService(
    private val userRepository: UserRepository
)  {
    fun register(user: UserEntity): UserEntity {
        // Insert the user
        val newUser = userRepository.save(user)
        return newUser
    }

    fun loadUserByUsername(username: UUID): UserEntity{
        return userRepository.findByUsername(username) ?: throw UserNotFoundException("User nor found")
    }

}