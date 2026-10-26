package org.rsmod.content.skills.runecraft.scripts

import org.rsmod.api.player.protect.ProtectedAccess
import org.rsmod.api.player.vars.VarPlayerIntMapSetter
import org.rsmod.api.player.vars.intVarp
import org.rsmod.api.script.onOpLoc1
import org.rsmod.api.script.onOpNpc3
import org.rsmod.api.script.onOpNpc4
import org.rsmod.api.script.onPlayerLogin
import org.rsmod.content.skills.runecraft.configs.RunecraftLocs
import org.rsmod.content.skills.runecraft.configs.RunecraftNpcs
import org.rsmod.content.skills.runecraft.configs.RunecraftSeqs
import org.rsmod.content.skills.runecraft.configs.RunecraftSpotanims
import org.rsmod.content.skills.runecraft.configs.RunecraftVarBits
import org.rsmod.content.skills.runecraft.configs.RunecraftVarps
import org.rsmod.game.entity.Npc
import org.rsmod.game.entity.Player
import org.rsmod.game.type.varbit.VarBitType
import org.rsmod.map.CoordGrid
import org.rsmod.plugin.scripts.PluginScript
import org.rsmod.plugin.scripts.ScriptContext

private var Player.runeMysteries: Int by intVarp(RunecraftVarps.rune_mysteries)

private var Player.essenceMineOrigin: Int by intVarp(RunecraftVarps.essence_mine_origin)

/** The wizards who send players to the Rune Essence Mine, and where its portals bring them back. */
enum class EssenceOrigin(val shout: String, val flag: VarBitType, val returnTile: CoordGrid) {
    Aubury("Senventior Disthine Molenko!", RunecraftVarBits.essence_aubury, CoordGrid(3253, 3401)),
    Sedridor(
        "Senventior disthine molenko!",
        RunecraftVarBits.essence_wizards_tower,
        CoordGrid(3103, 9570),
    ),
    Distentor(
        "Senventior Disthine Molenko!",
        RunecraftVarBits.essence_wizards_guild,
        CoordGrid(2594, 3088),
    ),
    Cromperty(
        "Senventior Disthine Molenko!",
        RunecraftVarBits.essence_cromperty,
        CoordGrid(2684, 3326),
    ),
    Brimstail(
        "Senventior Disthine Molenko!",
        RunecraftVarBits.essence_brimstail,
        CoordGrid(2410, 9817),
    ),
}

/** The wizard's shout and Teleport Other casting, then the player appears in the mine. */
suspend fun ProtectedAccess.sendToEssenceMine(npc: Npc, origin: EssenceOrigin) {
    npc.say(origin.shout)
    // UNVERIFIED: the cache has no essence-mine entries; this is the Teleport Other set.
    npc.anim(RunecraftSeqs.teleport_other_cast)
    npc.spotanim(RunecraftSpotanims.teleport_other_cast)
    anim(RunecraftSeqs.teleport_other_impact)
    spotanim(RunecraftSpotanims.teleport_other_impact)
    delay(3)
    player.essenceMineOrigin = origin.ordinal
    VarPlayerIntMapSetter.set(player, origin.flag, 1)
    teleport(EssenceMine.ARRIVAL)
}

/**
 * The Rune Essence Mine: reached by the `Teleport` option of Aubury, Archmage Sedridor, Wizard
 * Distentor, Wizard Cromperty and Brimstail, left by any portal, which returns the player to
 * whoever sent them. Quests count as met, so Rune Mysteries is marked complete on login: that is
 * what shows the `Teleport` option, which the client takes from the npcs' multinpc.
 */
class EssenceMine : PluginScript() {
    override fun ScriptContext.startup() {
        onPlayerLogin { player.runeMysteries = maxOf(player.runeMysteries, RUNE_MYSTERIES_DONE) }
        onOpNpc4(RunecraftNpcs.aubury) { sendToEssenceMine(it.npc, EssenceOrigin.Aubury) }
        onOpNpc3(RunecraftNpcs.archmage_sedridor) {
            sendToEssenceMine(it.npc, EssenceOrigin.Sedridor)
        }
        onOpNpc3(RunecraftNpcs.wizard_distentor) {
            sendToEssenceMine(it.npc, EssenceOrigin.Distentor)
        }
        onOpNpc3(RunecraftNpcs.wizard_cromperty) {
            sendToEssenceMine(it.npc, EssenceOrigin.Cromperty)
        }
        onOpNpc3(RunecraftNpcs.brimstail) { sendToEssenceMine(it.npc, EssenceOrigin.Brimstail) }
        onOpLoc1(RunecraftLocs.mine_portal) { leave() }
    }

    private fun ProtectedAccess.leave() {
        val origin =
            EssenceOrigin.entries.getOrElse(player.essenceMineOrigin) { EssenceOrigin.Aubury }
        teleport(origin.returnTile)
    }

    companion object {
        const val RUNE_MYSTERIES_DONE = 6

        /** UNVERIFIED: an open tile in the middle chamber, picked from the map's collision. */
        val ARRIVAL = CoordGrid(2911, 4832)
    }
}
