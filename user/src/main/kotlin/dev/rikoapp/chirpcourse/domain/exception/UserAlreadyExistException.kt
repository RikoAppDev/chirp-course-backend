package dev.rikoapp.chirpcourse.domain.exception

class UserAlreadyExistException : RuntimeException(
    "A user with this username or email already exists."
)