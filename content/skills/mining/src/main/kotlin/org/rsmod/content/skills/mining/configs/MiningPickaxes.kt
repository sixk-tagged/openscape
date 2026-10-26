package org.rsmod.content.skills.mining.configs

import org.rsmod.api.config.refs.objs
import org.rsmod.api.config.refs.params
import org.rsmod.api.type.editors.obj.ObjEditor
import org.rsmod.game.type.obj.ObjType
import org.rsmod.game.type.seq.SeqType

internal object MiningPickaxes : ObjEditor() {
    init {
        pickaxe(objs.bronze_pickaxe, level = 1, ticks = 8, MiningSeqs.bronze)
        pickaxe(MiningObjs.iron_pickaxe, level = 1, ticks = 7, MiningSeqs.iron)
        pickaxe(MiningObjs.steel_pickaxe, level = 6, ticks = 6, MiningSeqs.steel)
        pickaxe(MiningObjs.black_pickaxe, level = 11, ticks = 5, MiningSeqs.black)
        pickaxe(MiningObjs.mithril_pickaxe, level = 21, ticks = 5, MiningSeqs.mithril)
        pickaxe(MiningObjs.adamant_pickaxe, level = 31, ticks = 4, MiningSeqs.adamant)
        pickaxe(MiningObjs.rune_pickaxe, level = 41, ticks = 3, MiningSeqs.rune)
        pickaxe(MiningObjs.gilded_pickaxe, level = 41, ticks = 3, MiningSeqs.gilded)
        // Note: dragon/3a/infernal/crystal skip OSRS's occasional 2-tick roll; add it once
        // there is a way to obtain them.
        pickaxe(objs.dragon_pickaxe, level = 61, ticks = 3, MiningSeqs.dragon)
        pickaxe(objs.third_age_pickaxe, level = 61, ticks = 3, MiningSeqs.third_age)
        pickaxe(objs.infernal_pickaxe, level = 61, ticks = 3, MiningSeqs.infernal)
        pickaxe(objs.crystal_pickaxe, level = 71, ticks = 3, MiningSeqs.crystal)
    }

    private fun pickaxe(type: ObjType, level: Int, ticks: Int, anim: SeqType) {
        edit(type) {
            contentGroup = MiningContent.pickaxe
            param[params.levelrequire] = level
            param[params.skill_anim] = anim
            param[MiningParams.pickaxe_ticks] = ticks
        }
    }
}
