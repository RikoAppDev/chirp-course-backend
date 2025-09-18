package dev.rikoapp.chirpcourse.api.util

import jakarta.validation.Constraint
import jakarta.validation.Payload
import jakarta.validation.constraints.Pattern
import kotlin.reflect.KClass

@Target(AnnotationTarget.FIELD, AnnotationTarget.PROPERTY_GETTER)
@Retention(AnnotationRetention.RUNTIME)
@Constraint(validatedBy = [])
@Pattern(
    regexp = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?])(?=\\S+$).{12,64}$",
    message = "Password must be 12-64 characters long, contain at least one uppercase, lowercase, digit, and special character, and must not include spaces."
)
annotation class Password(
    val message: String = "Password must be 12-64 characters long, contain at least one uppercase, lowercase, digit, and special character, and must not include spaces.",
    val groups: Array<KClass<out Any>> = [],
    val payload: Array<KClass<out Payload>> = []
)
