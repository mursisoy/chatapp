package es.unizar.mii.tmdad.auth.validation

import jakarta.validation.ConstraintValidator
import jakarta.validation.ConstraintValidatorContext
import org.passay.*


class PasswordConstraintValidator : ConstraintValidator<ValidPassword, String?> {
    override fun isValid(password: String?, context: ConstraintValidatorContext): Boolean {
        // @formatter:off
        val validator = PasswordValidator(
            listOf(
                LengthRule(8, 30),
                CharacterRule(EnglishCharacterData.UpperCase,1),
                CharacterRule(EnglishCharacterData.LowerCase, 1),
                CharacterRule(EnglishCharacterData.Digit, 1),
                CharacterRule(EnglishCharacterData.Special, 1)
            )
        )
        val result: RuleResult = validator.validate(PasswordData(password))
        if (result.isValid) {
            return true
        }
        context.disableDefaultConstraintViolation()
        context.buildConstraintViolationWithTemplate(validator.getMessages(result).joinToString(","))
            .addConstraintViolation()
        return false
    }
}