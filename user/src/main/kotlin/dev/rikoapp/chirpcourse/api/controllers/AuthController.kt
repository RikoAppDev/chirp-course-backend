package dev.rikoapp.chirpcourse.api.controllers

import dev.rikoapp.chirpcourse.api.dto.AuthenticatedUserDto
import dev.rikoapp.chirpcourse.api.dto.ChangePasswordRequest
import dev.rikoapp.chirpcourse.api.dto.EmailRequest
import dev.rikoapp.chirpcourse.api.dto.LoginRequest
import dev.rikoapp.chirpcourse.api.dto.RefreshRequest
import dev.rikoapp.chirpcourse.api.dto.RegisterRequest
import dev.rikoapp.chirpcourse.api.dto.ResetPasswordRequest
import dev.rikoapp.chirpcourse.api.dto.UserDto
import dev.rikoapp.chirpcourse.api.mappers.toAuthenticatedUserDto
import dev.rikoapp.chirpcourse.api.mappers.toUserDto
import dev.rikoapp.chirpcourse.infra.rate_limiting.EmailRateLimiter
import dev.rikoapp.chirpcourse.service.AuthService
import dev.rikoapp.chirpcourse.service.EmailVerificationService
import dev.rikoapp.chirpcourse.service.PasswordResetService
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/auth")
class AuthController(
    private val authService: AuthService,
    private val emailVerificationService: EmailVerificationService,
    private val passwordResetService: PasswordResetService,
    private val emailRateLimiter: EmailRateLimiter
) {

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

    @PostMapping("/refresh")
    fun refresh(
        @RequestBody body: RefreshRequest
    ): AuthenticatedUserDto {
        return authService.refresh(
            body.refreshToken
        ).toAuthenticatedUserDto()
    }

    @PostMapping("/resend-verification")
    fun resendVerification(
        @Valid @RequestBody body: EmailRequest
    ) {
        emailRateLimiter.withRateLimit(
            email = body.email
        ) {
            emailVerificationService.resendVerificationEmail(body.email)
        }
    }

    @GetMapping("/verify")
    fun verifyEmail(
        @RequestParam token: String
    ) {
        emailVerificationService.verifyEmail(token)
    }

    @PostMapping("/forgot-password")
    fun forgotPassword(
        @Valid @RequestBody body: EmailRequest
    ) {
        passwordResetService.requestPasswordReset(body.email)
    }

    @PostMapping("/reset-password")
    fun resetPassword(
        @Valid @RequestBody body: ResetPasswordRequest
    ) {
        passwordResetService.resetPassword(
            token = body.token,
            newPassword = body.newPassword
        )
    }

    @PostMapping("/change-password")
    fun changePassword(
        @Valid @RequestBody body: ChangePasswordRequest
    ) {
        // TODO: Extract user ID from security context
    }
}