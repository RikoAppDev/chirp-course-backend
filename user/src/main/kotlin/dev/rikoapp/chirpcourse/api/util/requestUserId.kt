package dev.rikoapp.chirpcourse.api.util

import dev.rikoapp.chirpcourse.domain.exception.UnauthorizedException
import dev.rikoapp.chirpcourse.domain.model.UserId
import org.springframework.security.core.context.SecurityContextHolder

val requestUserId: UserId
    get() = SecurityContextHolder.getContext().authentication?.principal as? UserId
        ?: throw UnauthorizedException()