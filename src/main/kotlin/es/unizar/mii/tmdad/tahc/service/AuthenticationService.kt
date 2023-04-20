package es.unizar.mii.tmdad.tahc.service

import org.springframework.stereotype.Service
import es.unizar.mii.tmdad.tahc.dto.AuthenticationRequest
import es.unizar.mii.tmdad.tahc.dto.AuthenticationResponse
import es.unizar.mii.tmdad.tahc.dto.RegisterRequest

@Service
interface AuthenticationService {
    fun register(registerRequest: RegisterRequest): AuthenticationResponse
    fun login(authenticationRequest: AuthenticationRequest): AuthenticationResponse
}