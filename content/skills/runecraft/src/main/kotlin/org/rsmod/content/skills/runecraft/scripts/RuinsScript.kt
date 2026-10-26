package org.rsmod.content.skills.runecraft.scripts

import jakarta.inject.Inject
import org.rsmod.api.player.hat
import org.rsmod.api.player.protect.ProtectedAccess
import org.rsmod.api.player.vars.VarPlayerIntMapSetter
import org.rsmod.api.script.advanced.onWearposChange
import org.rsmod.api.script.onOpLoc1
import org.rsmod.api.script.onOpLocU
import org.rsmod.api.script.onPlayerLogin
import org.rsmod.content.skills.runecraft.configs.Altar
import org.rsmod.content.skills.runecraft.configs.Altars
import org.rsmod.content.skills.runecraft.configs.RunecraftObjs
import org.rsmod.game.entity.Player
import org.rsmod.game.inv.isType
import org.rsmod.game.type.obj.ObjTypeList
import org.rsmod.game.type.obj.Wearpos
import org.rsmod.plugin.scripts.PluginScript
import org.rsmod.plugin.scripts.ScriptContext

/**
 * Mysterious ruins and the altar rooms' exit portals. The ruins is a multiloc whose `Enter` shows
 * while the player wears the altar's tiara (or an elemental/catalytic one, or a Runecraft cape);
 * without one, the player uses a talisman on the ruins.
 */
class RuinsScript @Inject constructor(private val objTypes: ObjTypeList) : PluginScript() {
    override fun ScriptContext.startup() {
        onPlayerLogin { player.syncRuinsBits() }
        onWearposChange {
            if (wearpos == Wearpos.Hat || wearpos == Wearpos.Back) {
                player.syncRuinsBits()
            }
        }
        for (altar in Altars.withRuins) {
            val ruins = checkNotNull(altar.ruins)
            onOpLoc1(ruins.loc) { enter(altar) }
            onOpLoc1(ruins.portal) { leave(altar) }
            for (talisman in talismansFor(altar)) {
                onOpLocU(ruins.loc, talisman) { enter(altar) }
            }
        }
    }

    private fun talismansFor(altar: Altar) =
        listOf(
            checkNotNull(altar.binding).talisman,
            if (altar.binding?.elemental == true) {
                RunecraftObjs.elemental_talisman
            } else {
                RunecraftObjs.catalytic_talisman
            },
        )

    private fun ProtectedAccess.enter(altar: Altar) {
        mes("You feel a powerful force take hold of you...")
        teleport(checkNotNull(altar.ruins).inside)
    }

    private fun ProtectedAccess.leave(altar: Altar) {
        teleport(checkNotNull(altar.ruins).outside)
    }

    /** Sets each ruins' `Enter` bit from the worn hat and back slots, as the official game does. */
    fun Player.syncRuinsBits() {
        val capeWorn = RunecraftGear.wearsCape(this, objTypes)
        val hat = hat
        for (altar in Altars.withRuins) {
            val ruins = checkNotNull(altar.ruins)
            val binding = checkNotNull(altar.binding)
            val tiaraWorn =
                hat != null &&
                    (hat.isType(binding.tiara) ||
                        (binding.elemental && hat.isType(RunecraftObjs.tiara_elemental)) ||
                        (!binding.elemental && hat.isType(RunecraftObjs.tiara_catalytic)))
            VarPlayerIntMapSetter.set(this, ruins.bit, if (capeWorn || tiaraWorn) 1 else 0)
        }
    }
}
