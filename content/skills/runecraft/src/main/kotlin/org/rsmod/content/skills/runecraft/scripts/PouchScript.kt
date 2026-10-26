package org.rsmod.content.skills.runecraft.scripts

import jakarta.inject.Inject
import org.rsmod.api.player.protect.ProtectedAccess
import org.rsmod.api.player.stat.baseRunecraftingLvl
import org.rsmod.api.player.vars.VarPlayerIntMapSetter
import org.rsmod.api.script.onOpHeld1
import org.rsmod.api.script.onOpHeld2
import org.rsmod.api.script.onOpHeld3
import org.rsmod.content.skills.runecraft.configs.Pouch
import org.rsmod.content.skills.runecraft.configs.Pouches
import org.rsmod.content.skills.runecraft.configs.RunecraftObjs
import org.rsmod.game.type.obj.ObjType
import org.rsmod.game.type.obj.ObjTypeList
import org.rsmod.plugin.scripts.PluginScript
import org.rsmod.plugin.scripts.ScriptContext

/**
 * Essence pouches: `Fill` moves the matching essence in, `Empty` takes it out, `Check` reports. A
 * pouch holds one type of essence at a time. Medium, large, giant and colossal pouches wear down as
 * essence is stored, unless a Runecraft cape is worn, and lose capacity until they crumble.
 * Repairing them (the Dark mage in the Abyss) is not built.
 */
class PouchScript @Inject constructor(private val objTypes: ObjTypeList) : PluginScript() {
    override fun ScriptContext.startup() {
        for (pouch in Pouches.all) {
            for (obj in listOfNotNull(pouch.full, pouch.decayed)) {
                onOpHeld1(obj) { fill(pouch, it.slot) }
                onOpHeld2(obj) { empty(pouch) }
                onOpHeld3(obj) { checkContents(pouch) }
            }
        }
    }

    private fun ProtectedAccess.fill(pouch: Pouch, slot: Int) {
        val level = player.baseRunecraftingLvl
        if (level < pouch.level) {
            // UNVERIFIED wording.
            mes("You need a Runecrafting level of ${pouch.level} to use this pouch.")
            return
        }
        val stored = player.vars[pouch.count]
        val wear = pouch.wear?.let { player.vars[it] } ?: 0
        val capacity = pouch.capacity(level, wear) ?: 0
        if (stored >= capacity) {
            mes("Your ${pouch.name} pouch is full.")
            return
        }
        val essence = if (stored > 0) essenceOf(player.vars[pouch.type]) else pickEssence()
        val moved = minOf(capacity - stored, invTotal(inv, essence))
        if (moved == 0) {
            mes(
                if (stored > 0) {
                    "You can only keep one type of essence in a pouch at a time."
                } else {
                    "You don't have any essence to put in the pouch."
                }
            )
            return
        }
        invDel(inv, essence, moved)
        VarPlayerIntMapSetter.set(
            player,
            pouch.type,
            if (essence == RunecraftObjs.pure_essence) 1 else 0,
        )
        VarPlayerIntMapSetter.set(player, pouch.count, stored + moved)
        wearDown(pouch, slot, level, wear, moved)
    }

    private fun ProtectedAccess.pickEssence(): ObjType =
        if (RunecraftObjs.pure_essence in player.inv) {
            RunecraftObjs.pure_essence
        } else {
            RunecraftObjs.rune_essence
        }

    private fun essenceOf(type: Int): ObjType =
        if (type == 1) RunecraftObjs.pure_essence else RunecraftObjs.rune_essence

    /**
     * Storing essence wears the pouch, which can shrink it, swap it for the worn one, or end it.
     */
    private fun ProtectedAccess.wearDown(
        pouch: Pouch,
        slot: Int,
        level: Int,
        wear: Int,
        moved: Int,
    ) {
        val varp = pouch.wear ?: return
        if (RunecraftGear.wearsCape(player, objTypes)) {
            return
        }
        val added = if (pouch.randomWear) (1..moved).sumOf { random.of(4) } else moved
        val worn = wear + added
        VarPlayerIntMapSetter.set(player, varp, worn)
        val capacity = pouch.capacity(level, worn)
        if (capacity == null) {
            inv[slot]?.let { invDel(inv, objTypes[it], 1, slot = slot) }
            VarPlayerIntMapSetter.set(player, pouch.count, 0)
            VarPlayerIntMapSetter.set(player, varp, 0)
            // UNVERIFIED wording.
            mes("Your ${pouch.name} pouch has been destroyed.")
            return
        }
        val decayed = pouch.decayed
        if (
            decayed != null &&
                inv[slot]?.id == pouch.full.id &&
                worn >= pouch.tiers(level).first().below
        ) {
            invReplaceSlot(inv, slot, 1, decayed)
            mes("Your pouch has decayed through use.")
        }
        spillExcess(pouch, capacity)
    }

    /**
     * A capacity that shrank below the contents returns the surplus, or loses it if there is no
     * room.
     */
    private fun ProtectedAccess.spillExcess(pouch: Pouch, capacity: Int) {
        val stored = player.vars[pouch.count]
        if (stored <= capacity) {
            return
        }
        val surplus = stored - capacity
        val essence = essenceOf(player.vars[pouch.type])
        if (inv.freeSpace() >= surplus) {
            invAdd(inv, essence, surplus)
        }
        VarPlayerIntMapSetter.set(player, pouch.count, capacity)
    }

    private fun ProtectedAccess.empty(pouch: Pouch) {
        val stored = player.vars[pouch.count]
        if (stored == 0) {
            mes("There is no essence in this pouch.")
            return
        }
        val moved = minOf(stored, inv.freeSpace())
        if (moved == 0) {
            mes("You don't have any free space in your inventory.")
            return
        }
        invAdd(inv, essenceOf(player.vars[pouch.type]), moved)
        VarPlayerIntMapSetter.set(player, pouch.count, stored - moved)
    }

    private fun ProtectedAccess.checkContents(pouch: Pouch) {
        val stored = player.vars[pouch.count]
        // UNVERIFIED wording.
        if (stored == 0) {
            mes("There is no essence in this pouch.")
            return
        }
        val kind = if (player.vars[pouch.type] == 1) "pure" else "rune"
        val verb = if (stored == 1) "is" else "are"
        mes("There $verb $stored $kind essence in this pouch.")
    }
}
