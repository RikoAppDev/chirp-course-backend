package dev.rikoapp.chirpcourse.infra.database.mappers

import dev.rikoapp.chirpcourse.domain.model.User
import dev.rikoapp.chirpcourse.infra.database.entities.UserEntity

fun UserEntity.toUser(): User {
    return User(
        id = id!!,
        username = username,
        email = email,
        hasEmailVerified = hasVerifiedEmail
    )
}