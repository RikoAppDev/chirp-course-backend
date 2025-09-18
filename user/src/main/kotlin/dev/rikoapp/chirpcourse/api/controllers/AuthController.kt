package dev.rikoapp.chirpcourse.api.controllers

import dev.rikoapp.chirpcourse.api.dto.AuthenticatedUserDto
import dev.rikoapp.chirpcourse.api.dto.LoginRequest
import dev.rikoapp.chirpcourse.api.dto.RegisterRequest
import dev.rikoapp.chirpcourse.api.dto.UserDto
import dev.rikoapp.chirpcourse.api.mappers.toAuthenticatedUserDto
import dev.rikoapp.chirpcourse.api.mappers.toUserDto
import dev.rikoapp.chirpcourse.service.auth.AuthService
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/auth")
class AuthController(private val authService: AuthService) {

    @PostMapping("/register")
    fun register(
        @Valid @RequestBody body: RegisterRequest
    ): UserDto {
        return authService.register(
            email = body.email,
            username = body.username,
            password = body.password
        ).toUserDto()
    }

    @PostMapping("/login")
    fun login(
        @Valid @RequestBody body: LoginRequest
    ): AuthenticatedUserDto {
        return authService.login(
            email = body.email,
            password = body.password
        ).toAuthenticatedUserDto()
    }
}