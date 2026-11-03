package org.rsmod.content.other.npcdrops

import jakarta.inject.Inject
import org.rsmod.api.area.checker.AreaChecker
import org.rsmod.api.config.constants
import org.rsmod.api.config.refs.areas
import org.rsmod.api.death.NpcDropTables
import org.rsmod.api.death.NpcDrops
import org.rsmod.api.death.NpcKilled
import org.rsmod.api.random.GameRandom
import org.rsmod.api.repo.obj.ObjRepository
import org.rsmod.api.script.onEvent
import org.rsmod.plugin.scripts.PluginScript
import org.rsmod.plugin.scripts.ScriptContext

/**
 * Every monster killed inside the Wilderness can drop the looting bag, at 1 in `150 / combat level`
 * (between 1 in 3 and 1 in 15), whichever area wrote up its table. The monsters that only the
 * Wilderness has roll it in their own tables, at the rate the wiki prints, and are skipped here;
 * aviansie, abyssal demons, black chinchompas and the Wilderness' ents and golems never drop it.
 */
class WildernessBagRule
@Inject
constructor(
    private val tables: NpcDropTables,
    private val random: GameRandom,
    private val objRepo: ObjRepository,
    private val areaChecker: AreaChecker,
) : PluginScript() {
    override fun ScriptContext.startup() {
        onEvent<NpcKilled> { roll() }
    }

    private fun NpcKilled.roll() {
        val type = npc.type
        if (
            NEVER.any { type.name.equals(it, ignoreCase = true) } ||
                type.id == wilderness_drop_npcs.ent.id ||
                type.id == wilderness_drop_npcs.rune_golem.id ||
                tables[type]?.rollsBag() == true ||
                !areaChecker.inArea(areas.wilderness, npc.coords)
        ) {
            return
        }
        val chance = (150 / maxOf(type.vislevel, 1)).coerceIn(3, 15)
        if (random.of(chance) == 0) {
            val duration = hero.lootDropDuration ?: constants.lootdrop_duration
            objRepo.add(wilderness_drop_objs.looting_bag, npc.coords, duration, hero)
        }
    }

    /** Whether one of the table's always-rolled sub-tables is the looting bag. */
    private fun NpcDrops.rollsBag(): Boolean =
        always.any { drop ->
            drop.table?.drops?.any { it.obj?.id == wilderness_drop_objs.looting_bag.id } == true
        }

    private companion object {
        val NEVER = listOf("Aviansie", "Abyssal demon", "Black chinchompa")
    }
}
