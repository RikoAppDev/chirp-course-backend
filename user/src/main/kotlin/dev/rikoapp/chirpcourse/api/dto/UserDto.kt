package dev.rikoapp.chirpcourse.api.dto

import dev.rikoapp.chirpcourse.domain.model.UserId

data class UserDto(
    val id: UserId,
    val email: String,
    val username: String,
    val hasVerifiedEmail: Boolean
)
