package dev.rikoapp.chirpcourse.service

import dev.rikoapp.chirpcourse.domain.exception.InvalidTokenException
import dev.rikoapp.chirpcourse.domain.exception.UserNotFoundException
import dev.rikoapp.chirpcourse.domain.model.EmailVerificationToken
import dev.rikoapp.chirpcourse.infra.database.entities.EmailVerificationTokenEntity
import dev.rikoapp.chirpcourse.infra.database.mappers.toEmailVerificationToken
import dev.rikoapp.chirpcourse.infra.database.repositories.EmailVerificationTokenRepository
import dev.rikoapp.chirpcourse.infra.database.repositories.UserRepository
import org.springframework.beans.factory.annotation.Value
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import java.time.temporal.ChronoUnit

@Service
class EmailVerificationService(
    private val emailVerificationTokenRepository: EmailVerificationTokenRepository,
    private val userRepository: UserRepository,
    @param:Value("\${chirp.email.verification.expiry-hours}") private val expiryHours: Long
) {
    @Transactional
    fun createVerificationToken(email: String): EmailVerificationToken {
        val userEntity = userRepository.findByEmail(email)
            ?: throw UserNotFoundException()

        emailVerificationTokenRepository.invalidateActiveTokensForUser(userEntity)

        val token = EmailVerificationTokenEntity(
            expiresAt = Instant.now().plus(expiryHours, ChronoUnit.HOURS),
            user = userEntity
        )

        return emailVerificationTokenRepository.save(token).toEmailVerificationToken()
    }

    @Transactional
    fun verifyEmail(token: String) {
        val verificationToken = emailVerificationTokenRepository.findByToken(token)
            ?: throw InvalidTokenException("Email verification token is not valid")

        if (verificationToken.isUsed) {
            throw InvalidTokenException("Email verification token has already been used")
        }

        if (verificationToken.isExpired) {
            throw InvalidTokenException("Email verification token has expired")
        }

        emailVerificationTokenRepository.save(
            verificationToken.apply {
                usedAt = Instant.now()
            }
        )
        userRepository.save(
            verificationToken.user.apply {
                hasVerifiedEmail = true
            }
        )
    }

    @Scheduled(cron = "0 0 3 * * *")
    fun cleanupExpiredTokens() {
        emailVerificationTokenRepository.deleteByExpiresAtLessThan(
            now = Instant.now()
        )
    }
}