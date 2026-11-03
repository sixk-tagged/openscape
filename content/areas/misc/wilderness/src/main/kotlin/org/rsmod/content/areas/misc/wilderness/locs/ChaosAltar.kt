package org.rsmod.content.areas.misc.wilderness.locs

import jakarta.inject.Inject
import org.rsmod.api.config.refs.stats
import org.rsmod.api.player.protect.ProtectedAccess
import org.rsmod.api.random.GameRandom
import org.rsmod.api.script.onOpLocU
import org.rsmod.api.type.refs.obj.ObjReferences
import org.rsmod.content.areas.misc.wilderness.configs.WildernessSeqs
import org.rsmod.content.areas.misc.wilderness.configs.wilderness_locs
import org.rsmod.game.loc.BoundLocInfo
import org.rsmod.game.type.obj.ObjType
import org.rsmod.map.CoordGrid
import org.rsmod.plugin.scripts.PluginScript
import org.rsmod.plugin.scripts.ScriptContext

/**
 * Offering bones at the Chaos Temple (hut) altar: 3.5 times the burying experience, and half the
 * bones are not used up. The other chaos altars only recharge Prayer.
 */
class ChaosAltar @Inject constructor(private val random: GameRandom) : PluginScript() {
    override fun ScriptContext.startup() {
        for ((bone, xp) in BONES) {
            onOpLocU(wilderness_locs.chaos_altar, bone) { offer(it.loc, bone, xp, it.invSlot) }
        }
    }

    private suspend fun ProtectedAccess.offer(
        altar: BoundLocInfo,
        bone: ObjType,
        xp: Double,
        slot: Int,
    ) {
        if (altar.coords != HUT_ALTAR) {
            mes("Nothing interesting happens.")
            return
        }
        val spared = random.of(2) == 0
        if (!spared && !invDel(inv, bone, count = 1, slot = slot).success) {
            return
        }
        anim(WildernessSeqs.pray)
        statAdvance(stats.prayer, xp * BONE_XP_MULTIPLIER)
        if (spared) {
            mes("The Dark Lord spares your sacrifice but still rewards you for your efforts.")
        }
    }

    private companion object {
        const val BONE_XP_MULTIPLIER = 3.5
        val HUT_ALTAR = CoordGrid(2947, 3820)

        // The experience for burying each bone (the prayer skill's table).
        val BONES: List<Pair<ObjType, Double>> =
            with(ChaosAltarBones) {
                listOf(
                    bones to 4.5,
                    bones_burnt to 4.5,
                    wolf_bones to 4.5,
                    bat_bones to 5.3,
                    big_bones to 15.0,
                    jogre_bones to 15.0,
                    zogre_bones to 22.5,
                    babydragon_bones to 30.0,
                    wyrm_bones to 50.0,
                    wyvern_bones to 72.0,
                    dragon_bones to 72.0,
                    drake_bones to 80.0,
                    lava_dragon_bones to 85.0,
                    hydra_bones to 110.0,
                    dagannoth_bones to 125.0,
                    superior_dragon_bones to 150.0,
                )
            }
    }
}

object ChaosAltarBones : ObjReferences() {
    val bones = find("bones")
    val bones_burnt = find("bones_burnt")
    val wolf_bones = find("wolf_bones")
    val bat_bones = find("bat_bones")
    val big_bones = find("big_bones")
    val jogre_bones = find("tbwt_jogre_bones")
    val zogre_bones = find("zogre_bones")
    val babydragon_bones = find("babydragon_bones")
    val wyrm_bones = find("wyrm_bones")
    val wyvern_bones = find("wyvern_bones")
    val dragon_bones = find("dragon_bones")
    val drake_bones = find("drake_bones")
    val lava_dragon_bones = find("lava_dragon_bones")
    val hydra_bones = find("hydra_bones")
    val dagannoth_bones = find("dagannoth_king_bones")
    val superior_dragon_bones = find("dragon_bones_superior")

    /** Every bone the altar takes. */
    val all: List<ObjType> =
        listOf(
            bones,
            bones_burnt,
            wolf_bones,
            bat_bones,
            big_bones,
            jogre_bones,
            zogre_bones,
            babydragon_bones,
            wyrm_bones,
            wyvern_bones,
            dragon_bones,
            drake_bones,
            lava_dragon_bones,
            hydra_bones,
            dagannoth_bones,
            superior_dragon_bones,
        )
}
