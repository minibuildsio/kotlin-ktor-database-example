package io.minibuilds.infrastructure

import io.ktor.server.application.*
import org.jetbrains.exposed.sql.Database

fun Application.configureDatabase() =
    Database.connect(
        driver = "org.postgresql.Driver",
        url = environment.config.property("database.url").getString(),
        user = environment.config.property("database.user").getString(),
        password = environment.config.property("database.password").getString()
    )
