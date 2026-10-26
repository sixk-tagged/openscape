package org.rsmod.api.account.character.main

import java.nio.file.Files
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.rsmod.api.db.DatabaseConfig
import org.rsmod.api.db.migration.FlywayMigration
import org.rsmod.api.db.sqlite.SqliteConnection
import org.rsmod.api.db.sqlite.SqliteDatabase
import org.rsmod.api.testing.GameTestState

class AccountMembershipTest {
    @Test
    fun GameTestState.`new accounts are members`() =
        runInjectedGameTest(CharacterAccountRepository::class) { repository ->
            val file = Files.createTempFile("members-test", ".db").toFile().apply { deleteOnExit() }
            val config = DatabaseConfig("jdbc:sqlite:", file.absolutePath, null, null)
            FlywayMigration(config).migrate()
            val db = SqliteDatabase().apply { connect(SqliteConnection(config)) }

            val members = runBlocking {
                db.withTransaction { connection ->
                    val id = repository.insertOrSelectAccountId(connection, "Member", "x")
                    connection.prepareStatement("SELECT members FROM accounts WHERE id = ?").use {
                        it.setInt(1, checkNotNull(id))
                        it.executeQuery().use { rs ->
                            rs.next()
                            rs.getBoolean(1)
                        }
                    }
                }
            }
            assertEquals(true, members)
        }
}
