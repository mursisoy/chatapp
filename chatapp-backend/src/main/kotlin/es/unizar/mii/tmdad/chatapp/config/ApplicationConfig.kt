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
import es.unizar.mii.tmdad.chatapp.service.AuthenticationService
import org.springframework.boot.ApplicationRunner

@Configuration
class ApplicationConfig(
    private val userRepository: UserRepository
) {
    @Bean
    fun userDetailsService(): UserDetailsService {
        return UserDetailsService { username ->
            userRepository.findByUsername(username) ?: throw UsernameNotFoundException("User not found")
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
    fun databaseInitializer(authenticationService: AuthenticationService) = ApplicationRunner {

        authenticationService.register(UserEntity(
            username = "admin@chatapp.local",
            password = passwordEncoder().encode("admin"),
            name = "Foo",
            lastname = "Bar",
            role = Role.ADMIN
        ))

        for (i in 1..99) {
            authenticationService.register(
                UserEntity(
                    username = "user${i}@chatapp.local",
                    password = passwordEncoder().encode("user${i}"),
                    name = "User",
                    lastname = i.toString().padStart(2,'0'),
                    role = Role.USER
                )
            )
        }
    }
}