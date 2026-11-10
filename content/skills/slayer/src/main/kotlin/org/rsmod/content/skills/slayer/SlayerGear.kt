package org.rsmod.content.skills.slayer

import jakarta.inject.Inject
import org.rsmod.api.area.checker.AreaChecker
import org.rsmod.api.config.refs.stats
import org.rsmod.api.config.refs.varbits
import org.rsmod.api.config.refs.varps
import org.rsmod.api.player.protect.ProtectedAccess
import org.rsmod.api.player.stat.statBase
import org.rsmod.api.player.teleportRefusal
import org.rsmod.api.script.onOpHeld1
import org.rsmod.api.script.onOpHeld2
import org.rsmod.api.script.onOpHeld3
import org.rsmod.api.script.onOpHeld4
import org.rsmod.api.script.onOpHeldU
import org.rsmod.api.type.refs.obj.ObjReferences
import org.rsmod.api.type.refs.seq.SeqReferences
import org.rsmod.api.type.refs.spot.SpotanimReferences
import org.rsmod.game.type.obj.ObjType
import org.rsmod.map.CoordGrid
import org.rsmod.plugin.scripts.PluginScript
import org.rsmod.plugin.scripts.ScriptContext

/**
 * The Slayer helmet (wiki "Slayer helmet"): with "Malevolent Masquerade" unlocked and 55 Crafting,
 * using any part on another while all six are in the inventory makes one (an imbued black mask
 * makes the imbued helmet); Disassemble gives the parts back and Check shows the task. The Slayer
 * ring (wiki "Slayer ring"): Rub and Teleport take it to the Stronghold Slayer Cave, the Slayer
 * Tower or the Fremennik Slayer Dungeon for one of its eight charges (up to level 30 Wilderness);
 * the last charge leaves an enchanted gem. Check shows the task.
 */
