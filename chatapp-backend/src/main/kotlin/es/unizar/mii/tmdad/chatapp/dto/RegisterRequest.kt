package es.unizar.mii.tmdad.chatapp.dto

import es.unizar.mii.tmdad.auth.validation.PasswordMatches
import es.unizar.mii.tmdad.auth.validation.ValidPassword
import jakarta.validation.constraints.Size
import org.jetbrains.annotations.NotNull

@PasswordMatches
data class RegisterRequest(
    @NotNull
    @Size(min = 1)
    var email: String,
    @Size(min = 1)
    var name: String,
    @Size(min = 1)
    var lastname: String,
//    @ValidPassword
    @NotNull
    @Size(min = 1)
    var password: String,
    @NotNull
    @Size(min = 1)
    val passwordConfirmation: String
)
