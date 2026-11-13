package org.rsmod.api.ge

import jakarta.inject.Inject
import java.nio.file.Files
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.parallel.Execution
import org.junit.jupiter.api.parallel.ExecutionMode
import org.rsmod.api.config.refs.objs
import org.rsmod.api.db.DatabaseConfig
import org.rsmod.api.db.DatabaseConnection
import org.rsmod.api.db.migration.FlywayMigration
import org.rsmod.api.db.sqlite.SqliteConnection
import org.rsmod.api.db.sqlite.SqliteDatabase
import org.rsmod.api.testing.GameTestState
import org.rsmod.game.entity.Player
import org.rsmod.game.entity.PlayerList
import org.rsmod.game.inv.InvObj
import org.rsmod.game.type.inv.InvTypeList
import org.rsmod.game.type.obj.ObjTypeList

@Execution(ExecutionMode.SAME_THREAD)
class GrandExchangePersistenceTest {
    class Deps
    @Inject
    constructor(
        val ge: GrandExchangeImpl,
        val objTypes: ObjTypeList,
        val invTypes: InvTypeList,
        val players: PlayerList,
    )

    /** Applies each write to a real database straight away, like the account writer would. */
    private class DatabasePersistence(private val db: SqliteDatabase) : GePersistence {
        var actors = 0

        override fun commit(actor: Player?, work: (DatabaseConnection) -> Unit) {
            if (actor != null) {
                actors++
            }
            runBlocking { db.withTransaction { work(it) } }
        }
    }

    @Test
    fun GameTestState
        .`a restarted exchange has the same offers, boxes, history, limits and prices`() {
        val db = migratedDatabase()
        val persistence = DatabasePersistence(db)
        val clock = GrandExchangeTest.TestClock()
        runInjectedGameTest(
            Deps::class,
            GrandExchangeTestModule(clock = clock, persistence = persistence),
        ) { deps ->
            val seller = player
            val buyer = registerPlayer(uuid = 1)
            insertCharacter(db, 0)
            insertCharacter(db, 1)
            val item = firstObjType {
                it.stockmarket &&
                    it.tradeable &&
                    !it.members &&
                    !it.stackable &&
                    !it.isCert &&
                    !it.isPlaceholder &&
                    it.canCert &&
                    GeBuyLimits[it.id] >= 5
            }
            seller.inv[0] = InvObj(deps.objTypes.cert(item), 4)
            buyer.inv[0] = InvObj(objs.coins, 10_000)
            val ge = deps.ge

            ge.place(seller, 0, GeOfferType.Sell, item, 3, 100)
            ge.place(seller, 1, GeOfferType.Sell, item, 1, 500)
            ge.place(buyer, 0, GeOfferType.Buy, item, 2, 100)
            ge.place(buyer, 1, GeOfferType.Buy, item, 1, 50)
            ge.abort(buyer, 1)
            assertEquals(4, persistence.actors)

            val restarted =
                GrandExchangeImpl(
                    deps.objTypes,
                    deps.invTypes,
                    deps.players,
                    NoopGePersistence,
                    clock,
                )
            runBlocking { db.withTransaction { restarted.load(it) } }

            assertEquals(ge.offers(seller), restarted.offers(seller))
            assertEquals(ge.offers(buyer), restarted.offers(buyer))
            assertEquals(ge.history(buyer), restarted.history(buyer))
            assertEquals(ge.guidePrice(item), restarted.guidePrice(item))
            assertEquals(
                ge.buyLimitRemaining(buyer, item),
                restarted.buyLimitRemaining(buyer, item),
            )
            assertEquals(GeBuyLimits[item.id] - 2, restarted.buyLimitRemaining(buyer, item))
            // The loaded book trades: a buy takes the cheapest sell left, partly filled before.
            assertEquals(
                GePlaceResult.Placed,
                restarted.place(buyer, 2, GeOfferType.Buy, item, 1, 500),
            )
            assertEquals(3, restarted.offers(seller)[0]!!.completedCount)

            // The account save hook writes the same rows again and nothing else changes.
            val before = ge.offers(seller)
            runBlocking { db.withTransaction { ge.saveWork(0)!!(it) } }
            val reloaded =
                GrandExchangeImpl(
                    deps.objTypes,
                    deps.invTypes,
                    deps.players,
                    NoopGePersistence,
                    clock,
                )
            runBlocking { db.withTransaction { reloaded.load(it) } }
            assertEquals(before, reloaded.offers(seller))
        }
    }

    private fun migratedDatabase(): SqliteDatabase {
        val file = Files.createTempFile("ge-test", ".db").toFile().apply { deleteOnExit() }
        val config = DatabaseConfig("jdbc:sqlite:", file.absolutePath, null, null)
        FlywayMigration(config).migrate()
        return SqliteDatabase().apply { connect(SqliteConnection(config)) }
    }

    /** The exchange tables reference `characters`: give each test character a row with its id. */
    private fun insertCharacter(db: SqliteDatabase, id: Int) = runBlocking {
        db.withTransaction { connection ->
            connection
                .prepareStatement(
                    "INSERT OR IGNORE INTO accounts (id, login_username, password_hash) " +
                        "VALUES (?, ?, 'x')"
                )
                .use {
                    it.setInt(1, id + 100)
                    it.setString(2, "ge$id")
                    it.executeUpdate()
                }
            connection
                .prepareStatement(
                    "INSERT INTO characters (id, account_id, realm_id) VALUES (?, ?, 1)"
                )
                .use {
                    it.setInt(1, id)
                    it.setInt(2, id + 100)
                    it.executeUpdate()
                }
        }
    }
}
