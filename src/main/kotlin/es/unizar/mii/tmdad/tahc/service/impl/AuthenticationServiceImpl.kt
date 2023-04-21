package es.unizar.mii.tmdad.tahc.service.impl

import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import es.unizar.mii.tmdad.tahc.config.JwtService
import es.unizar.mii.tmdad.tahc.dto.AuthenticationRequest
import es.unizar.mii.tmdad.tahc.dto.AuthenticationResponse
import es.unizar.mii.tmdad.tahc.dto.RegisterRequest
import es.unizar.mii.tmdad.tahc.entity.Role
import es.unizar.mii.tmdad.tahc.entity.UserEntity
import es.unizar.mii.tmdad.tahc.exception.UserNotFoundException
import es.unizar.mii.tmdad.tahc.repository.UserRepository
import es.unizar.mii.tmdad.tahc.service.AuthenticationService
import io.jsonwebtoken.Claims

@Service
class AuthenticationServiceImpl(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder,
    private val jwtService: JwtService,
    private val authenticationManager: AuthenticationManager
) : AuthenticationService {
    override fun register(registerRequest: RegisterRequest): AuthenticationResponse {
        val user = UserEntity(
            username = registerRequest.username,
            password = passwordEncoder.encode(registerRequest.password),
            role = Role.USER
        )

        userRepository.save(user)

        val jwt = jwtService.generateToken(user)

        return AuthenticationResponse(
            accessToken = jwt,
            expiresAt = jwtService.extractClaim(jwt, Claims::getExpiration).time,
            type="Bearer"
        )
    }

    override fun login(authenticationRequest: AuthenticationRequest): AuthenticationResponse {
        authenticationManager.authenticate(
            UsernamePasswordAuthenticationToken(
                authenticationRequest.username,
                authenticationRequest.password
            )
        )

        val user =
            userRepository.findByUsername(authenticationRequest.username) ?: throw UserNotFoundException("User nor found")

        val jwt = jwtService.generateToken(user)
        return AuthenticationResponse(
            accessToken = jwt,
            expiresAt = jwtService.extractClaim(jwt, Claims::getExpiration).time,
            type="Bearer")
    }
}