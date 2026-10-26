package org.rsmod.content.skills.prayer

import org.rsmod.api.config.refs.seqs
import org.rsmod.api.config.refs.stats
import org.rsmod.api.config.refs.varbits
import org.rsmod.api.player.protect.ProtectedAccess
import org.rsmod.api.player.stat.stat
import org.rsmod.api.player.stat.statBase
import org.rsmod.api.player.stat.statRestore
import org.rsmod.api.script.onOpHeld1
import org.rsmod.api.script.onOpLoc1
import org.rsmod.api.type.refs.loc.LocReferences
import org.rsmod.api.type.refs.obj.ObjReferences
import org.rsmod.api.type.refs.seq.SeqReferences
import org.rsmod.game.inv.Inventory
import org.rsmod.game.type.obj.ObjType
import org.rsmod.plugin.scripts.PluginScript
import org.rsmod.plugin.scripts.ScriptContext

/** Burying bones (op1 on every bone type) and praying at altars. */
class Prayer : PluginScript() {
    override fun ScriptContext.startup() {
        for ((bone, xp) in bones) {
            onOpHeld1(bone) { bury(bone, xp, it.slot, it.inventory) }
        }
        onOpLoc1(prayer_locs.altar) { prayAt() }
    }

    private suspend fun ProtectedAccess.bury(bone: ObjType, xp: Double, slot: Int, inv: Inventory) {
        if (!invDel(inv, bone, count = 1, slot = slot).success) {
            return
        }
        anim(seqs.human_pickupfloor)
        mes("You dig a hole in the ground...")
        delay(1)
        mes("You bury the bones.")
        statAdvance(stats.prayer, xp)
    }

    private fun ProtectedAccess.prayAt() {
        if (player.stat(stats.prayer) >= player.statBase(stats.prayer)) {
            mes("You already have full Prayer points.")
            return
        }
        anim(prayer_seqs.human_pray)
        player.statRestore(stats.prayer)
        vars[varbits.prayer_drain_counter] = 0
        mes("You recharge your Prayer points.")
    }

    private val bones: Map<ObjType, Double> by lazy {
        with(prayer_objs) {
            mapOf(
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

object prayer_objs : ObjReferences() {
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
}

object prayer_locs : LocReferences() {
    val altar = find("altar")
}

object prayer_seqs : SeqReferences() {
    val human_pray = find("human_pray")
}
