package io.minibuilds.infrastructure

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import io.ktor.server.application.*
import org.jetbrains.exposed.sql.Database

fun Application.configureDatabase() {
    val config = HikariConfig().also {
        it.jdbcUrl = environment.config.property("database.url").getString()
        it.username = environment.config.property("database.user").getString()
        it.password = environment.config.property("database.password").getString()
        it.driverClassName = "org.postgresql.Driver"
        it.maximumPoolSize = 5
    }

    Database.connect(datasource = HikariDataSource(config))
}
