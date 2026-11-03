package org.rsmod.content.areas.misc.wilderness.locs

import jakarta.inject.Inject
import org.rsmod.api.player.protect.ProtectedAccess
import org.rsmod.api.random.GameRandom
import org.rsmod.api.repo.loc.LocRepository
import org.rsmod.api.script.onOpLoc1
import org.rsmod.api.type.refs.obj.ObjReferences
import org.rsmod.content.areas.misc.wilderness.configs.wilderness_locs
import org.rsmod.game.loc.BoundLocInfo
import org.rsmod.game.type.obj.ObjType
import org.rsmod.plugin.scripts.PluginScript
import org.rsmod.plugin.scripts.ScriptContext

/**
 * The muddy chest in the middle of the Lava Maze: a muddy key (the chaos dwarves drop them) buys
 * five fixed items and, 3 times in 40, one more from the rare table. Larran's key, one of the rare
 * table's lines, is not built.
 *
 * Note: the messages and how long the chest stays open are not on the wiki.
 */
class MuddyChest
@Inject
constructor(private val locRepo: LocRepository, private val random: GameRandom) : PluginScript() {
    override fun ScriptContext.startup() {
        onOpLoc1(wilderness_locs.muddy_chest) { open(it.loc) }
    }

    private fun ProtectedAccess.open(chest: BoundLocInfo) {
        if (invTotal(inv, MuddyChestObjs.muddy_key) == 0) {
            mes("The chest is locked.")
            return
        }
        // Room for every item, counting none of them as stacking with what is carried.
        if (inv.freeSpace() < ALWAYS.size + 1) {
            mes("You don't have enough inventory space.")
            return
        }
        invDel(inv, MuddyChestObjs.muddy_key, 1)
        locRepo.change(chest, wilderness_locs.muddy_chest_open, OPEN_TICKS)
        mes("You unlock the chest with your key.")
        for ((obj, count) in ALWAYS) {
            invAdd(inv, obj, count)
        }
        rare()?.let { (obj, count) -> invAdd(inv, obj, count) }
        mes("You find some treasure in the chest!")
    }

    /** One more item 3 times in 40, picked by the rare table's weights. */
    private fun rare(): Pair<ObjType, Int>? {
        if (random.of(RARE_CHANCE_TOTAL) >= RARE_CHANCE) {
            return null
        }
        var roll = random.of(RARE_TOTAL)
        for ((obj, weight, count) in RARE) {
            if (roll < weight) {
                return obj to count
            }
            roll -= weight
        }
        return null
    }

    private companion object {
        const val OPEN_TICKS = 10
        const val RARE_CHANCE = 3
        const val RARE_CHANCE_TOTAL = 40
        const val RARE_TOTAL = 21

        val ALWAYS: List<Pair<ObjType, Int>> =
            with(MuddyChestObjs) {
                listOf(
                    uncut_ruby to 1,
                    cert_mithril_bar to 2,
                    lawrune to 5,
                    deathrune to 5,
                    chaosrune to 15,
                )
            }

        // (obj, weight out of 21, quantity); Larran's key takes the last 6. The wiki prints these
        // out of 280 per opening: 21/280 is the 3 in 40.
        val RARE: List<Triple<ObjType, Int, Int>> =
            with(MuddyChestObjs) {
                listOf(
                    Triple(cert_blighted_mantaray, 3, 25),
                    Triple(cert_blighted_karambwan, 3, 25),
                    Triple(blighted_sack_icebarrage, 3, 25),
                    Triple(cert_blighted_anglerfish, 3, 15),
                    Triple(cert_blighted_super_restore, 3, 3),
                )
            }
    }
}

object MuddyChestObjs : ObjReferences() {
    val muddy_key = find("muddy_key")
    val uncut_ruby = find("uncut_ruby")
    val cert_mithril_bar = find("cert_mithril_bar")
    val lawrune = find("lawrune")
    val deathrune = find("deathrune")
    val chaosrune = find("chaosrune")
    val cert_blighted_mantaray = find("cert_blighted_mantaray")
    val cert_blighted_karambwan = find("cert_blighted_karambwan")
    val blighted_sack_icebarrage = find("blighted_sack_icebarrage")
    val cert_blighted_anglerfish = find("cert_blighted_anglerfish")
    val cert_blighted_super_restore = find("cert_blighted_4dose2restore")
}
