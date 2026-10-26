package org.rsmod.content.skills.runecraft.scripts

import org.rsmod.api.config.refs.content
import org.rsmod.api.player.back
import org.rsmod.api.player.front
import org.rsmod.api.player.vars.intVarp
import org.rsmod.content.skills.runecraft.configs.RunecraftObjs
import org.rsmod.content.skills.runecraft.configs.RunecraftVarps
import org.rsmod.game.entity.Player
import org.rsmod.game.inv.isType
import org.rsmod.game.type.obj.ObjTypeList

/** Binding necklace charges spent; the necklace crumbles at `CombinationRunes.NECKLACE_CHARGES`. */
var Player.bindingNecklaceUsed: Int by intVarp(RunecraftVarps.binding_necklace_used)

object RunecraftGear {
    /**
     * A Runecraft cape or max cape: it opens every ruins, stands in for every talisman and tiara,
     * and stops pouches decaying.
     */
    fun wearsCape(player: Player, objTypes: ObjTypeList): Boolean {
        val cape = player.back ?: return false
        return cape.isType(RunecraftObjs.runecrafting_cape) ||
            cape.isType(RunecraftObjs.runecrafting_cape_trimmed) ||
            objTypes[cape].isContentType(content.max_cape)
    }

    fun wearsBindingNecklace(player: Player): Boolean =
        player.front?.isType(RunecraftObjs.binding_necklace) == true
}
