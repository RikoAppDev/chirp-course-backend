package dev.rikoapp.chirpcourse.api.dto

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import dev.rikoapp.chirpcourse.api.util.Password

data class ResetPasswordRequest @JsonCreator constructor(
    @JsonProperty("token")
    val token: String,
    @field:Password
    @JsonProperty("newPassword")
    val newPassword: String
)
