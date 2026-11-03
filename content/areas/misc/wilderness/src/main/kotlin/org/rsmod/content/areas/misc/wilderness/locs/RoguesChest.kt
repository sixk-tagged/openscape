package org.rsmod.content.areas.misc.wilderness.locs

import jakarta.inject.Inject
import org.rsmod.api.config.refs.objs
import org.rsmod.api.config.refs.stats
import org.rsmod.api.player.protect.ProtectedAccess
import org.rsmod.api.player.stat.thievingLvl
import org.rsmod.api.random.GameRandom
import org.rsmod.api.repo.loc.LocRepository
import org.rsmod.api.script.onOpLoc1
import org.rsmod.api.script.onOpLoc2
import org.rsmod.api.type.refs.inv.InvReferences
import org.rsmod.api.type.refs.obj.ObjReferences
import org.rsmod.content.areas.misc.wilderness.configs.wilderness_locs
import org.rsmod.game.hit.HitType
import org.rsmod.game.loc.BoundLocInfo
import org.rsmod.game.type.obj.ObjType
import org.rsmod.plugin.scripts.PluginScript
import org.rsmod.plugin.scripts.ScriptContext

/**
 * The three chests at the bottom of the Rogues' Castle: Thieving 84 opens them for experience and a
 * roll on the loot table; anyone lower is hit by the trap. Open and Search for traps do the same.
 * The loot goes straight into an open looting bag when the thief has one. The level 135 rogues
 * turning on the thief and the diaries' bigger hauls are not built.
 */
class RoguesChest
@Inject
constructor(private val locRepo: LocRepository, private val random: GameRandom) : PluginScript() {
    override fun ScriptContext.startup() {
        onOpLoc1(wilderness_locs.rogue_chest) { unlock(it.loc) }
        onOpLoc2(wilderness_locs.rogue_chest) { unlock(it.loc) }
    }

    private fun ProtectedAccess.unlock(chest: BoundLocInfo) {
        if (player.thievingLvl < THIEVING_LEVEL) {
            // Note: the message is not on the wiki.
            mes("You trigger a trap on the chest!")
            takeInstantHit(HitType.Typeless, random.of(TRAP_MIN, TRAP_MAX))
            return
        }
        val hasOpenBag = inv.any { it?.id == RogueChestObjs.looting_bag_open.id }
        val bag = if (hasOpenBag) inv(RogueChestInvs.looting_bag) else null
        if (inv.isFull() && (bag == null || bag.isFull())) {
            mes("You don't have enough inventory space.")
            return
        }
        locRepo.change(chest, wilderness_locs.rogue_chest_open, RESTOCK_TICKS)
        statAdvance(stats.thieving, THIEVING_XP)
        val (loot, count) = rollLoot()
        if (bag == null || invAdd(bag, loot, count).failure) {
            invAdd(inv, loot, count)
        }
        mes("You find some treasure in the chest.")
    }

    private fun rollLoot(): Pair<ObjType, Int> {
        var roll = random.of(LOOT_TOTAL)
        for ((obj, weight, count) in LOOT) {
            if (roll < weight) {
                return obj to count
            }
            roll -= weight
        }
        error("Loot weights do not add up to $LOOT_TOTAL")
    }

    private companion object {
        const val THIEVING_LEVEL = 84
        const val THIEVING_XP = 701.7
        const val RESTOCK_TICKS = 34
        const val TRAP_MIN = 16
        const val TRAP_MAX = 43
        const val LOOT_TOTAL = 66

        // The wiki's base quantities, stackable or noted; the diaries' bonuses are out of scope.
        val LOOT: List<Triple<ObjType, Int, Int>> =
            with(RogueChestObjs) {
                listOf(
                    Triple(naturerune, 11, 30),
                    Triple(cert_red_spiders_eggs, 10, 4),
                    Triple(lawrune, 10, 30),
                    Triple(cert_coal, 8, 15),
                    Triple(objs.coins, 6, 3_375),
                    Triple(cert_vile_ashes, 3, 11),
                    Triple(cert_uncut_diamond, 3, 2),
                    Triple(cert_uncut_diamond, 2, 3),
                    Triple(cert_uncut_emerald, 2, 7),
                    Triple(blighted_sack_icebarrage, 2, 9),
                    Triple(prayer_potion_2, 2, 1),
                    Triple(cert_iron_ore, 1, 30),
                    Triple(chaosrune, 1, 45),
                    Triple(deathrune, 1, 37),
                    Triple(cert_blighted_mantaray, 1, 15),
                    Triple(cert_blighted_anglerfish, 1, 11),
                    Triple(cert_uncut_sapphire, 1, 11),
                    Triple(cert_dragonstone, 1, 2),
                )
            }
    }
}

object RogueChestInvs : InvReferences() {
    /** What a looting bag holds. */
    val looting_bag = find("looting_bag")
}

object RogueChestObjs : ObjReferences() {
    val looting_bag_open = find("looting_bag_open")
    val naturerune = find("naturerune")
    val lawrune = find("lawrune")
    val chaosrune = find("chaosrune")
    val deathrune = find("deathrune")
    val cert_red_spiders_eggs = find("cert_red_spiders_eggs")
    val cert_coal = find("cert_coal")
    val cert_vile_ashes = find("cert_vile_ashes")
    val cert_uncut_diamond = find("cert_uncut_diamond")
    val cert_uncut_emerald = find("cert_uncut_emerald")
    val cert_uncut_sapphire = find("cert_uncut_sapphire")
    val cert_iron_ore = find("cert_iron_ore")
    val cert_dragonstone = find("cert_dragonstone")
    val cert_blighted_mantaray = find("cert_blighted_mantaray")
    val cert_blighted_anglerfish = find("cert_blighted_anglerfish")
    val blighted_sack_icebarrage = find("blighted_sack_icebarrage")
    val prayer_potion_2 = find("2doseprayerrestore")
}
