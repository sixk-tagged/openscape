package org.rsmod.content.other.npcattacks

import org.rsmod.api.combat.commons.npc.NpcAttack
import org.rsmod.api.combat.commons.npc.NpcAttackProfile
import org.rsmod.api.combat.commons.npc.NpcAttackStyle
import org.rsmod.api.combat.commons.npc.NpcDrain
import org.rsmod.api.combat.commons.types.MeleeAttackType
import org.rsmod.api.config.refs.projanims
import org.rsmod.api.config.refs.seqs
import org.rsmod.api.config.refs.spotanims
import org.rsmod.api.config.refs.stats
import org.rsmod.api.config.refs.synths
import org.rsmod.api.type.refs.npc.NpcReferences
import org.rsmod.api.type.refs.seq.SeqReferences
import org.rsmod.api.type.refs.spot.SpotanimReferences
import org.rsmod.game.type.npc.NpcType

/**
 * Every ranged or magic monster in Lumbridge, Draynor, Al Kharid and Varrock, from the OSRS wiki
 * (max hits, speeds). To add one, give it a line in [PROFILES]: its npc types, how far it shoots
 * from and the attacks it rolls. The attack range is applied to the npc type by
 * [NpcAttackRangeEditor] and the profile is registered by [NpcAttackProfileScript].
 */
object NpcAttackProfiles {
    // UNVERIFIED: the attack ranges (the wiki gives none), which animation and sound each npc uses
    // (the cache has none for them), how often a dark wizard curses instead of striking, the
    // wyvern's pick weights, and the wyvern's icy breath freezing (left out: no freeze system).
    private val wizard =
        NpcAttackProfile(
            range = 8,
            attacks =
                listOf(
                    NpcAttack(
                        style = NpcAttackStyle.Magic,
                        maxHit = 4,
                        anim = seqs.human_caststrike,
                        sound = synths.firestrike_cast_and_fire,
                        launch = spotanims.firestrike_casting,
                        travel = spotanims.firestrike_travel,
                        impact = spotanims.firestrike_impact,
                        impactSound = synths.firestrike_hit,
                    )
                ),
        )

    /** Level 7: Water Strike, and Confuse (Attack -5%) one cast in four. */
    private val darkWizardLow =
        NpcAttackProfile(
            range = 8,
            attacks =
                listOf(
                    NpcAttack(
                        style = NpcAttackStyle.Magic,
                        maxHit = 2,
                        anim = seqs.human_caststrike,
                        sound = synths.waterstrike_cast_and_fire,
                        launch = spotanims.waterstrike_casting,
                        travel = spotanims.waterstrike_travel,
                        impact = spotanims.waterstrike_impact,
                        impactSound = synths.waterstrike_hit,
                        weight = 3,
                    ),
                    NpcAttack(
                        style = NpcAttackStyle.Magic,
                        maxHit = 0,
                        anim = NpcAttackSeqs.human_castconfuse,
                        sound = synths.confuse_cast_and_fire,
                        launch = NpcAttackSpotanims.confuse_casting,
                        travel = NpcAttackSpotanims.confuse_travel,
                        impact = NpcAttackSpotanims.confuse_impact,
                        impactSound = synths.confuse_hit,
                        drain = NpcDrain(stats.attack, percent = 5),
                    ),
                ),
        )

    /** Level 20: Earth Strike, and Weaken (Strength -5%) one cast in four. */
    private val darkWizardHigh =
        NpcAttackProfile(
            range = 8,
            attacks =
                listOf(
                    NpcAttack(
                        style = NpcAttackStyle.Magic,
                        maxHit = 6,
                        anim = seqs.human_caststrike,
                        sound = synths.earthstrike_cast_and_fire,
                        launch = spotanims.earthstrike_casting,
                        travel = spotanims.earthstrike_travel,
                        impact = spotanims.earthstrike_impact,
                        impactSound = synths.earthstrike_hit,
                        weight = 3,
                    ),
                    NpcAttack(
                        style = NpcAttackStyle.Magic,
                        maxHit = 0,
                        anim = NpcAttackSeqs.human_castweaken,
                        sound = synths.weaken_all,
                        launch = NpcAttackSpotanims.weaken_casting,
                        travel = NpcAttackSpotanims.weaken_travel,
                        impact = NpcAttackSpotanims.weaken_impact,
                        drain = NpcDrain(stats.strength, percent = 5),
                    ),
                ),
        )

