package es.unizar.mii.tmdad.chatapp.controller

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import es.unizar.mii.tmdad.chatapp.dto.AuthenticationRequest
import es.unizar.mii.tmdad.chatapp.dto.AuthenticationResponse
import es.unizar.mii.tmdad.chatapp.dto.RegisterRequest
import es.unizar.mii.tmdad.chatapp.dao.Role
import es.unizar.mii.tmdad.chatapp.dao.UserEntity
import es.unizar.mii.tmdad.chatapp.exception.UserNotFoundException
import es.unizar.mii.tmdad.chatapp.service.AuthenticationService
import es.unizar.mii.tmdad.chatapp.service.JwtService
import io.jsonwebtoken.Claims
import jakarta.validation.Valid
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.crypto.password.PasswordEncoder

@RestController
@RequestMapping("/api/v1/auth")
class AuthController(
    private val authenticationService: AuthenticationService,
    private val authenticationManager: AuthenticationManager,
    private val passwordEncoder: PasswordEncoder,
    private val jwtService: JwtService
) {

    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler(UserNotFoundException::class)
    fun handleNotFoundException(e: NoSuchElementException): ResponseEntity<Map<String, String?>> {
        return ResponseEntity(mapOf("message" to e.message), HttpStatus.NOT_FOUND)
    }

    @PostMapping("/register")
    fun register(
        @Valid @RequestBody registerRequest: RegisterRequest
    ): ResponseEntity<AuthenticationResponse> {
        val user = UserEntity(
            username = registerRequest.email,
            password = passwordEncoder.encode(registerRequest.password),
            role = Role.USER
        )

        val registeredUser = authenticationService.register(user)

        val jwt = jwtService.generateToken(registeredUser)

        val response = AuthenticationResponse(
            accessToken = jwt,
            expiresAt = jwtService.extractClaim(jwt, Claims::getExpiration).time,
            type="Bearer"
        )
        return ResponseEntity.ok(response)
    }

    @PostMapping("/login")
    fun login(
        @RequestBody authenticationRequest: AuthenticationRequest
    ): ResponseEntity<AuthenticationResponse> {
        val user = authenticationService.loadUserByUsername(authenticationRequest.email)
        val authentication: Authentication = authenticationManager.authenticate(
            UsernamePasswordAuthenticationToken(
                user,
                authenticationRequest.password
            )
        )

        val jwt = jwtService.generateToken(authentication.principal as UserEntity)

        return ResponseEntity.ok(AuthenticationResponse(
            accessToken = jwt,
            expiresAt = jwtService.extractClaim(jwt, Claims::getExpiration).time,
            type="Bearer"))


    }
}