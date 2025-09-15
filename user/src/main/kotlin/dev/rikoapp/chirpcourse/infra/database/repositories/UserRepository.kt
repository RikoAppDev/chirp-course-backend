package dev.rikoapp.chirpcourse.infra.database.repositories

import dev.rikoapp.chirpcourse.domain.model.UserId
import dev.rikoapp.chirpcourse.infra.database.entities.UserEntity
import org.springframework.data.jpa.repository.JpaRepository

interface UserRepository : JpaRepository<UserEntity, UserId> {
    fun findByEmail(email: String): UserEntity?
    fun findByEmailOrUsername(email: String, username: String): UserEntity?
}