package io.minibuilds.infrastructure

import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.javatime.date
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.LocalDate

@Serializable
data class User(
    val id: Int,
    val name: String,
    @Contextual
    val dateOfBirth: LocalDate
)

object UserTable : Table() {
    val id = integer("id").autoIncrement()
    val name = varchar("name", length = 50)
    val dateOfBirth = date("date_of_birth")

    override val primaryKey = PrimaryKey(id)
}

class UserRepository {

    fun getAllUsers(): List<User> =
        transaction {
            UserTable.selectAll()
                .map { it.toUser() }
        }

    fun getUser(id: Int): User? =
        transaction {
            UserTable
                .select { UserTable.id eq id }
                .map { it.toUser() }
                .singleOrNull()
        }

    fun addUser(name: String, dateOfBirth: LocalDate): User? =
        transaction {
            val insertStatement = UserTable.insert {
                it[UserTable.name] = name
                it[UserTable.dateOfBirth] = dateOfBirth
            }
            insertStatement.resultedValues?.first()?.toUser()
        }

    companion object {
        private fun ResultRow.toUser() = User(
            this[UserTable.id],
            this[UserTable.name],
            this[UserTable.dateOfBirth]
        )
    }
}
