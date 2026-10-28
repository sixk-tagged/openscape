package org.rsmod.content.other.poison

import jakarta.inject.Inject
import org.rsmod.api.combat.commons.poison.NpcPoisonTable
import org.rsmod.api.config.refs.params
import org.rsmod.api.type.editors.npc.NpcEditor
import org.rsmod.api.type.refs.npc.NpcReferences
import org.rsmod.plugin.scripts.PluginScript
import org.rsmod.plugin.scripts.ScriptContext

/**
 * Monsters whose landed attacks poison, with the damage the poison starts at (OSRS wiki, Poison).
 * None of the monsters spawned in Lumbridge, Draynor, Al Kharid or Varrock poison; these are the
 * poisonous ones around them, ready for when their areas arrive.
 */
class PoisonousMonsters @Inject constructor(private val table: NpcPoisonTable) : PluginScript() {
    override fun ScriptContext.startup() {
        with(PoisonousNpcs) {
            table[poison_scorpion] = 3
            table[poisonspider] = 6
            table[dungeonspider] = 3
            table[swamp_cave_slime] = 3
        }
    }
}

internal object PoisonousNpcs : NpcReferences() {
    val poison_scorpion = find("poison_scorpion")
    val poisonspider = find("poisonspider")
    val dungeonspider = find("dungeonspider")
    val swamp_cave_slime = find("swamp_cave_slime")
    val cows =
        listOf(
            find("cow"),
            find("cow2"),
            find("cow2_calf"),
            find("cow3"),
            find("cow3_calf"),
            find("cow_beef"),
        )
}

/** Cows cannot be poisoned (wiki). */
internal object CowPoisonImmunity : NpcEditor() {
    init {
        PoisonousNpcs.cows.forEach { edit(it) { param[params.poison_immunity] = 1 } }
    }
}
