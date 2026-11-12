package org.rsmod.content.areas.city.gnomestronghold

import jakarta.inject.Inject
import org.rsmod.api.player.dialogue.Dialogue
import org.rsmod.api.player.protect.ProtectedAccess
import org.rsmod.api.script.onOpLoc1
import org.rsmod.api.script.onOpLoc2
import org.rsmod.api.script.onOpLoc4
import org.rsmod.api.script.onOpNpc1
import org.rsmod.api.script.onOpNpc3
import org.rsmod.api.shops.Shops
import org.rsmod.game.entity.Npc
import org.rsmod.game.type.inv.InvType
import org.rsmod.game.type.npc.NpcType
import org.rsmod.map.CoordGrid
import org.rsmod.plugin.scripts.PluginScript
import org.rsmod.plugin.scripts.ScriptContext

/**
 * The Gnome Stronghold: the Grand Tree's shops and the "top floor" / "bottom floor" ladder options.
 * Banks, doors, stairs and the ladders that land on the player's own tile are content groups set in
 * [GnomeStrongholdLocEditor].
 */
// UNVERIFIED: the greetings are the shopkeepers' usual openers; the wiki reference has none.
class GnomeStrongholdScript @Inject constructor(private val shops: Shops) : PluginScript() {
    override fun ScriptContext.startup() {
        keeper(gnome_npcs.heckel, "Funch's Fine Groceries", gnome_invs.heckel, "Hello there!")
        keeper(gnome_npcs.hudo, "Grand Tree Groceries", gnome_invs.hudo, "Hello there!")
        keeper(gnome_npcs.gulluck, "Gulluck and Sons", gnome_invs.gulluck, "Hello there!")
        keeper(
            gnome_npcs.rometti,
            "Rometti's Fine Fashions",
            gnome_invs.rometti,
            "Welcome to Rometti's Fine Fashions.",
        )

        // The main south gate (the transport table's tiles either side of it).
        onOpLoc1(gnome_locs.area_gate) { gate() }

        // The Grand Tree's ladders run between its four floors on the player's own tile.
        onOpLoc2(gnome_locs.ladder_ground) { ladderTo(3) }
        onOpLoc2(gnome_locs.ladder_roof) { ladderTo(0) }
        onOpLoc4(gnome_locs.ladder_first) { ladderTo(3) }
        onOpLoc4(gnome_locs.ladder_second) { ladderTo(0) }
    }

    private suspend fun ProtectedAccess.ladderTo(level: Int) {
        arriveDelay()
        delay(1)
        telejump(player.coords.translateLevel(level - player.coords.level))
    }

    private suspend fun ProtectedAccess.gate() {
        arriveDelay()
        delay(1)
        val z = if (player.coords.z >= GATE_Z) GATE_SOUTH_Z else GATE_NORTH_Z
        telejump(CoordGrid(player.coords.x.coerceIn(GATE_MIN_X, GATE_MAX_X), z))
    }

    private fun ScriptContext.keeper(npc: NpcType, title: String, inv: InvType, greeting: String) {
        onOpNpc1(npc) { startDialogue(it.npc) { shopTalk(it.npc, title, inv, greeting) } }
        onOpNpc3(npc) { shops.open(player, it.npc, title, inv) }
    }

    private suspend fun Dialogue.shopTalk(npc: Npc, title: String, inv: InvType, greeting: String) {
        chatNpc(happy, greeting)
        val choice = choice2("Yes please. What are you selling?", 1, "No thanks.", 2)
        if (choice == 1) {
            shops.open(player, npc, title, inv)
        } else {
            chatPlayer(neutral, "No thanks.")
        }
    }

    private companion object {
        const val GATE_Z = 3384
        const val GATE_SOUTH_Z = 3382
        const val GATE_NORTH_Z = 3385
        const val GATE_MIN_X = 2460
        const val GATE_MAX_X = 2462
    }
}
