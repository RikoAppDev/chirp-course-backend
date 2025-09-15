package dev.rikoapp.chirpcourse.api.mappers

import dev.rikoapp.chirpcourse.api.dto.AuthenticatedUserDto
import dev.rikoapp.chirpcourse.api.dto.UserDto
import dev.rikoapp.chirpcourse.domain.model.AuthenticatedUser
import dev.rikoapp.chirpcourse.domain.model.User

fun AuthenticatedUser.toAuthenticatedUserDto(): AuthenticatedUserDto {
    return AuthenticatedUserDto(
        user = user.toUserDto(),
        accessToken = accessToken,
        refreshToken = refreshToken
    )
}

fun User.toUserDto(): UserDto {
    return UserDto(
        id = id,
        email = email,
        username = username,
        hasVerifiedEmail = hasEmailVerified
    )
}