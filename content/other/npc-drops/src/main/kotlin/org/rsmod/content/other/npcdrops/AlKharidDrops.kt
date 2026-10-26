package org.rsmod.content.other.npcdrops

import jakarta.inject.Inject
import org.rsmod.api.death.NpcDropTables
import org.rsmod.api.death.NpcDrops
import org.rsmod.api.type.refs.npc.NpcReferences
import org.rsmod.plugin.scripts.PluginScript
import org.rsmod.plugin.scripts.ScriptContext

/** Drop tables (OSRS wiki, Oct 2025) for the Al Kharid monsters. */
class AlKharidDrops @Inject constructor(private val tables: NpcDropTables) : PluginScript() {
    override fun ScriptContext.startup() {
        with(alkharid_npcs) {
            // The warriors and the town men all use the man table.
            listOf(al_kharid_warrior, al_kharid_man).forEach { tables[it] = manDrops }
            // The scorpion has no bones and no table.
            tables[scorpion] = NpcDrops()
            listOf(desert_wolf, desert_wolf2, desert_wolf3).forEach { tables[it] = wolfDrops }
            tables[ugthanki] = ugthankiDrops
            tables[shantay_guard] = bonesOnly
        }
    }
}

private val wolfDrops = NpcDrops(always = listOf(always(extra_objs.wolf_bones)))

private val ugthankiDrops = NpcDrops(always = listOf(bones, always(extra_objs.raw_ugthanki_meat)))

object alkharid_npcs : NpcReferences() {
    val al_kharid_warrior = find("al_kharid_warrior")
    val al_kharid_man = find("al_kharid_man")
    val scorpion = find("scorpion")
    val desert_wolf = find("desert_wolf")
    val desert_wolf2 = find("desert_wolf2")
    val desert_wolf3 = find("desert_wolf3")
    val ugthanki = find("ugthanki")
    val shantay_guard = find("shantay_guard")
}