class SlayerGear
@Inject
internal constructor(private val tasks: SlayerTasks, private val areaChecker: AreaChecker) :
    PluginScript() {
    override fun ScriptContext.startup() {
        val parts = GearObjs.masks + GearObjs.imbued_masks + GearObjs.other_parts
        for ((i, first) in parts.withIndex()) {
            for (second in parts.drop(i + 1)) {
                if (first.isMask() && second.isMask()) {
                    continue
                }
                onOpHeldU(first, second) { assemble() }
            }
        }
        onOpHeld3(GearObjs.slayer_helm) { checkSlayerTask(tasks) }
        onOpHeld3(GearObjs.slayer_helm_i) { checkSlayerTask(tasks) }
        onOpHeld4(GearObjs.slayer_helm) { disassemble(GearObjs.slayer_helm, GearObjs.mask) }
        onOpHeld4(GearObjs.slayer_helm_i) {
            disassemble(GearObjs.slayer_helm_i, GearObjs.imbued_mask)
        }

        // The ring's Wear is op1 (op2 is Rub), which has no default equip.
        for (ring in GearObjs.rings + GearObjs.eternal_ring) {
            onOpHeld1(ring) { invEquip(it.slot) }
        }
        for ((charges, ring) in GearObjs.rings.withIndex()) {
            onOpHeld2(ring) { teleportMenu(ring, charges + 1) }
            onOpHeld3(ring) { teleportMenu(ring, charges + 1) }
            onOpHeld4(ring) { checkSlayerTask(tasks) }
        }
        onOpHeld2(GearObjs.eternal_ring) { teleportMenu(GearObjs.eternal_ring, null) }
        onOpHeld3(GearObjs.eternal_ring) { teleportMenu(GearObjs.eternal_ring, null) }
        onOpHeld4(GearObjs.eternal_ring) { checkSlayerTask(tasks) }
    }

    // ---- Slayer helmet

    private fun ObjType.isMask(): Boolean =
        (GearObjs.masks + GearObjs.imbued_masks).any { it.id == id }

    // UNVERIFIED: every message here.
    private fun ProtectedAccess.assemble() {
        if (!player.hasSlayerUnlock(MASQUERADE_BIT)) {
            mes("You need to learn how to make a Slayer helmet from a Slayer Master first.")
            return
        }
        if (player.statBase(stats.crafting) < HELMET_LEVEL) {
            mes("You need a Crafting level of $HELMET_LEVEL to make a Slayer helmet.")
            return
        }
        val imbued = GearObjs.imbued_masks.firstOrNull { it in inv }
        val mask = imbued ?: GearObjs.masks.firstOrNull { it in inv }
        if (mask == null || GearObjs.other_parts.any { it !in inv }) {
            mes(
                "You need a black mask, earmuffs, a facemask, a nose peg, a spiny helmet and an " +
                    "enchanted gem to make a Slayer helmet."
            )
            return
        }
        invDel(inv, mask)
        for (part in GearObjs.other_parts) {
            invDel(inv, part)
        }
        invAdd(inv, if (imbued != null) GearObjs.slayer_helm_i else GearObjs.slayer_helm)
        mes("You combine the parts into a Slayer helmet.")
    }

    private fun ProtectedAccess.disassemble(helm: ObjType, mask: ObjType) {
        if (inv.freeSpace() < GearObjs.other_parts.size) {
            mes("You need ${GearObjs.other_parts.size} free inventory spaces to do that.")
            return
        }
        invDel(inv, helm)
        invAdd(inv, mask)
        for (part in GearObjs.other_parts) {
            invAdd(inv, part)
        }
    }

    // ---- Slayer ring

    /** [charges] is null for the eternal ring. */
    private suspend fun ProtectedAccess.teleportMenu(ring: ObjType, charges: Int?) {
        var picked: CoordGrid? = null
        startDialogue {
            picked =
                choice4(
                    "Stronghold Slayer Cave.",
                    STRONGHOLD,
                    "Slayer Tower.",
                    TOWER,
                    "Fremennik Slayer Dungeon.",
                    FREMENNIK,
                    "Nowhere.",
                    null,
                    title = "Teleport to:",
                )
        }
        val dest = picked ?: return
        val refusal = player.teleportRefusal(areaChecker, limit = WILDERNESS_LIMIT)
        if (refusal != null) {
            mes(refusal)
            return
        }
        if (charges != null && ring !in inv) {
            return
        }
        anim(GearSeqs.teleport)
        spotanim(GearSpots.teleport, height = 92)
        delay(TELEPORT_TICKS)
        telejump(dest)
        anim(GearSeqs.teleport_end)
        if (charges == null) {
            return
        }
        if (charges > 1) {
            invReplace(inv, ring, 1, GearObjs.rings[charges - 2])
            mes("Your Slayer ring has ${charges - 1} uses left.")
        } else {
            invReplace(inv, ring, 1, GearObjs.enchanted_gem)
            mes("Your Slayer ring crumbles, leaving an enchanted gem behind.")
        }
    }

    private companion object {
        const val MASQUERADE_BIT = 5
        const val HELMET_LEVEL = 55
        const val WILDERNESS_LIMIT = 30
        const val TELEPORT_TICKS = 3

        val STRONGHOLD = CoordGrid(2431, 3422)
        val TOWER = CoordGrid(3421, 3537)
        val FREMENNIK = CoordGrid(2802, 9999)
    }
}

/** The enchanted gem's, Slayer helmet's and Slayer ring's Check. */
internal fun ProtectedAccess.checkSlayerTask(tasks: SlayerTasks) {
    val count = vars[varps.slayer_count]
    if (count <= 0) {
        mes("You need something new to hunt.")
        return
    }
    val name = tasks.byId[vars[varps.slayer_target]]?.name ?: "monsters"
    if (vars[varbits.slayer_master] == SlayerMaster.Krystilia.id) {
        mes("You're assigned to kill $name in the Wilderness; only $count more to go.")
    } else {
        mes("You're assigned to kill $name; only $count more to go.")
    }
}

object GearObjs : ObjReferences() {
    val mask = find("harmless_black_mask")
    val imbued_mask = find("nzone_black_mask")
    val masks = listOf(mask) + (1..10).map { find("harmless_black_mask_$it") }
    val imbued_masks = listOf(imbued_mask) + (1..10).map { find("nzone_black_mask_$it") }
    val enchanted_gem = find("slayer_gem")
    val other_parts =
        listOf(
            find("slayer_earmuffs"),
            find("slayer_facemask"),
            find("slayer_nosepeg"),
            find("wallbeast_spike_helmet"),
            enchanted_gem,
        )
    val slayer_helm = find("slayer_helm")
    val slayer_helm_i = find("slayer_helm_i")

    /** Charges 1 to 8. */
    val rings = (1..8).map { find("slayer_ring_$it") }
    val eternal_ring = find("slayer_ring_eternal")
}

internal object GearSeqs : SeqReferences() {
    val teleport = find("human_castteleport")
    val teleport_end = find("human_castteleport_reverse")
}

internal object GearSpots : SpotanimReferences() {
    val teleport = find("teleport_casting")
}
