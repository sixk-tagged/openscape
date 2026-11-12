package org.rsmod.content.other.npcdrops

import jakarta.inject.Inject
import org.rsmod.api.death.NpcDropTables
import org.rsmod.api.death.NpcDrops
import org.rsmod.api.type.refs.npc.NpcReferences
import org.rsmod.plugin.scripts.PluginScript
import org.rsmod.plugin.scripts.ScriptContext

/**
 * Drop tables (OSRS wiki, Oct 2025) for the monsters around Seers' Village, Catherby and White Wolf
 * Mountain that no earlier area script covers. The Wilderness script gives the wolf and white wolf
 * types their table; the mountain's wolves have their own types.
 */
class KandarinNorthDrops @Inject constructor(private val tables: NpcDropTables) : PluginScript() {
    override fun ScriptContext.startup() {
        with(kandarin_north_drop_npcs) {
            mountain_wolves.forEach { tables[it] = mountainWolfDrops }
            tables[guard_dog] = bonesOnly
            tables[renegade_knight] = bonesOnly
            tables[rooster] = chickenDrops
            tables[ranging_guard] = guardDrops
        }
    }
}

private val mountainWolfDrops = NpcDrops(always = listOf(always(extra_objs.wolf_bones)))

object kandarin_north_drop_npcs : NpcReferences() {
    val mountain_wolves =
        listOf(find("whitewolf_sentry"), find("pack_wolf_whiter"), find("wolfpack_leader_whiter"))
    val guard_dog = find("guarddog")
    val renegade_knight = find("renegade_knight")
    val rooster = find("rooster")
    val ranging_guard = find("ranging_guild_guard")
}
