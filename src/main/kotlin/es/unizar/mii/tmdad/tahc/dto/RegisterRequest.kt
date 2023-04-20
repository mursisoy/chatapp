package es.unizar.mii.tmdad.tahc.dto

import es.unizar.mii.tmdad.auth.validation.PasswordMatches
import es.unizar.mii.tmdad.auth.validation.ValidPassword
import jakarta.validation.constraints.Size
import org.jetbrains.annotations.NotNull

@PasswordMatches
data class RegisterRequest(
    @NotNull
    @Size(min = 1)
    var username: String,
    @ValidPassword
    var password: String,
    @NotNull
    @Size(min = 1)
    val passwordConfirmation: String
)