    /** Bites and tail-lashes up close, throws iceballs and breathes ice from range. */
    private val skeletalWyvern =
        NpcAttackProfile(
            range = 7,
            speed = 6,
            attacks =
                listOf(
                    NpcAttack(
                        style = NpcAttackStyle.Melee,
                        maxHit = 13,
                        anim = NpcAttackSeqs.wyvern_skeleton_bite,
                        melee = MeleeAttackType.Slash,
                    ),
                    NpcAttack(
                        style = NpcAttackStyle.Melee,
                        maxHit = 13,
                        anim = NpcAttackSeqs.wyvern_skeleton_tail_swipe,
                        melee = MeleeAttackType.Slash,
                    ),
                    NpcAttack(
                        style = NpcAttackStyle.Ranged,
                        maxHit = 13,
                        anim = NpcAttackSeqs.wyvern_skeleton_iceball,
                        launch = NpcAttackSpotanims.wyvern_skeleton_launch_iceball,
                        travel = NpcAttackSpotanims.wyvern_skeleton_travel_iceball,
                        projAnim = projanims.magic_spell,
                        weight = 2,
                    ),
                    NpcAttack(
                        style = NpcAttackStyle.Magic,
                        maxHit = 50,
                        anim = NpcAttackSeqs.wyvern_skeleton_ice_breath,
                        travel = NpcAttackSpotanims.wyvern_skeleton_travel_breath,
                        impact = NpcAttackSpotanims.wyvern_skeleton_impact_breath,
                    ),
                ),
        )

    val PROFILES: List<Pair<List<NpcType>, NpcAttackProfile>> =
        listOf(
            listOf(NpcAttackNpcs.wizard) to wizard,
            listOf(
                NpcAttackNpcs.young_dark_wizard,
                NpcAttackNpcs.qip_ds_young_dark_wizard1,
                NpcAttackNpcs.qip_ds_young_dark_wizard2,
            ) to darkWizardLow,
            listOf(
                NpcAttackNpcs.bearded_dark_wizard,
                NpcAttackNpcs.qip_ds_young_dark_wizard3,
                NpcAttackNpcs.qip_ds_young_dark_wizard4,
            ) to darkWizardHigh,
            listOf(
                NpcAttackNpcs.skeletal_wyvern1,
                NpcAttackNpcs.skeletal_wyvern2,
                NpcAttackNpcs.skeletal_wyvern3,
                NpcAttackNpcs.skeletal_wyvern4,
            ) to skeletalWyvern,
        )
}

object NpcAttackNpcs : NpcReferences() {
    val wizard = find("wizard")
    val young_dark_wizard = find("young_dark_wizard")
    val bearded_dark_wizard = find("bearded_dark_wizard")
    val qip_ds_young_dark_wizard1 = find("qip_ds_young_dark_wizard1")
    val qip_ds_young_dark_wizard2 = find("qip_ds_young_dark_wizard2")
    val qip_ds_young_dark_wizard3 = find("qip_ds_young_dark_wizard3")
    val qip_ds_young_dark_wizard4 = find("qip_ds_young_dark_wizard4")
    val skeletal_wyvern1 = find("skeletal_wyvern1")
    val skeletal_wyvern2 = find("skeletal_wyvern2")
    val skeletal_wyvern3 = find("skeletal_wyvern3")
    val skeletal_wyvern4 = find("skeletal_wyvern4")
}

internal object NpcAttackSeqs : SeqReferences() {
    val human_castconfuse = find("human_castconfuse")
    val human_castweaken = find("human_castweaken")
    val wyvern_skeleton_bite = find("wyvern_skeleton_bite")
    val wyvern_skeleton_tail_swipe = find("wyvern_skeleton_tail_swipe")
    val wyvern_skeleton_ice_breath = find("wyvern_skeleton_ice_breath")
    val wyvern_skeleton_iceball = find("wyvern_skeleton_iceball")
}

internal object NpcAttackSpotanims : SpotanimReferences() {
    val confuse_casting = find("confuse_casting")
    val confuse_travel = find("confuse_travel")
    val confuse_impact = find("confuse_impact")
    val weaken_casting = find("weaken_casting")
    val weaken_travel = find("weaken_travel")
    val weaken_impact = find("weaken_impact")
    val wyvern_skeleton_launch_iceball = find("wyvern_skeleton_launch_iceball")
    val wyvern_skeleton_travel_iceball = find("wyvern_skeleton_travel_iceball")
    val wyvern_skeleton_travel_breath = find("wyvern_skeleton_travel_breath")
    val wyvern_skeleton_impact_breath = find("wyvern_skeleton_impact_breath")
}
