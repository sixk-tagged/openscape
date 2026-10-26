package org.rsmod.content.skills.mining.configs

import org.rsmod.api.config.refs.params
import org.rsmod.api.config.refs.stats
import org.rsmod.api.type.editors.obj.ObjEditor
import org.rsmod.game.type.obj.ObjType

/** Rewards stay with whoever earned them: only golden nuggets can change hands. */
internal object MiningRewardObjs : ObjEditor() {
    init {
        edit(MiningObjs.prospector_helmet) { tradeable = false }
        edit(MiningObjs.prospector_jacket) { tradeable = false }
        edit(MiningObjs.prospector_legs) { tradeable = false }
        edit(MiningObjs.prospector_boots) { tradeable = false }
        gloves(MiningObjs.mining_gloves, level = 20)
        gloves(MiningObjs.superior_mining_gloves, level = 55)
        gloves(MiningObjs.expert_mining_gloves, level = 70)
    }

    private fun gloves(type: ObjType, level: Int) {
        edit(type) {
            tradeable = false
            param[params.statreq1_skill] = stats.mining
            param[params.statreq1_level] = level
        }
    }
}
