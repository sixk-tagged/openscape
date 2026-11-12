package org.rsmod.content.skills.slayer

import jakarta.inject.Inject
import org.rsmod.api.combat.commons.npc.NpcAttack
import org.rsmod.api.combat.commons.npc.NpcAttackProfile
import org.rsmod.api.combat.commons.npc.NpcAttackStyle
import org.rsmod.api.combat.commons.npc.NpcAttackTable
import org.rsmod.api.config.refs.params
import org.rsmod.api.config.refs.stats
import org.rsmod.api.config.refs.varbits
import org.rsmod.api.player.hit.processor.InstantPlayerHitProcessor
import org.rsmod.api.player.hit.takeInstantHit
import org.rsmod.api.player.stat.statSub
import org.rsmod.api.random.GameRandom
import org.rsmod.api.type.editors.npc.NpcEditor
import org.rsmod.api.type.editors.obj.ObjEditor
import org.rsmod.api.type.refs.npc.NpcReferences
import org.rsmod.api.type.refs.obj.ObjReferences
import org.rsmod.api.type.refs.seq.SeqReferences
import org.rsmod.api.type.refs.spot.SpotanimReferences
import org.rsmod.game.entity.Player
import org.rsmod.game.hit.HitType
import org.rsmod.game.type.obj.ObjType
import org.rsmod.game.type.seq.SeqType
import org.rsmod.plugin.scripts.PluginScript
import org.rsmod.plugin.scripts.ScriptContext

/**
 * The Slayer monsters whose attacks need protective gear (wiki "Banshee", "Aberrant spectre",
 * "Cockatrice", "Basilisk"): banshees scream through anything but earmuffs (6 more damage and
 * drained stats), aberrant spectres hit through prayer and drain combat stats unless a nose peg is
 * worn, and the cockatrice's and basilisk's gaze drains combat stats unless a mirror shield (or V's
 * shield) is held. A Slayer helmet counts as earmuffs and a nose peg, not as a mirror shield.
 * Turoth and kurask are marked `leafbladed_only` (the hit processor zeroes other damage).
 */
class SlayerProtection
@Inject
constructor(private val table: NpcAttackTable, private val instantHits: InstantPlayerHitProcessor) :
    PluginScript() {
    // UNVERIFIED: how much the scream and the stench drain (the wiki says "greatly" and "extremely
    // quickly"), the spectres' attack range and their projectile.
    private val bansheeProfile =
        NpcAttackProfile(
            range = 1,
            attacks =
                listOf(
                    NpcAttack(
                        style = NpcAttackStyle.Melee,
                        maxHit = 2,
                        anim = ProtectionSeqs.banshee_attack,
                        onAttack = { player -> scream(player) },
                    )
                ),
        )

    private val spectreProfile =
        NpcAttackProfile(
            range = SPECTRE_RANGE,
            attacks =
                listOf(
                    NpcAttack(
                        style = NpcAttackStyle.Magic,
                        maxHit = SPECTRE_MAX,
                        travel = ProtectionSpots.spectre_proj,
                        rollDamage = ::stench,
                    )
                ),
        )

    override fun ScriptContext.startup() {
        // UNVERIFIED: superiors use their lesser monster's attack (and its max hit).
        for (cockatrice in ProtectedNpcs.cockatrices) {
            table[cockatrice] = gazeProfile(ProtectionSeqs.cockatrice_attack)
        }
        for (basilisk in ProtectedNpcs.basilisks) {
            table[basilisk] = gazeProfile(ProtectionSeqs.basilisk_attack)
        }
        for (banshee in ProtectedNpcs.banshees) {
            table[banshee] = bansheeProfile
        }
        for (spectre in ProtectedNpcs.aberrant_spectres) {
            table[spectre] = spectreProfile
        }
    }

    private fun gazeProfile(attack: SeqType) =
        NpcAttackProfile(
            range = 1,
            attacks =
                listOf(
                    NpcAttack(
                        style = NpcAttackStyle.Melee,
                        maxHit = GAZE_MAX,
                        anim = attack,
                        onAttack = { player -> gaze(player) },
                    )
                ),
        )

    private fun gaze(player: Player) {
        if (ProtectionObjs.mirror_shields.any { it in player.worn }) {
            return
        }
        for (stat in STENCH_STATS) {
            player.statSub(stat, DRAIN, 0)
        }
    }

    private fun scream(player: Player) {
        if (player.wears(ProtectionObjs.earmuffs)) {
            return
        }
        player.takeInstantHit(HitType.Typeless, SCREAM_DAMAGE, instantHits)
        for (stat in SCREAM_STATS) {
            player.statSub(stat, DRAIN, 0)
        }
    }

    /** With a nose peg the normal magic attack; without, a hit through prayer up to 14. */
    private fun stench(player: Player, accurate: Boolean, random: GameRandom): Int {
        if (player.wears(ProtectionObjs.nose_peg)) {
            val prayed = player.vars[varbits.protect_from_magic] == 1
            return if (!accurate || prayed) 0 else random.of(0, SPECTRE_MAX)
        }
        for (stat in STENCH_STATS) {
            player.statSub(stat, DRAIN, 0)
        }
        return random.of(0, STENCH_MAX)
    }

    private fun Player.wears(gear: ObjType): Boolean =
        gear in worn || ProtectionObjs.slayer_helmets.any { it in worn }

    private companion object {
        const val SCREAM_DAMAGE = 6
        const val DRAIN = 2
        const val SPECTRE_MAX = 8
        const val STENCH_MAX = 14
        const val SPECTRE_RANGE = 7
        const val GAZE_MAX = 5

        val SCREAM_STATS =
            listOf(
                stats.attack,
                stats.strength,
                stats.defence,
                stats.ranged,
                stats.magic,
                stats.prayer,
                stats.agility,
            )
        val STENCH_STATS =
            listOf(stats.attack, stats.strength, stats.defence, stats.ranged, stats.magic)
    }
}

