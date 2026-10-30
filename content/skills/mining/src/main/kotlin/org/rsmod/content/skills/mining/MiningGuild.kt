package org.rsmod.content.skills.mining

import org.rsmod.api.config.refs.stats
import org.rsmod.api.stats.levelmod.InvisibleLevelMod
import org.rsmod.api.type.builders.varp.VarpBuilder
import org.rsmod.api.type.refs.varp.VarpReferences
import org.rsmod.content.skills.mining.configs.MiningObjs
import org.rsmod.game.entity.Player
import org.rsmod.game.type.obj.ObjType
import org.rsmod.map.CoordGrid

/**
 * The Mining Guild under Falador (both halves): an invisible +7 Mining boost, rocks that respawn in
 * half the time, and unidentified minerals for Belona's exchange.
 */
object MiningGuild {
    const val LEVEL_BOOST = 7

    // UNVERIFIED: the wiki lists the boost, the faster respawns and the minerals under the
    // members' half; every account is a member, so the whole guild gets them.
    fun contains(coords: CoordGrid): Boolean =
        coords.level == 0 && coords.x in 3008..3061 && coords.z in 9690..9756

    /** The chance (1 in n) of an unidentified mineral with each ore, or null for none. */
    fun mineralChance(ore: ObjType): Int? =
        when (ore.id) {
            MiningObjs.iron_ore.id -> 100
            MiningObjs.coal.id -> 60
            MiningObjs.mithril_ore.id -> 40
            MiningObjs.adamantite_ore.id -> 30
            MiningObjs.runite_ore.id -> 20
            MiningObjs.amethyst.id -> 20
            else -> null
        }
}

class MiningGuildBoost : InvisibleLevelMod(stats.mining) {
    override fun Player.calculateBoost(): Int =
        if (MiningGuild.contains(coords)) MiningGuild.LEVEL_BOOST else 0
}

object MiningGuildVarps : VarpReferences() {
    /** Set by Belona's Toggle-minerals: no unidentified minerals while mining. */
    val minerals_off = find("mguild_minerals_off")
}

internal object MiningGuildVarpBuilder : VarpBuilder() {
    init {
        build("mguild_minerals_off")
    }
}
