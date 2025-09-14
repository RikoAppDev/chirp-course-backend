pluginManagement {
    includeBuild("build-logic")
    repositories {
        maven { url = uri("https://repo.spring.io/snapshot") }
        gradlePluginPortal()
    }
}
rootProject.name = "ChirpCourse"

include("app")
include("user")
include("chat")
include("notification")
include("common")