package dev.rikoapp.chirpcourse.infra.database.mappers

import dev.rikoapp.chirpcourse.domain.model.EmailVerificationToken
import dev.rikoapp.chirpcourse.infra.database.entities.EmailVerificationTokenEntity

fun EmailVerificationTokenEntity.toEmailVerificationToken(): EmailVerificationToken {
    return EmailVerificationToken(
        id = id,
        token = token,
        user = user.toUser()
    )
}