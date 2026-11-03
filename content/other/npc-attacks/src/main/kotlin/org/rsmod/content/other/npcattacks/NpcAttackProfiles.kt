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
import org.rsmod.api.config.refs.varbits
import org.rsmod.api.config.refs.varps
import org.rsmod.api.player.lefthand
import org.rsmod.api.player.magic.freeze
import org.rsmod.api.player.output.mes
import org.rsmod.api.player.vars.intVarp
import org.rsmod.api.type.refs.npc.NpcReferences
import org.rsmod.api.type.refs.obj.ObjReferences
import org.rsmod.api.type.refs.seq.SeqReferences
import org.rsmod.api.type.refs.spot.SpotanimReferences
import org.rsmod.api.type.refs.timer.TimerReferences
import org.rsmod.game.entity.Player
import org.rsmod.game.inv.isAnyType
import org.rsmod.game.type.npc.NpcType

private var Player.teleblockTicks by intVarp(varps.teleblock_ticks)

/**
 * Every ranged or magic monster in Lumbridge, Draynor, Al Kharid, Varrock and the Wilderness
 * (including its dragons, whose dragonfire is in [Dragonfire]), from the OSRS wiki (max hits,
 * speeds). To add one, give it a line in [PROFILES]: its npc types, how far it shoots from and the
 * attacks it rolls. The attack range is applied to the npc type by [NpcAttackRangeEditor] and the
 * profile is registered by [NpcAttackProfileScript].
 */
