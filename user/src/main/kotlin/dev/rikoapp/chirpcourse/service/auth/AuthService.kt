package dev.rikoapp.chirpcourse.service.auth

import dev.rikoapp.chirpcourse.domain.exception.EmailNotVerifiedException
import dev.rikoapp.chirpcourse.domain.exception.InvalidCredentialsException
import dev.rikoapp.chirpcourse.domain.exception.InvalidTokenException
import dev.rikoapp.chirpcourse.domain.exception.UserAlreadyExistException
import dev.rikoapp.chirpcourse.domain.exception.UserNotFoundException
import dev.rikoapp.chirpcourse.domain.model.AuthenticatedUser
import dev.rikoapp.chirpcourse.domain.model.User
import dev.rikoapp.chirpcourse.domain.model.UserId
import dev.rikoapp.chirpcourse.infra.database.entities.RefreshTokenEntity
import dev.rikoapp.chirpcourse.infra.database.entities.UserEntity
import dev.rikoapp.chirpcourse.infra.database.mappers.toUser
import dev.rikoapp.chirpcourse.infra.database.repositories.RefreshTokenRepository
import dev.rikoapp.chirpcourse.infra.database.repositories.UserRepository
import dev.rikoapp.chirpcourse.infra.security.PasswordEncoder
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.security.MessageDigest
import java.time.Instant
import kotlin.io.encoding.Base64

@Service
class AuthService(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder,
    private val jwtService: JwtService,
    private val refreshTokenRepository: RefreshTokenRepository,
    private val emailVerificationService: EmailVerificationService
) {
    @Transactional
    fun register(email: String, username: String, password: String): User {
        val trimmedEmail = email.trim()

        val user = userRepository.findByEmailOrUsername(
            trimmedEmail,
            username.trim()
        )
        if (user != null) {
            throw UserAlreadyExistException()
        }

        val savedUser = userRepository.saveAndFlush(
            UserEntity(
                email = trimmedEmail,
                username = username.trim(),
                hashedPassword = passwordEncoder.encode(password)!!
            )
        ).toUser()

        val token = emailVerificationService.createVerificationToken(trimmedEmail)

        return savedUser
    }

    fun login(
        email: String,
        password: String
    ): AuthenticatedUser {
        val user = userRepository.findByEmail(email.trim())
            ?: throw InvalidCredentialsException()

        if (!passwordEncoder.matches(password, user.hashedPassword)) {
            throw InvalidCredentialsException()
        }

        if (!user.hasVerifiedEmail) {
            throw EmailNotVerifiedException()
        }

        return user.id?.let { userId ->
            val accessToken = jwtService.generateAccessToken(userId)
            val refreshToken = jwtService.generateRefreshToken(userId)

            storeRefreshToken(userId, refreshToken)
            AuthenticatedUser(
                user = user.toUser(),
                accessToken = accessToken,
                refreshToken = refreshToken
            )
        } ?: throw UserNotFoundException()
    }

    @Transactional
    fun refresh(refreshToken: String): AuthenticatedUser {
        if (!jwtService.validateRefreshToken(refreshToken)) {
            throw InvalidTokenException(
                message = "The attached refresh token is not valid"
            )
        }

        val userId = jwtService.getUserIdFromToken(refreshToken)
        val user = userRepository.findByIdOrNull(userId) ?: throw UserNotFoundException()

        val hashedToken = hashToken(refreshToken)
        return user.id?.let { userId ->
            refreshTokenRepository.findByUserIdAndHashedToken(
                userId = userId,
                hashedToken = hashedToken
            ) ?: throw InvalidTokenException(
                message = "The attached refresh token is not valid"
            )

            refreshTokenRepository.deleteByUserIdAndHashedToken(
                userId = userId,
                hashedToken = hashedToken
            )

            val newAccessToken = jwtService.generateAccessToken(userId)
            val newRefreshToken = jwtService.generateRefreshToken(userId)

            storeRefreshToken(userId, newRefreshToken)

            AuthenticatedUser(
                user = user.toUser(),
                accessToken = newAccessToken,
                refreshToken = newRefreshToken
            )
        } ?: throw UserNotFoundException()
    }

    private fun storeRefreshToken(userId: UserId, token: String) {
        val hashedToken = hashToken(token)
        val expiryMs = jwtService.refreshTokenValidityMs
        val expiresAt = Instant.now().plusMillis(expiryMs)

        refreshTokenRepository.save(
            RefreshTokenEntity(
                userId = userId,
                expiresAt = expiresAt,
                hashedToken = hashedToken
            )
        )
    }

    private fun hashToken(token: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val hashBytes = digest.digest(token.encodeToByteArray())
        return Base64.encode(hashBytes)
    }
}