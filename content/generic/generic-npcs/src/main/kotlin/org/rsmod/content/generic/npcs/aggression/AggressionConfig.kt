package org.rsmod.content.generic.npcs.aggression

import org.rsmod.api.config.refs.huntmodes
import org.rsmod.api.type.editors.npc.NpcEditor
import org.rsmod.api.type.refs.npc.NpcReferences

/**
 * The monsters the OSRS wiki calls aggressive in the areas we have (the cache has no hunt modes, so
 * nothing attacks on its own otherwise). `cowardly` only goes for players up to twice the monster's
 * combat level, and every hunt mode turns tolerant after 10 minutes (`NpcPlayerHuntProcessor`).
 *
 * Left passive on purpose: the caged lesser demon in the Wizards' Tower, which can't reach anyone,
 * and the undead trees by Draynor Manor, which can't be attacked back.
 */
object AggressiveNpcs : NpcReferences() {
    val levelChecked =
        listOf(
            // Lumbridge Swamp, and Varrock's sewers and south-east mine.
            find("giantrat"),
            find("giantrat_grey"),
            find("giantrat_grey2"),
            find("giantrat_grey3"),
            find("giantrat1"),
            find("giantrat1_2"),
            find("giantrat1_3"),
            find("giantspider1"),
            find("giantspider2"),
            find("mugger"),
            // Draynor Village, Draynor Manor and the Draynor Sewers.
            find("young_dark_wizard"),
            find("highwayman"),
            // The jail's patrolling guards; the one at the cells (`jailguard`) is passive.
            find("jailguard_two_patrol"),
            find("jailguard_three_patrol"),
            find("jailguard_four_patrol"),
            find("house_ghost"),
            find("house_ghost2"),
            find("house_ghost3"),
            find("house_ghost4"),
            find("house_ghost5"),
            // Level 140: twice that is past any combat level, so they always attack.
            find("skeletal_wyvern1"),
            find("skeletal_wyvern2"),
            find("skeletal_wyvern3"),
            find("skeletal_wyvern4"),
            // Skeletons and zombies in the Draynor and Varrock sewers. The wiki has every
            // skeleton aggressive, `skeleton_unagressive` included.
            find("skeleton_unarmed"),
            find("skeleton_unarmed3"),
            find("skeleton_unarmed4"),
            find("skeleton_unagressive"),
            find("skeleton_unagressive2"),
            find("skeleton_unagressive3"),
            find("skeleton_armed"),
            find("skeleton_armed2"),
            find("skeleton_armed3"),
            find("skeleton_armed4"),
            find("skeleton_armed5"),
            find("zombie_unarmed_sewer1"),
            find("zombie_unarmed_sewer2"),
            find("zombie_unarmed_sewer3"),
            find("zombie_unarmed_sewer4"),
            find("zombie_armed_sewer1"),
            find("zombie_armed_sewer2"),
            find("zombie_armed_sewer3"),
            find("zombie_armed_sewer4"),
            find("zombie2"),
            find("zombie2_b"),
            find("zombie2_c"),
            // Al Kharid's mine and the desert past the Shantay Pass.
            find("scorpion"),
            find("desert_wolf"),
            find("desert_wolf2"),
            find("desert_wolf3"),
            find("ugthanki"),
            find("shantay_guard"),
            // Varrock: the stone circle, the palace bear cage and the sewers.
            find("qip_ds_young_dark_wizard1"),
            find("qip_ds_young_dark_wizard2"),
            find("qip_ds_young_dark_wizard3"),
            find("qip_ds_young_dark_wizard4"),
            find("brownbear"),
            find("ghost"),
            find("ghost5"),
            find("ghost8"),
            find("deadly_red_spider"),
            find("mossgiant"),
            find("mossgiant2"),
            find("mossgiant3"),
            find("mossgiant4"),
        )

    /** The wiki's "always aggressive", whatever the player's level. */
    val always = listOf(find("sourhog"))
}

internal object AggressiveNpcEditor : NpcEditor() {
    init {
        // Only melee has a default npc attack (`NvPCombatScript`), so the dark wizards close in
        // too.
        AggressiveNpcs.levelChecked.forEach { edit(it) { huntMode = huntmodes.cowardly } }
        AggressiveNpcs.always.forEach { edit(it) { huntMode = huntmodes.aggressive_melee } }
    }
}
