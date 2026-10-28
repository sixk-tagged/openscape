package org.rsmod.api.combat.commons.npc

import jakarta.inject.Singleton
import org.rsmod.api.combat.commons.types.MeleeAttackType
import org.rsmod.api.random.GameRandom
import org.rsmod.game.type.npc.NpcType
import org.rsmod.game.type.proj.ProjAnimType
import org.rsmod.game.type.seq.SeqType
import org.rsmod.game.type.spot.SpotanimType
import org.rsmod.game.type.stat.StatType
import org.rsmod.game.type.synth.SynthType

public enum class NpcAttackStyle {
    Melee,
    Ranged,
    Magic,
}

/** A stat the attack lowers by [percent] of the target's base level when it lands (Confuse...). */
public class NpcDrain(public val stat: StatType, public val percent: Int)

/**
 * One attack an npc can pick. [maxHit] is the wiki max hit; the hit chance still comes from the
 * npc's stats (`AccuracyFormulae`). A `null` [anim] or [sound] falls back to the npc's
 * `attack_anim` and `attack_sound` params. [launch] plays on the npc, [travel] is the projectile
 * (ranged and magic only, flown with [projAnim], default `arrow` or `magic_spell`) and [impact]
 * plays on the target when the projectile arrives. [drain] only applies to magic: a landing hit
 * does no damage. [melee] is the defence type a melee attack is rolled against (crush by default).
 * [weight] is the share of picks relative to the profile's other attacks.
 */
public class NpcAttack(
    public val style: NpcAttackStyle,
    public val maxHit: Int,
    public val anim: SeqType? = null,
    public val sound: SynthType? = null,
    public val launch: SpotanimType? = null,
    public val travel: SpotanimType? = null,
    public val impact: SpotanimType? = null,
    public val impactSound: SynthType? = null,
    public val projAnim: ProjAnimType? = null,
    public val drain: NpcDrain? = null,
    public val melee: MeleeAttackType? = null,
    public val weight: Int = 1,
) {
    init {
        require(style == NpcAttackStyle.Melee || travel != null) { "$style needs a projectile." }
        require(weight > 0) { "Weight must be positive." }
    }
}

/**
 * Everything an npc type attacks with, and from how far ([range], the `NpcType.attackRange` the
 * content module applies). An npc with a profile closes in with `apPlayer2` and attacks from
 * [range]; melee attacks are only picked when the target is adjacent. [speed] is the wiki attack
 * speed in ticks, for npcs whose `attackrate` param is missing (default 4).
 */
public class NpcAttackProfile(
    public val range: Int,
    public val attacks: List<NpcAttack>,
    public val speed: Int? = null,
) {
    init {
        require(attacks.isNotEmpty()) { "A profile needs at least one attack." }
    }

    public fun pick(random: GameRandom, adjacent: Boolean): NpcAttack {
        val options = attacks.filter { adjacent || it.style != NpcAttackStyle.Melee }
        val pool = options.ifEmpty { attacks }
        var roll = random.of(maxExclusive = pool.sumOf { it.weight })
        for (attack in pool) {
            if (roll < attack.weight) {
                return attack
            }
            roll -= attack.weight
        }
        return pool.last()
    }
}

/**
 * Npc type id -> attacks. Content registers a profile per ranged or magic npc at startup; npcs
 * without one attack in melee from their `attack_anim`. Adding a monster is one entry here.
 */
@Singleton
public class NpcAttackTable {
    private val profiles = hashMapOf<Int, NpcAttackProfile>()

    public operator fun get(type: NpcType): NpcAttackProfile? = profiles[type.id]

    public operator fun set(type: NpcType, profile: NpcAttackProfile) {
        profiles[type.id] = profile
    }
}