object NpcAttackProfiles {
    // UNVERIFIED: the attack ranges (the wiki gives none), which animation and sound each npc uses
    // (the cache has none for them), how often a dark wizard curses instead of striking and the
    // wyvern's pick weights.
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
                        onLand = ::icyBreathFreeze,
                    ),
                ),
        )

    /**
     * The icy breath freezes like the Ancient ice spells unless the ancient wyvern shield is worn.
     * UNVERIFIED: how long (the wiki gives no length; Ice Rush's 8 ticks), and that the freeze
     * starts when the breath is launched rather than when it arrives.
     */
    internal fun icyBreathFreeze(target: Player) {
        val shield = target.lefthand
        if (!shield.isAnyType(NpcAttackObjs.wyvern_shield, NpcAttackObjs.wyvern_shield_uncharged)) {
            target.freeze(ICY_BREATH_FREEZE_TICKS)
        }
    }

    private const val ICY_BREATH_FREEZE_TICKS = 8

    /**
     * A slash and dragonfire, which the green, black and lava dragons only breathe from melee
     * range, one attack in six (Mod Ash, via the wiki). Baby dragons have no profile, as they
     * cannot breathe fire. UNVERIFIED: the slash and breath animations, the projectile and the
     * sound (the cache names a dragon's, nothing says it is theirs) and that the black and lava
     * dragons share them.
     */
    private fun dragon(slashMax: Int) =
        NpcAttackProfile(
            range = 1,
            attacks =
                listOf(
                    NpcAttack(
                        style = NpcAttackStyle.Melee,
                        maxHit = slashMax,
                        anim = NpcAttackSeqs.dragon_attack,
                        melee = MeleeAttackType.Slash,
                        weight = 5,
                    ),
                    NpcAttack(
                        style = NpcAttackStyle.Magic,
                        maxHit = Dragonfire.MAX_HIT,
                        anim = NpcAttackSeqs.dragon_firebreath_all_attack,
                        sound = synths.firebreath,
                        launch = NpcAttackSpotanims.firebreath_attack,
                        travel = NpcAttackSpotanims.firebreath_travel,
                        rollDamage = Dragonfire::roll,
                    ),
                ),
        )

    // The slashes' max hits are the wiki's.
    private val greenDragon = dragon(slashMax = 8)
    private val blackDragon = dragon(slashMax = 21)
    private val lavaDragon = dragon(slashMax = 23)

    /**
     * Fists, and a spell that is a mix of Confuse and Bind: no damage, and a brief bind if it
     * lands. UNVERIFIED: how brief (the wiki says only "very briefly"), how often it is cast and
     * that it stays in melee range.
     */
    private val chaosDruid =
        NpcAttackProfile(
            range = 1,
            attacks =
                listOf(
                    NpcAttack(
                        style = NpcAttackStyle.Melee,
                        maxHit = 2,
                        melee = MeleeAttackType.Crush,
                        weight = 3,
                    ),
                    NpcAttack(
                        style = NpcAttackStyle.Magic,
                        maxHit = 0,
                        anim = NpcAttackSeqs.human_castentangle,
                        sound = synths.bind_cast,
                        launch = NpcAttackSpotanims.entangle_casting,
                        travel = NpcAttackSpotanims.entangle_travel,
                        impact = NpcAttackSpotanims.bind_impact,
                        impactSound = synths.bind_impact,
                        onLand = { it.freeze(CHAOS_DRUID_BIND_TICKS) },
                    ),
                ),
        )

    private const val CHAOS_DRUID_BIND_TICKS = 3

    /**
     * Magic up to 17, and Tele Block. UNVERIFIED: the spell graphics (Fire Wave's), the distance
     * they cast from, and how often Tele Block is cast.
     */
    private val elderChaosDruid =
        NpcAttackProfile(
            range = 8,
            attacks =
                listOf(
                    NpcAttack(
                        style = NpcAttackStyle.Magic,
                        maxHit = 17,
                        anim = seqs.human_castwave,
                        sound = synths.firewave_cast_and_fire,
                        launch = spotanims.firewave_casting,
                        travel = spotanims.firewave_travel,
                        impact = spotanims.firewave_impact,
                        impactSound = synths.firewave_hit,
                        weight = 3,
                    ),
                    NpcAttack(
                        style = NpcAttackStyle.Magic,
                        maxHit = 0,
                        anim = NpcAttackSeqs.human_casting_tele_block,
                        sound = synths.teleportblock_cast,
                        travel = spotanims.ballista_special,
                        impact = NpcAttackSpotanims.tele_block_impact,
                        impactSound = synths.teleportblock_impact,
                        onLand = ::teleBlock,
                    ),
                ),
        )

    /**
     * Tele Block as the Elder Chaos druids cast it: the standard five minutes (half under Protect
     * from Magic), started over if the target is already blocked. The Tele Block spell's own clock
     * counts it down. UNVERIFIED: the message (the wiki words the player version only).
     */
    internal fun teleBlock(target: Player) {
        val prayer = target.vars[varbits.protect_from_magic] == 1
        target.teleblockTicks = if (prayer) TELE_BLOCK_HALF_TICKS else TELE_BLOCK_TICKS
        target.softTimer(NpcAttackTimers.teleblock, 1)
        val time = if (prayer) "2 minutes, 30 seconds" else "5 minutes"
        target.mes(
            "<col=800080>A Tele Block has been cast on you by the Elder Chaos druid. " +
                "It will expire in $time.</col>"
        )
    }

    private const val TELE_BLOCK_TICKS = 500
    private const val TELE_BLOCK_HALF_TICKS = 250

    /**
     * Level 23: fists and a spell. UNVERIFIED: the wiki gives one max hit (2) for both, so Wind
     * Strike's graphics are a guess, as are the distance and how often each is used.
     */
    private val monkOfZamorak =
        NpcAttackProfile(
            range = 8,
            attacks =
                listOf(
                    NpcAttack(
                        style = NpcAttackStyle.Melee,
                        maxHit = 2,
                        melee = MeleeAttackType.Crush,
                    ),
                    NpcAttack(
                        style = NpcAttackStyle.Magic,
                        maxHit = 2,
                        anim = seqs.human_caststrike,
                        sound = synths.windstrike_cast_and_fire,
                        launch = spotanims.windstrike_casting,
                        travel = spotanims.windstrike_travel,
                        impact = spotanims.windstrike_impact,
                        impactSound = synths.windstrike_hit,
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
            listOf(
                NpcAttackNpcs.green_dragon,
                NpcAttackNpcs.green_dragon2,
                NpcAttackNpcs.green_dragon3,
                NpcAttackNpcs.green_dragon4,
                NpcAttackNpcs.green_dragon5,
            ) to greenDragon,
            listOf(NpcAttackNpcs.black_dragon4, NpcAttackNpcs.black_dragon5) to blackDragon,
            listOf(NpcAttackNpcs.lava_dragon) to lavaDragon,
            listOf(NpcAttackNpcs.chaos_druid) to chaosDruid,
            listOf(NpcAttackNpcs.elder_chaos_druid) to elderChaosDruid,
            listOf(NpcAttackNpcs.monk_of_zamorak) to monkOfZamorak,
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
    val green_dragon = find("green_dragon")
    val green_dragon2 = find("green_dragon2")
    val green_dragon3 = find("green_dragon3")
    val green_dragon4 = find("green_dragon4")
    val green_dragon5 = find("green_dragon5")
    val black_dragon4 = find("black_dragon4")
    val black_dragon5 = find("black_dragon5")
    val lava_dragon = find("lava_dragon")
    val chaos_druid = find("chaos_druid")
    val elder_chaos_druid = find("wilderness_chaos_druid")
    val monk_of_zamorak = find("chaos_temple_monk_wilderness")
}

/** The shields that protect against dragonfire (or the wyvern's icy breath), charged or not. */
object NpcAttackObjs : ObjReferences() {
    val anti_dragon_shield = find("antidragonbreathshield")
    val dragonfire_shield = find("dragonfire_shield")
    val dragonfire_shield_uncharged = find("dragonfire_shield_uncharged")
    val dragonfire_ward = find("dragonfire_ward")
    val dragonfire_ward_uncharged = find("dragonfire_ward_uncharged")
    val wyvern_shield = find("wyvern_shield")
    val wyvern_shield_uncharged = find("wyvern_shield_uncharged")
}

internal object NpcAttackTimers : TimerReferences() {
    /** Counts the Tele Block down; the Tele Block spell's module runs it. */
    val teleblock = find("wild_teleblock")
}

internal object NpcAttackSeqs : SeqReferences() {
    val dragon_attack = find("dragon_attack")
    val dragon_firebreath_all_attack = find("dragon_firebreath_all_attack")
    val human_castconfuse = find("human_castconfuse")
    val human_castentangle = find("human_castentangle")
    val human_casting_tele_block = find("human_casting_tele_block")
    val human_castweaken = find("human_castweaken")
    val wyvern_skeleton_bite = find("wyvern_skeleton_bite")
    val wyvern_skeleton_tail_swipe = find("wyvern_skeleton_tail_swipe")
    val wyvern_skeleton_ice_breath = find("wyvern_skeleton_ice_breath")
    val wyvern_skeleton_iceball = find("wyvern_skeleton_iceball")
}

internal object NpcAttackSpotanims : SpotanimReferences() {
    val bind_impact = find("bind_impact")
    val entangle_casting = find("entangle_casting")
    val entangle_travel = find("entangle_travel")
    val firebreath_attack = find("firebreath_attack")
    val firebreath_travel = find("firebreath_travel")
    val tele_block_impact = find("tele_block_impact")
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
