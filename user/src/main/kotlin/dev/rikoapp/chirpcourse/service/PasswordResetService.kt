package dev.rikoapp.chirpcourse.service

import dev.rikoapp.chirpcourse.domain.exception.InvalidCredentialsException
import dev.rikoapp.chirpcourse.domain.exception.InvalidTokenException
import dev.rikoapp.chirpcourse.domain.exception.SamePasswordException
import dev.rikoapp.chirpcourse.domain.exception.UserNotFoundException
import dev.rikoapp.chirpcourse.domain.model.UserId
import dev.rikoapp.chirpcourse.infra.database.entities.PasswordResetTokenEntity
import dev.rikoapp.chirpcourse.infra.database.repositories.PasswordResetTokenRepository
import dev.rikoapp.chirpcourse.infra.database.repositories.RefreshTokenRepository
import dev.rikoapp.chirpcourse.infra.database.repositories.UserRepository
import dev.rikoapp.chirpcourse.infra.security.PasswordEncoder
import org.springframework.beans.factory.annotation.Value
import org.springframework.data.repository.findByIdOrNull
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import java.time.temporal.ChronoUnit

@Service
class PasswordResetService(
    private val userRepository: UserRepository,
    private val passwordResetTokenRepository: PasswordResetTokenRepository,
    private val passwordEncoder: PasswordEncoder,
    @param:Value("\${chirp.email.reset-password.expiry-minutes}")
    private val expiryMinutes: Long,
    private val refreshTokenRepository: RefreshTokenRepository
) {
    @Transactional
    fun requestPasswordReset(email: String) {
        val user = userRepository.findByEmail(email) ?: return

        passwordResetTokenRepository.invalidateActiveTokensForUser(user)

        val token = PasswordResetTokenEntity(
            user = user,
            expiresAt = Instant.now().plus(expiryMinutes, ChronoUnit.MINUTES)
        )
        passwordResetTokenRepository.save(token)

        // TODO: Inform notification service about password reset trigger to send email
    }

    @Transactional
    fun resetPassword(token: String, newPassword: String) {
        val resetToken = passwordResetTokenRepository.findByToken(token)
            ?: throw InvalidTokenException("Invalid password reset token")

        if (resetToken.isUsed) {
            throw InvalidTokenException("Password reset token has already been used")
        }

        if (resetToken.isExpired) {
            throw InvalidTokenException("Password reset token has expired")
        }

        val user = resetToken.user

        if (passwordEncoder.matches(newPassword, user.hashedPassword)) {
            throw SamePasswordException()
        }

        val newPasswordHashed = passwordEncoder.encode(newPassword)
        userRepository.save(
            user.apply {
                this.hashedPassword = newPasswordHashed!!
            }
        )
        passwordResetTokenRepository.save(
            resetToken.apply {
                usedAt = Instant.now()
            }
        )
        refreshTokenRepository.deleteByUserId(user.id!!)
    }

    @Transactional
    fun changePassword(
        userId: UserId,
        oldPassword: String,
        newPassword: String
    ) {
        val user = userRepository.findByIdOrNull(userId)
            ?: throw UserNotFoundException()

        if (!passwordEncoder.matches(oldPassword, user.hashedPassword)) {
            throw InvalidCredentialsException()
        }

        if (oldPassword == newPassword) {
            throw SamePasswordException()
        }

        refreshTokenRepository.deleteByUserId(user.id!!)

        val newPasswordHashed = passwordEncoder.encode(newPassword)
        userRepository.save(
            user.apply {
                this.hashedPassword = newPasswordHashed!!
            }
        )
    }

    @Scheduled(cron = "0 0 3 * * *")
    fun cleanupExpiredTokens() {
        passwordResetTokenRepository.deleteByExpiresAtLessThan(
            now = Instant.now()
        )
    }
}