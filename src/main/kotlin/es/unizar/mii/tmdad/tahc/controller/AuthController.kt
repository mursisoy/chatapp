package es.unizar.mii.tmdad.tahc.controller

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import es.unizar.mii.tmdad.tahc.dto.AuthenticationRequest
import es.unizar.mii.tmdad.tahc.dto.AuthenticationResponse
import es.unizar.mii.tmdad.tahc.dto.RegisterRequest
import es.unizar.mii.tmdad.tahc.exception.UserNotFoundException
import es.unizar.mii.tmdad.tahc.service.AuthenticationService

@RestController
@RequestMapping("/api/v1/auth")
class AuthController(
    private val authenticationService: AuthenticationService
) {

    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler(UserNotFoundException::class)
    fun handleNotFoundException(e: NoSuchElementException): ResponseEntity<Map<String, String?>> {
        return ResponseEntity(mapOf("message" to e.message), HttpStatus.NOT_FOUND)
    }

    @PostMapping("/register")
    fun register(
        @RequestBody registerRequest: RegisterRequest
    ): ResponseEntity<AuthenticationResponse> {
        return ResponseEntity.ok(authenticationService.register(registerRequest))
    }

    @PostMapping("/login")
    fun login(
        @RequestBody authenticationRequest: AuthenticationRequest
    ): ResponseEntity<AuthenticationResponse> {
        return ResponseEntity.ok(authenticationService.login(authenticationRequest))
    }
}