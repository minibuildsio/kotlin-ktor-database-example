package io.minibuilds

import io.minibuilds.infrastructure.UserRepository
import io.minibuilds.infrastructure.configureDatabase
import io.minibuilds.ui.configureErrorHandling
import io.minibuilds.ui.configureRouting
import io.minibuilds.ui.configureSerialization
import io.ktor.server.application.*

fun main(args: Array<String>) {
    io.ktor.server.netty.EngineMain.main(args)
}

fun Application.module() {
    configureSerialization()
    configureErrorHandling()

    configureDatabase()
    configureRouting(UserRepository())
}
