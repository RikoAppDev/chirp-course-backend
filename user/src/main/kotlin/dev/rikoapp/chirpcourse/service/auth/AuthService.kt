package dev.rikoapp.chirpcourse.service.auth

import dev.rikoapp.chirpcourse.domain.exception.UserAlreadyExistException
import dev.rikoapp.chirpcourse.domain.model.User
import dev.rikoapp.chirpcourse.infra.database.entities.UserEntity
import dev.rikoapp.chirpcourse.infra.database.mappers.toUser
import dev.rikoapp.chirpcourse.infra.database.repositories.UserRepository
import dev.rikoapp.chirpcourse.infra.security.PasswordEncoder
import org.springframework.stereotype.Service

@Service
class AuthService(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder
) {
    fun register(email: String, username: String, password: String): User {
        val user = userRepository.findByEmailOrUsername(
            email.trim(),
            username.trim()
        )

        if (user != null) {
            throw UserAlreadyExistException()
        }

        val savedUser = userRepository.save(
            UserEntity(
                email = email.trim(),
                username = username.trim(),
                hashedPassword = passwordEncoder.encode(password)!!
            )
        ).toUser()

        return savedUser
    }
}