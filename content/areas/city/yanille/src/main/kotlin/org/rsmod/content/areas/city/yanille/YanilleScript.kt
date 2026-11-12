package org.rsmod.content.areas.city.yanille

import jakarta.inject.Inject
import org.rsmod.api.config.refs.stats
import org.rsmod.api.player.dialogue.Dialogue
import org.rsmod.api.player.protect.ProtectedAccess
import org.rsmod.api.player.stat.stat
import org.rsmod.api.script.onOpLoc1
import org.rsmod.api.script.onOpNpc1
import org.rsmod.api.script.onOpNpc3
import org.rsmod.api.script.onOpNpc5
import org.rsmod.api.shops.Shops
import org.rsmod.game.entity.Npc
import org.rsmod.game.loc.BoundLocInfo
import org.rsmod.game.loc.LocAngle
import org.rsmod.game.type.inv.InvType
import org.rsmod.game.type.npc.NpcType
import org.rsmod.map.CoordGrid
import org.rsmod.plugin.scripts.PluginScript
import org.rsmod.plugin.scripts.ScriptContext

/**
 * Yanille: Aleck's Hunter Emporium, Frenita's Cookery Shop, the Wizards' Guild's two shops and its
 * doors (Magic 66, boosts count), and the Yanille Agility Dungeon's stairs between its halves.
 */
// UNVERIFIED: the greetings are the shopkeepers' usual openers; the wiki reference has none.
class YanilleScript @Inject constructor(private val shops: Shops) : PluginScript() {
    override fun ScriptContext.startup() {
        keeper(
            yanille_npcs.aleck,
            "Aleck's Hunter Emporium",
            yanille_invs.aleck,
            "Hello there!",
            tradeOp = 5,
        )
        keeper(
            yanille_npcs.frenita,
            "Frenita's Cookery Shop",
            yanille_invs.frenita,
            "Would you like to buy some cooking equipment?",
        )
        keeper(
            yanille_npcs.akutha,
            "Magic Guild Store (Runes and Staves)",
            yanille_invs.akutha,
            "Welcome to the Magic Guild Store.",
        )
        keeper(
            yanille_npcs.sinina,
            "Magic Guild Store (Mystic Robes)",
            yanille_invs.sinina,
            "Welcome to the Magic Guild Store.",
        )

        onOpLoc1(yanille_locs.guild_door_left) { guildDoor(it.loc) }
        onOpLoc1(yanille_locs.guild_door_right) { guildDoor(it.loc) }

        // UNVERIFIED: the way back up lands on the tile the way down starts from.
        onOpLoc1(yanille_locs.dungeon_stairs_down) { telejump(DUNGEON_NORTH) }
        onOpLoc1(yanille_locs.dungeon_stairs_up) { telejump(DUNGEON_SOUTH) }
    }

    /**
     * The guild's doors stay shut: from the door's own tile you go in (Magic 66 needed), from the
     * tile inside you come out.
     */
    private suspend fun ProtectedAccess.guildDoor(door: BoundLocInfo) {
        val (outside, inside) =
            when (door.angle) {
                LocAngle.East -> door.coords to door.coords.translateX(1)
                LocAngle.West -> door.coords to door.coords.translateX(-1)
                LocAngle.North -> door.coords to door.coords.translateZ(1)
                LocAngle.South -> door.coords to door.coords.translateZ(-1)
            }
        if (coords == inside) {
            telejump(outside)
            return
        }
        if (player.stat(stats.magic) < GUILD_MAGIC_LEVEL) {
            // UNVERIFIED: the wording.
            mes("You need a Magic level of $GUILD_MAGIC_LEVEL to enter the Wizards' Guild.")
            return
        }
        telejump(inside)
    }

    /** [tradeOp] is the npc's Trade op in the cache: 3 for most, 5 for Aleck. */
    private fun ScriptContext.keeper(
        npc: NpcType,
        title: String,
        inv: InvType,
        greeting: String,
        tradeOp: Int = 3,
    ) {
        onOpNpc1(npc) { startDialogue(it.npc) { shopTalk(it.npc, title, inv, greeting) } }
        when (tradeOp) {
            3 -> onOpNpc3(npc) { shops.open(player, it.npc, title, inv) }
            5 -> onOpNpc5(npc) { shops.open(player, it.npc, title, inv) }
            else -> error("No trade op $tradeOp")
        }
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
        const val GUILD_MAGIC_LEVEL = 66

        val DUNGEON_NORTH = CoordGrid(2620, 9566)
        val DUNGEON_SOUTH = CoordGrid(2620, 9496)
    }
}
