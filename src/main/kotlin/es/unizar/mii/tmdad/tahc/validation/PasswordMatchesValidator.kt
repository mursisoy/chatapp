package es.unizar.mii.tmdad.auth.validation

import es.unizar.mii.tmdad.tahc.dto.RegisterRequest
import jakarta.validation.ConstraintValidator
import jakarta.validation.ConstraintValidatorContext


class PasswordMatchesValidator : ConstraintValidator<PasswordMatches, Any> {
    override fun isValid(value: Any?, context: ConstraintValidatorContext?): Boolean {
        val user = value as RegisterRequest
        return user.password == user.passwordConfirmation
    }
}