package org.rsmod.api.ge

import org.rsmod.api.db.DatabaseConnection

internal data class OfferRow(
    val characterId: Int,
    val slot: Int,
    val offerId: Long,
    val type: GeOfferType,
    val obj: Int,
    val price: Int,
    val count: Int,
    val completedCount: Int,
    val completedGold: Long,
    val taxPaid: Long,
    val status: GeOfferStatus,
    val collectItems: Int,
    val collectCoins: Long,
)

internal data class WindowRow(val obj: Int, val start: Long, val bought: Int)

/** Everything stored for one character. Written whole, so a write is always a full state. */
internal data class CharacterRows(
    val characterId: Int,
    val offers: List<OfferRow>,
    val history: List<GeHistoryEntry>,
    val windows: List<WindowRow>,
)

internal class LoadedRows(val characters: List<CharacterRows>, val prices: Map<Int, Int>)

internal object GeSql {
    fun writeCharacter(connection: DatabaseConnection, rows: CharacterRows) {
        for (table in listOf("ge_offers", "ge_history", "ge_buy_limits")) {
            connection.prepareStatement("DELETE FROM $table WHERE character_id = ?").use {
                it.setInt(1, rows.characterId)
                it.executeUpdate()
            }
        }
        connection
            .prepareStatement(
                """
                    INSERT INTO ge_offers (character_id, slot, offer_id, type, obj, price, count,
                        completed_count, completed_gold, tax_paid, status, collect_items,
                        collect_coins)
                    VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """
                    .trimIndent()
            )
            .use { insert ->
                for (row in rows.offers) {
                    insert.setInt(1, row.characterId)
                    insert.setInt(2, row.slot)
                    insert.setLong(3, row.offerId)
                    insert.setInt(4, row.type.ordinal)
                    insert.setInt(5, row.obj)
                    insert.setInt(6, row.price)
                    insert.setInt(7, row.count)
                    insert.setInt(8, row.completedCount)
                    insert.setLong(9, row.completedGold)
                    insert.setLong(10, row.taxPaid)
                    insert.setInt(11, row.status.ordinal)
                    insert.setInt(12, row.collectItems)
                    insert.setLong(13, row.collectCoins)
                    insert.addBatch()
                }
                insert.executeBatch()
            }
        connection
            .prepareStatement(
                """
                    INSERT INTO ge_history (character_id, position, obj, type, count, gold, tax)
                    VALUES (?, ?, ?, ?, ?, ?, ?)
                """
                    .trimIndent()
            )
            .use { insert ->
                rows.history.forEachIndexed { position, entry ->
                    insert.setInt(1, rows.characterId)
                    insert.setInt(2, position)
                    insert.setInt(3, entry.obj)
                    insert.setInt(4, entry.type.ordinal)
                    insert.setInt(5, entry.count)
                    insert.setLong(6, entry.gold)
                    insert.setLong(7, entry.tax)
                    insert.addBatch()
                }
                insert.executeBatch()
            }
        connection
            .prepareStatement(
                """
                    INSERT INTO ge_buy_limits (character_id, obj, window_start, bought)
                    VALUES (?, ?, ?, ?)
                """
                    .trimIndent()
            )
            .use { insert ->
                for (window in rows.windows) {
                    insert.setInt(1, rows.characterId)
                    insert.setInt(2, window.obj)
                    insert.setLong(3, window.start)
                    insert.setInt(4, window.bought)
                    insert.addBatch()
                }
                insert.executeBatch()
            }
    }

    fun writePrices(connection: DatabaseConnection, prices: Map<Int, Int>) {
        if (prices.isEmpty()) {
            return
        }
        val sql = "INSERT OR REPLACE INTO ge_prices (obj, price) VALUES (?, ?)"
        connection.prepareStatement(sql).use { insert ->
            for ((obj, price) in prices) {
                insert.setInt(1, obj)
                insert.setInt(2, price)
                insert.addBatch()
            }
            insert.executeBatch()
        }
    }

    fun load(connection: DatabaseConnection): LoadedRows {
        val offers = HashMap<Int, MutableList<OfferRow>>()
        connection.prepareStatement("SELECT * FROM ge_offers").use { select ->
            select.executeQuery().use { rs ->
                while (rs.next()) {
                    val row =
                        OfferRow(
                            characterId = rs.getInt("character_id"),
                            slot = rs.getInt("slot"),
                            offerId = rs.getLong("offer_id"),
                            type = GeOfferType.entries[rs.getInt("type")],
                            obj = rs.getInt("obj"),
                            price = rs.getInt("price"),
                            count = rs.getInt("count"),
                            completedCount = rs.getInt("completed_count"),
                            completedGold = rs.getLong("completed_gold"),
                            taxPaid = rs.getLong("tax_paid"),
                            status = GeOfferStatus.entries[rs.getInt("status")],
                            collectItems = rs.getInt("collect_items"),
                            collectCoins = rs.getLong("collect_coins"),
                        )
                    offers.getOrPut(row.characterId) { ArrayList() } += row
                }
            }
        }
        val history = HashMap<Int, MutableList<GeHistoryEntry>>()
        connection.prepareStatement("SELECT * FROM ge_history ORDER BY position").use { select ->
            select.executeQuery().use { rs ->
                while (rs.next()) {
                    val entry =
                        GeHistoryEntry(
                            obj = rs.getInt("obj"),
                            type = GeOfferType.entries[rs.getInt("type")],
                            count = rs.getInt("count"),
                            gold = rs.getLong("gold"),
                            tax = rs.getLong("tax"),
                        )
                    history.getOrPut(rs.getInt("character_id")) { ArrayList() } += entry
                }
            }
        }
        val windows = HashMap<Int, MutableList<WindowRow>>()
        connection.prepareStatement("SELECT * FROM ge_buy_limits").use { select ->
            select.executeQuery().use { rs ->
                while (rs.next()) {
                    val row =
                        WindowRow(
                            obj = rs.getInt("obj"),
                            start = rs.getLong("window_start"),
                            bought = rs.getInt("bought"),
                        )
                    windows.getOrPut(rs.getInt("character_id")) { ArrayList() } += row
                }
            }
        }
        val prices = HashMap<Int, Int>()
        connection.prepareStatement("SELECT obj, price FROM ge_prices").use { select ->
            select.executeQuery().use { rs ->
                while (rs.next()) {
                    prices[rs.getInt("obj")] = rs.getInt("price")
                }
            }
        }
        val characters = offers.keys + history.keys + windows.keys
        return LoadedRows(
            characters =
                characters.map {
                    CharacterRows(
                        characterId = it,
                        offers = offers[it].orEmpty(),
                        history = history[it].orEmpty(),
                        windows = windows[it].orEmpty(),
                    )
                },
            prices = prices,
        )
    }
}
