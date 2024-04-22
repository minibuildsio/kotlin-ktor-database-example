package io.minibuilds

import io.kotest.assertions.json.shouldBeJsonArray
import io.kotest.assertions.json.shouldContainJsonKeyValue
import io.kotest.core.spec.style.FeatureSpec
import io.kotest.matchers.shouldBe
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.testing.*
import io.minibuilds.infrastructure.User
import io.minibuilds.infrastructure.UserRepository
import io.minibuilds.ui.configureErrorHandling
import io.minibuilds.ui.configureRouting
import io.minibuilds.ui.configureSerialization
import io.mockk.coEvery
import io.mockk.mockk
import java.time.LocalDate

class ApplicationTest : FeatureSpec({
    val userRepository = mockk<UserRepository>()

    feature("users endpoint") {
        scenario("get users endpoint returns users") {
            testApplication {
                configureApplication(userRepository)

                coEvery {
                    userRepository.getAllUsers()
                } returns listOf(
                    User(1, "Gary", LocalDate.of(1997, 8, 23))
                )

                client.get("/users").apply {
                    status shouldBe HttpStatusCode.OK

                    val response = bodyAsText()
                    response.shouldBeJsonArray()
                    response.shouldContainJsonKeyValue("$[0].id", 1)
                    response.shouldContainJsonKeyValue("$[0].name", "Gary")
                    response.shouldContainJsonKeyValue("$[0].dateOfBirth", "1997-08-23")
                }
            }
        }

        scenario("get users id endpoint returns user with id") {
            testApplication {
                configureApplication(userRepository)

                coEvery {
                    userRepository.getUser(10)
                } returns User(10, "Gary", LocalDate.of(1997, 8, 23))

                client.get("/users/10").apply {
                    status shouldBe HttpStatusCode.OK

                    val response = bodyAsText()
                    response.shouldContainJsonKeyValue("$.name", "Gary")
                    response.shouldContainJsonKeyValue("$.dateOfBirth", "1997-08-23")
                }
            }
        }

        scenario("post users endpoint creates a user") {
            testApplication {
                configureApplication(userRepository)

                coEvery {
                    userRepository.addUser("Gary", LocalDate.of(1997, 8, 23))
                } returns User(10, "Gary", LocalDate.of(1997, 8, 23))

                client.post("/users") {
                    contentType(ContentType.Application.Json)
                    setBody("""{"name":"Gary","dateOfBirth":"1997-08-23"}""")
                }.apply {
                    status shouldBe HttpStatusCode.OK

                    val response = bodyAsText()
                    response.shouldContainJsonKeyValue("$.name", "Gary")
                    response.shouldContainJsonKeyValue("$.dateOfBirth", "1997-08-23")
                }
            }
        }
    }
}) {
    companion object {
        fun ApplicationTestBuilder.configureApplication(userRepository: UserRepository) {
            application {
                configureSerialization()
                configureErrorHandling()
                configureRouting(userRepository)
            }
        }
    }
}
