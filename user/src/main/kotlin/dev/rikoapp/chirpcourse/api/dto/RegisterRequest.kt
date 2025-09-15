package dev.rikoapp.chirpcourse.api.dto

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.Pattern
import org.hibernate.validator.constraints.Length

data class RegisterRequest @JsonCreator constructor(
    @field:Length(min = 3, max = 20, message = "Username must be between 3 and 20 characters long")
    @JsonProperty("username")
    val username: String,

    @field:Email(message = "Please provide a valid email address")
    @JsonProperty("email")
    val email: String,

    @field:Pattern(
        regexp = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?])(?=\\S+$).{12,64}$",
        message = "Password must be 12-64 characters long, contain at least one uppercase, lowercase, digit, and special character, and must not include spaces."
    )
    @JsonProperty("password")
    val password: String,
)
