package org.rsmod.content.generic.npcs.banker

import jakarta.inject.Inject
import org.rsmod.api.config.refs.content
import org.rsmod.api.repo.loc.LocRepository
import org.rsmod.api.script.onEvent
import org.rsmod.game.entity.Npc
import org.rsmod.game.entity.npc.NpcStateEvents
import org.rsmod.game.map.Direction
import org.rsmod.game.type.loc.LocTypeList
import org.rsmod.plugin.scripts.PluginScript
import org.rsmod.plugin.scripts.ScriptContext

/**
 * Turns each banker to face the bank booth beside them. Spawn directions are set per npc type, and
 * the same banker types stand on different sides of their booths in different banks.
 */
class BankerFacing
@Inject
constructor(private val locRepo: LocRepository, private val locTypes: LocTypeList) :
    PluginScript() {
    override fun ScriptContext.startup() {
        onEvent<NpcStateEvents.Create> {
            if (npc.isContentType(content.banker)) {
                npc.faceBooth()
            }
        }
    }

    private fun Npc.faceBooth() {
        val side = sides.firstOrNull { hasBooth(it) } ?: return
        // Whichever runs first, this or the avatar's creation, the avatar ends up facing the booth.
        respawnDir = side
        infoProtocol.setSpawnDirection(side.id)
    }

    private fun Npc.hasBooth(side: Direction): Boolean =
        locRepo.findAll(coords.translate(side.xOff, side.zOff)).any { loc ->
            locTypes[loc].op.any { it.equals("Bank", ignoreCase = true) }
        }

    private companion object {
        val sides = listOf(Direction.North, Direction.East, Direction.South, Direction.West)
    }
}
