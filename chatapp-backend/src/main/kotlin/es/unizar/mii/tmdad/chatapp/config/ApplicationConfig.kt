package es.unizar.mii.tmdad.chatapp.config

import es.unizar.mii.tmdad.chatapp.dao.Role
import es.unizar.mii.tmdad.chatapp.dao.UserEntity
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.AuthenticationProvider
import org.springframework.security.authentication.dao.DaoAuthenticationProvider
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import es.unizar.mii.tmdad.chatapp.repository.UserRepository
import es.unizar.mii.tmdad.chatapp.service.UserService
import org.springframework.boot.ApplicationRunner
import java.util.*

@Configuration
class ApplicationConfig(
    private val userRepository: UserRepository
) {
    @Bean
    fun userDetailsService(): UserDetailsService {
        return UserDetailsService { username ->
            userRepository.findByUsername(UUID.fromString(username)) ?: throw UsernameNotFoundException("User not found")
        }
    }

    @Bean
    fun authenticationProvider(): AuthenticationProvider {
        val authenticationProvider = DaoAuthenticationProvider()
        authenticationProvider.setUserDetailsService(userDetailsService())
        authenticationProvider.setPasswordEncoder(passwordEncoder())

        return authenticationProvider
    }

    @Bean
    fun authenticationManager(config: AuthenticationConfiguration): AuthenticationManager {
        return config.authenticationManager
    }

    @Bean
    fun passwordEncoder(): PasswordEncoder {
        return BCryptPasswordEncoder()
    }

    @Bean
    fun databaseInitializer(userService: UserService) = ApplicationRunner {

        userService.register(UserEntity(
            email = "admin@chatapp.local",
            password = passwordEncoder().encode("admin"),
            firstName = "Foo",
            lastName = "Bar",
            role = Role.ADMIN
        ))

        for (i in 1..20) {
            userService.register(
                UserEntity(
                    email = "user${i}@chatapp.local",
                    password = passwordEncoder().encode("user${i}"),
                    firstName = "User",
                    lastName = i.toString().padStart(2,'0'),
                    role = Role.USER
                )
            )
        }
    }
}