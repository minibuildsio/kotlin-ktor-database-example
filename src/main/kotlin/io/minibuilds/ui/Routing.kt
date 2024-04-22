package io.minibuilds.ui

import io.minibuilds.infrastructure.UserRepository
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import java.time.LocalDate

fun Application.configureRouting(userRepository: UserRepository) {
    routing {
        get("/users") {
            call.respond(HttpStatusCode.OK, userRepository.getAllUsers())
        }

        get("/users/{id}") {
            val id = call.parameters["id"]?.toIntOrNull()
                ?: throw IllegalArgumentException("id must be an integer")
            val user = userRepository.getUser(id)
            if (user != null) {
                call.respond(HttpStatusCode.OK, user)
            } else {
                call.respond(HttpStatusCode.NotFound)
            }
        }

        post("/users") {
            val userRequest = call.receive<CreateUserRequest>()

            val user = userRepository.addUser(userRequest.name, userRequest.dateOfBirth)
            call.respond(HttpStatusCode.OK, user!!)
        }
    }
}

@Serializable
data class CreateUserRequest(
    val name: String,
    @Contextual
    val dateOfBirth: LocalDate
)
