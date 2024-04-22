package io.minibuilds.infrastructure

import kotlinx.coroutines.Dispatchers
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.javatime.date
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import java.time.LocalDate

@Serializable
data class User(
    val id: Int,
    val name: String,
    @Contextual
    val dateOfBirth: LocalDate
)

class UserRepository {
    object UserTable : Table() {
        val id = integer("id").autoIncrement()
        val name = varchar("name", length = 50)
        val dateOfBirth = date("date_of_birth")

        override val primaryKey = PrimaryKey(id)
    }

    private suspend fun <T> dbQuery(block: suspend () -> T): T =
        newSuspendedTransaction(Dispatchers.IO) { block() }

    suspend fun getAllUsers(): List<User> {
        return dbQuery {
            UserTable.selectAll()
                .map { it.toUser() }
        }
    }

    suspend fun getUser(id: Int): User? {
        return dbQuery {
            UserTable
                .select { UserTable.id eq id }
                .map { it.toUser() }
                .singleOrNull()
        }
    }

    suspend fun addUser(name: String, dateOfBirth: LocalDate): User? = dbQuery {
        val insertStatement = UserTable.insert {
            it[UserTable.name] = name
            it[UserTable.dateOfBirth] = dateOfBirth
        }
        insertStatement.resultedValues?.first()?.toUser()
    }

    companion object {
        private fun ResultRow.toUser() = User(this[UserTable.id], this[UserTable.name], this[UserTable.dateOfBirth])
    }
}