internal object ProtectedNpcs : NpcReferences() {
    val cockatrices = listOf(find("slayer_cockatrice"), find("superior_cockatrice"))
    val basilisks = listOf(find("slayer_basilisk"), find("superior_basilisk"))
    val leaf_bladed_only =
        listOf(
                "slayer_turoth",
                "slayer_turoth_dad",
                "slayer_turoth_mum",
                "slayer_turoth_child",
                "slayer_turoth_baby",
                "slayer_turoth_dad2",
                "slayer_turoth_dad3",
                "slayer_kursk_1",
                "slayer_kursk_2",
                "slayer_kurask_troll",
                "superior_turoth",
                "superior_kurask",
            )
            .map { find(it) }
    val banshees = listOf(find("slayer_banshee_1"), find("superior_banshee"))
    val aberrant_spectres =
        (1..4).map { find("slayer_abberant_spectre_$it") } +
            find("superior_abberant_spectre") +
            (1..2).map { find("slayer_aberrantspectre_${it}_strongholdcave") }
}

internal object ProtectionObjs : ObjReferences() {
    val earmuffs = find("slayer_earmuffs")
    val nose_peg = find("slayer_nosepeg")
    val mirror_shields = listOf(find("slayer_mirror_shield"), find("v_shield"))
    val slayer_helmets =
        listOf(
                "slayer_helm",
                "slayer_helm_i",
                "slayer_helm_black",
                "slayer_helm_i_black",
                "slayer_helm_green",
                "slayer_helm_i_green",
                "slayer_helm_red",
                "slayer_helm_i_red",
                "slayer_helm_purple",
                "slayer_helm_i_purple",
                "slayer_helm_turquoise",
                "slayer_helm_i_turquoise",
                "slayer_helm_hydra",
                "slayer_helm_i_hydra",
                "slayer_helm_twisted",
                "slayer_helm_i_twisted",
                "slayer_helm_jad",
                "slayer_helm_i_jad",
                "slayer_helm_verzik",
                "slayer_helm_i_verzik",
                "slayer_helm_zuk",
                "slayer_helm_i_zuk",
                "slayer_helm_araxyte",
                "slayer_helm_i_araxyte",
            )
            .map { find(it) }
}

internal object ProtectionSeqs : SeqReferences() {
    val banshee_attack = find("banshee_attack")
    val cockatrice_attack = find("cockatrice_attack")
    val basilisk_attack = find("basilisk_attack")
}

internal object ProtectionSpots : SpotanimReferences() {
    val spectre_proj = find("spectre_melee_proj")
}

/** The spectres attack from a distance (the cache gives them no range); turoth and kurask. */
internal object ProtectionNpcEditor : NpcEditor() {
    init {
        for (spectre in ProtectedNpcs.aberrant_spectres) {
            edit(spectre) { attackRange = 7 }
        }
        for (npc in ProtectedNpcs.leaf_bladed_only) {
            edit(npc) {
                param[params.leafbladed_only] = 1
                // The leaf-bladed battleaxe's +17.5% (CombatNpcAttributeCollector).
                param[params.leafy] = 1
            }
        }
    }
}

/** The combat formulas' Black mask / Slayer helmet bonus reads these obj params. */
internal object SlayerGearObjEditor : ObjEditor() {
    init {
        for (mask in GearObjs.masks) {
            edit(mask) { param[params.blackmask] = 1 }
        }
        for (mask in GearObjs.imbued_masks) {
            edit(mask) { param[params.blackmask_imbued] = 1 }
        }
        for ((index, helm) in ProtectionObjs.slayer_helmets.withIndex()) {
            // The list alternates plain and imbued (`slayer_helm`, `slayer_helm_i`, ...).
            if (index % 2 == 0) {
                edit(helm) { param[params.slayer_helm] = 1 }
            } else {
                edit(helm) { param[params.slayer_helm_imbued] = 1 }
            }
        }
    }
}
