package org.rsmod.api.account.saver

import com.google.inject.Injector
import com.google.inject.spi.Elements
import com.google.inject.spi.LinkedKeyBinding
import jakarta.inject.Inject
import java.nio.file.Files
import java.time.LocalDateTime
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.rsmod.api.account.character.CharacterDataStage
import org.rsmod.api.account.character.CharacterModule
import org.rsmod.api.account.character.main.CharacterAccountRepository
import org.rsmod.api.config.refs.invs
import org.rsmod.api.config.refs.objs
import org.rsmod.api.config.refs.stats
import org.rsmod.api.db.DatabaseConfig
import org.rsmod.api.db.migration.FlywayMigration
import org.rsmod.api.db.sqlite.SqliteConnection
import org.rsmod.api.db.sqlite.SqliteDatabase
import org.rsmod.api.testing.GameTestState
import org.rsmod.game.entity.Player
import org.rsmod.game.inv.InvObj
import org.rsmod.game.type.varp.VarpLifetime
import org.rsmod.game.type.varp.VarpTypeList
import org.rsmod.map.CoordGrid

class PlayerSaveSnapshotTest {
    /**
     * If a save step reads a field the snapshot doesn't copy, the rows differ: autosave would write
     * a default over real data.
     */
    @Test
    fun GameTestState.`a snapshot saves exactly what the live player saves`() =
        runInjectedGameTest(Deps::class) { deps ->
            val db = migratedDatabase()
            val (accountId, characterId) = insertCharacter(db)
            val live = player
            live.accountId = accountId
            live.characterId = characterId
            live.modLevel = cacheTypes.modLevels.default()
            live.displayName = "Snapshot Test"
            live.lastKnownDevice = 7
            live.runEnergy = 4321
            live.lastLogin = LocalDateTime.of(2026, 1, 2, 3, 4, 5)
            live.statMap.setFineXP(stats.attack, 123_450)
            live.xpRate = 2.0
            live.teleport(CoordGrid(0, 50, 50, 10, 20))
            val permVarp = deps.varpTypes.values.first { it.scope == VarpLifetime.Perm }
            live.vars.backing[permVarp.id] = 12345
            live.setBaseLevel(stats.attack, 77)
            live.setCurrentLevel(stats.attack, 70)
            live.inv[3] = InvObj(objs.coins, 500)
            live.worn[3] = InvObj(objs.beer)
            live.invMap.getOrPut(cacheTypes.invs[invs.bank])[0] = InvObj(objs.coins, 9_999)

            assertTrue(deps.pipelines().size >= 2) { "Save steps found: ${deps.pipelines()}" }
            save(db, deps, live)
            val fromLive = rows(db)
            save(db, deps, live.saveSnapshot())
            val fromSnapshot = rows(db)
            assertEquals(fromLive, fromSnapshot)
        }

    private fun save(db: SqliteDatabase, deps: Deps, player: Player) = runBlocking {
        db.withTransaction { connection ->
            deps.repository.save(connection, player, player.accountId, player.characterId)
            for (pipeline in deps.pipelines()) {
                pipeline.save(connection, player, player.characterId)
            }
        }
    }

    private fun migratedDatabase(): SqliteDatabase {
        val file = Files.createTempFile("snapshot-test", ".db").toFile().apply { deleteOnExit() }
        val config = DatabaseConfig("jdbc:sqlite:", file.absolutePath, null, null)
        FlywayMigration(config).migrate()
        return SqliteDatabase().apply { connect(SqliteConnection(config)) }
    }

    private fun insertCharacter(db: SqliteDatabase): Pair<Int, Int> = runBlocking {
        db.withTransaction { connection ->
            val realmId =
                connection.prepareStatement("SELECT id FROM realms ORDER BY id LIMIT 1").use {
                    it.executeQuery().use { rs ->
                        rs.next()
                        rs.getInt(1)
                    }
                }
            connection
                .prepareStatement(
                    "INSERT INTO accounts (login_username, password_hash) VALUES ('snap', 'x')"
                )
                .use { it.executeUpdate() }
            val accountId =
                connection
                    .prepareStatement("SELECT id FROM accounts WHERE login_username = 'snap'")
                    .use {
                        it.executeQuery().use { rs ->
                            rs.next()
                            rs.getInt(1)
                        }
                    }
            connection
                .prepareStatement("INSERT INTO characters (account_id, realm_id) VALUES (?, ?)")
                .use {
                    it.setInt(1, accountId)
                    it.setInt(2, realmId)
                    it.executeUpdate()
                }
            val characterId =
                connection.prepareStatement("SELECT id FROM characters WHERE account_id = ?").use {
                    it.setInt(1, accountId)
                    it.executeQuery().use { rs ->
                        rs.next()
                        rs.getInt(1)
                    }
                }
            accountId to characterId
        }
    }

    /**
     * Every row of every table (but the timestamps a save stamps itself), sorted: a future save
     * step writing a new table or column is compared too.
     */
    private fun rows(db: SqliteDatabase): List<String> = runBlocking {
        db.withTransaction { connection ->
            val tables =
                connection
                    .prepareStatement(
                        "SELECT name FROM sqlite_master WHERE type = 'table' " +
                            "AND name NOT LIKE 'sqlite_%' AND name != 'flyway_schema_history'"
                    )
                    .use { statement ->
                        statement.executeQuery().use { rs ->
                            buildList { while (rs.next()) add(rs.getString(1)) }
                        }
                    }
            tables
                .flatMap { table ->
                    connection.prepareStatement("SELECT * FROM $table").use { statement ->
                        statement.executeQuery().use { rs ->
                            val meta = rs.metaData
                            val columns =
                                (1..meta.columnCount).filter {
                                    meta.getColumnName(it) !in STAMPED_COLUMNS
                                }
                            buildList {
                                while (rs.next()) {
                                    val values =
                                        columns.joinToString("|") {
                                            meta.getColumnName(it) + "=" + rs.getString(it)
                                        }
                                    add("$table: $values")
                                }
                            }
                        }
                    }
                }
                .sorted()
        }
    }

    class Deps
    @Inject
    constructor(
        val repository: CharacterAccountRepository,
        val varpTypes: VarpTypeList,
        private val injector: Injector,
    ) {
        /**
         * Every save step [CharacterModule] registers, so a future one is covered too. (The test
         * injector already declares the set, so the module can't simply be installed here.)
         */
        fun pipelines(): List<CharacterDataStage.Pipeline> =
            Elements.getElements(CharacterModule)
                .filterIsInstance<LinkedKeyBinding<*>>()
                .map { it.linkedKey.typeLiteral.rawType }
                .filter { CharacterDataStage.Pipeline::class.java.isAssignableFrom(it) }
                .distinct()
                .map { injector.getInstance(it) as CharacterDataStage.Pipeline }
    }

    private companion object {
        /**
         * Stamped by every save with the time of the write, so they differ between the two saves.
         */
        val STAMPED_COLUMNS = setOf("last_logout", "created_at", "updated_at")
    }
}
