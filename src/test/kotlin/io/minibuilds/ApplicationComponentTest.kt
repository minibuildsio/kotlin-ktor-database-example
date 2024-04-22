package io.minibuilds

import io.minibuilds.infrastructure.User
import io.minibuilds.infrastructure.UserRepository
import io.minibuilds.infrastructure.configureDatabase
import io.minibuilds.ui.LocalDateSerializer
import io.minibuilds.ui.configureErrorHandling
import io.minibuilds.ui.configureRouting
import io.minibuilds.ui.configureSerialization
import io.kotest.assertions.json.shouldBeJsonArray
import io.kotest.assertions.json.shouldContainJsonKey
import io.kotest.assertions.json.shouldContainJsonKeyValue
import io.kotest.core.spec.style.FeatureSpec
import io.kotest.matchers.shouldBe
import io.ktor.client.call.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.testing.*
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.contextual
import org.testcontainers.containers.BindMode
import org.testcontainers.containers.PostgreSQLContainer


class ApplicationComponentTest : FeatureSpec({

    val postgres = PostgreSQLContainer<Nothing>("postgres").apply {
        startupAttempts = 1
        withDatabaseName("exampledb")
        withUsername("postgres")
        withPassword("postgres")
        withFileSystemBind("src/test/resources/init.sql", "/docker-entrypoint-initdb.d/init.sql", BindMode.READ_ONLY)
    }

    beforeTest {
        postgres.start()
        System.setProperty("DB_URL", postgres.jdbcUrl)
    }

    feature("users endpoint") {
        scenario("get users endpoint returns users") {
            testApplication {
                configureApplication()

                client.post("/users") {
                    contentType(ContentType.Application.Json)
                    setBody("""{"name":"Gary","dateOfBirth":"1997-08-23"}""")
                }

                client.get("/users").apply {
                    status shouldBe HttpStatusCode.OK

                    val response = bodyAsText()
                    response.shouldBeJsonArray()
                    response.shouldContainJsonKey("$.[?(@.name == \"Gary\")]")
                    response.shouldContainJsonKey("$.[?(@.dateOfBirth == \"1997-08-23\")]")
                }
            }
        }

        scenario("get users id endpoint returns user with id") {
            testApplication {
                configureApplication()

                val client = createClient()

                val user = client.post("/users") {
                    contentType(ContentType.Application.Json)
                    setBody("""{"name":"Gary","dateOfBirth":"1997-08-23"}""")
                }.body<User>()

                client.get("/users/${user.id}").apply {
                    status shouldBe HttpStatusCode.OK

                    val response = bodyAsText()
                    response.shouldContainJsonKeyValue("$.name", "Gary")
                    response.shouldContainJsonKeyValue("$.dateOfBirth", "1997-08-23")
                }
            }
        }

        scenario("post users endpoint creates a user") {
            testApplication {
                configureApplication()

                client.post("/users") {
                    contentType(ContentType.Application.Json)
                    setBody("""{"name":"Gary","dateOfBirth":"1997-08-23"}""")
                }.apply {
                    status shouldBe HttpStatusCode.OK

                    val response = bodyAsText()
                    response shouldContainJsonKey "$.id"
                    response.shouldContainJsonKeyValue("$.name", "Gary")
                    response.shouldContainJsonKeyValue("$.dateOfBirth", "1997-08-23")
                }
            }
        }
    }
}) {
    companion object {
        fun ApplicationTestBuilder.configureApplication() {
            application {
                configureSerialization()
                configureErrorHandling()
                configureDatabase()
                configureRouting(UserRepository())
            }
        }

        fun ApplicationTestBuilder.createClient() = createClient {
            install(ContentNegotiation) {
                json(Json {
                    serializersModule = SerializersModule {
                        contextual(LocalDateSerializer)
                    }
                })
            }
        }
    }
}
