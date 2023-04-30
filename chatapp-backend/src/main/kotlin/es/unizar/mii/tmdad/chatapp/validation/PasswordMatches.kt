package es.unizar.mii.tmdad.auth.validation

import jakarta.validation.Constraint
import jakarta.validation.Payload
import kotlin.reflect.KClass

@MustBeDocumented
@Constraint(validatedBy = [PasswordMatchesValidator::class])
@Target(AnnotationTarget.TYPE, AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class PasswordMatches (
    val message: String = "Sorry, passwords does not match",
    val groups: Array<KClass<*>> = [],
    val payload: Array<KClass<out Payload>> = []
    )