package dev.rikoapp.chirpcourse

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class ChirpCourseApplication

fun main(args: Array<String>) {
    runApplication<ChirpCourseApplication>(*args)
}
