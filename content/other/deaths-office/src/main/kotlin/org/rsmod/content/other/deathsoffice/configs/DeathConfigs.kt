package org.rsmod.content.other.deathsoffice.configs

import org.rsmod.api.type.builders.map.npc.MapNpcSpawnBuilder
import org.rsmod.api.type.builders.varp.VarpBuilder
import org.rsmod.api.type.editors.inv.InvEditor
import org.rsmod.api.type.editors.npc.NpcEditor
import org.rsmod.api.type.refs.comp.ComponentReferences
import org.rsmod.api.type.refs.interf.InterfaceReferences
import org.rsmod.api.type.refs.inv.InvReferences
import org.rsmod.api.type.refs.loc.LocReferences
import org.rsmod.api.type.refs.npc.NpcReferences
import org.rsmod.api.type.refs.queue.QueueReferences
import org.rsmod.api.type.refs.seq.SeqReferences
import org.rsmod.api.type.refs.timer.TimerReferences
import org.rsmod.api.type.refs.varbit.VarBitReferences
import org.rsmod.api.type.refs.varp.VarpReferences
import org.rsmod.content.other.deathsoffice.DeathsOfficeScript
import org.rsmod.game.type.inv.InvScope
import org.rsmod.game.type.npc.NpcType

object DeathInvs : InvReferences() {
    val gravestone = find("gravestone")
    val death_permanent = find("death_permanent")
}

internal object DeathInvEditor : InvEditor() {
    init {
        edit(DeathInvs.gravestone) {
            scope = InvScope.Perm
            protect = false
        }
        edit(DeathInvs.death_permanent) {
            scope = InvScope.Perm
            protect = false
        }
    }
}

object DeathVarps : VarpReferences() {
    /** Logged-in ticks until the gravestone collapses; 0 means there is no grave. */
    val gravestone_ticks = find("gravestone_ticks")
    /** Packed [org.rsmod.map.CoordGrid] of the gravestone. */
    val gravestone_coord = find("gravestone_coord")
    val death_coffer = find("death_coffer")
    /** 0 never died; 1 in the tutorial; topic bits 2, 4 and 16; 8 tutorial finished. */
    val death_tutorial = find("death_tutorial")
    /**
     * Coins of fee already paid for what is in the grave now; it is unlocked once that covers it.
     */
    val gravestone_paid = find("gravestone_paid")
    /** Packed [org.rsmod.map.CoordGrid] the office's portal leads back to; 0 means Lumbridge. */
    val death_office_return = find("death_office_return")
}

internal object DeathVarpBuilder : VarpBuilder() {
    init {
        build("gravestone_ticks") { permanent = true }
        build("gravestone_coord") { permanent = true }
        build("death_coffer") { permanent = true }
        build("death_tutorial") { permanent = true }
        build("gravestone_paid") { permanent = true }
        build("death_office_return") { permanent = true }
    }
}

object DeathVarBits : VarBitReferences() {
    /** Which of the 255 gravestone npcs this player can see; 0 for none. */
    val gravestone_visible = find("gravestone_visible")
    /** Ticks shown on the HUD grave timer (hidden at 0). */
    val gravestone_duration = find("gravestone_duration")
    /** First grave slot (+1) of the free items, then of the 1k-fee and the 10k-fee items. */
    val threshold_free = find("gravestone_feethreshold_under100k")
    val threshold_1k = find("gravestone_feethreshold_under1m")
    val threshold_10k = find("gravestone_feethreshold_under10m")
}

object DeathTimers : TimerReferences() {
    val gravestone = find("gravestone")
}

object DeathQueues : QueueReferences() {
    /** Starts Death's first-death tutorial once the player is in his office. */
    val tutorial = find("death_tutorial")
}

object DeathNpcs : NpcReferences() {
    val death = find("death_office_death")
    /** What every grave slot npc shows its owner. */
    val grave = find("gravestone_default")
    /** Slot N (1-255) shows the grave only to a player whose `gravestone_visible` is N. */
    val graves: List<NpcType> = (1..255).map { find("gravestone_default_%03d".format(it)) }
}

internal object DeathNpcEditor : NpcEditor() {
    init {
        DeathNpcs.graves.forEach { edit(it) { defaultMode = none } }
    }
}

internal object DeathNpcSpawns : MapNpcSpawnBuilder() {
    override fun onPackMapTask() {
        resourceFile<DeathsOfficeScript>("npcs.toml")
    }
}

object DeathLocs : LocReferences() {
    /** The grave in Lumbridge's graveyard that leads into Death's Office. */
    val lumbridge_grave = find("death_office_access_grave")
    /** The white coffin in the crypt under Falador's White Knights' Castle. */
    val falador_coffin = find("death_office_access_whitecoffin")
    val exit_portal = find("death_office_exitportal")
    val coffer = find("death_coffer_access")
}

object CofferInterfaces : InterfaceReferences() {
    val main = find("death_coffer")
    val side = find("death_coffer_side")
}

object CofferComponents : ComponentReferences() {
    val side_items = find("death_coffer_side:items")
    val quantity1 = find("death_coffer:1")
    val quantity5 = find("death_coffer:5")
    val quantityX = find("death_coffer:x")
    val quantityAll = find("death_coffer:all")
    val confirm = find("death_coffer:confirm")
}

/** The interface scratch varps `death_coffer` reads (as var261-264 in its cs2). */
object CofferVarps : VarpReferences() {
    val coffer_shown = find("if1")
    /** Selected backpack slot, or -1. */
    val selected_slot = find("if2")
    /** Chosen quantity, [Int.MAX_VALUE] for All, 0 for none yet. */
    val quantity = find("if3")
    /** Coins per item, below 10,000 (or 1 for untradeables) when Death refuses it. */
    val value_each = find("if4")
}

object DeathSeqs : SeqReferences() {
    val portal_exit = find("death_office_portal_exit")
}

object DeathOfficeInterfaces : InterfaceReferences() {
    val retrieval = find("death_office")
}

object DeathOfficeComponents : ComponentReferences() {
    val items = find("death_office:items")
    val take1 = find("death_office:1")
    val take5 = find("death_office:5")
    val takeX = find("death_office:x")
    val takeAll = find("death_office:all")
    /** Takes every stored item, not just the selected one. */
    val takeEverything = find("death_office:takeall")
}

/** The interface scratch varps `death_office` reads (as var261-263 in its cs2). */
object DeathOfficeVarps : VarpReferences() {
    val coffer_shown = find("if1")
    /** Selected `death_permanent` slot, or -1. */
    val selected_slot = find("if2")
    val fee_each = find("if3")
}

object GraveInterfaces : InterfaceReferences() {
    val loot = find("gravestone_generic")
}

object GraveComponents : ComponentReferences() {
    val freeitems = find("gravestone_generic:freeitems")
    val freebutton = find("gravestone_generic:freebutton")
    val payitems = find("gravestone_generic:payitems")
    val paybutton = find("gravestone_generic:paybutton")
}
