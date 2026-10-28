package org.rsmod.api.combat.commons.poison

import jakarta.inject.Inject
import jakarta.inject.Singleton
import org.rsmod.api.random.GameRandom
import org.rsmod.game.entity.Npc
import org.rsmod.game.entity.Player
import org.rsmod.game.type.npc.NpcType
import org.rsmod.game.type.obj.ObjTypeList
import org.rsmod.game.type.obj.UnpackedObjType

/**
 * The poison a weapon or ammo obj carries, found by name: daggers, spears, hastae, arrows, bolts,
 * darts, throwing knives and javelins ending in `_p` (severity 20), `_p+` (25) or `_p++` (30).
 */
@Singleton
public class PoisonedWeapons @Inject constructor(private val objTypes: ObjTypeList) {
    private val severities: Map<Int, Int> by lazy {
        objTypes.values.mapNotNull { type -> type.severity()?.let { type.id to it } }.toMap()
    }

    /** The melee severity of [type], or 0 if it is not poisoned. */
    public fun meleeSeverity(type: UnpackedObjType?): Int = type?.let { severities[it.id] } ?: 0

    /** The severity of [type] fired or thrown, or 0 if it is not poisoned. */
    public fun rangedSeverity(type: UnpackedObjType?): Int {
        val severity = meleeSeverity(type)
        return if (severity == 0) 0 else severity - Poison.RANGED_REDUCTION
    }

    /**
     * Rolls a poisoned [weapon] (the melee weapon, or the ammo or thrown weapon when [ranged])
     * poisoning [npc]: 1 in 4 melee hits and 1 in 8 ranged ones, as long as the hit did [damage].
     * Note: poison starts when the hit is queued, not when it arrives; a clock is 30 ticks.
     */
    public fun poisonNpc(
        npc: Npc,
        source: Player,
        weapon: UnpackedObjType?,
        ranged: Boolean,
        damage: Int,
        random: GameRandom,
    ): Boolean {
        val severity = if (ranged) rangedSeverity(weapon) else meleeSeverity(weapon)
        if (severity <= 0 || damage <= 0) {
            return false
        }
        val oneIn = if (ranged) RANGED_CHANCE else MELEE_CHANCE
        return random.of(maxExclusive = oneIn) == 0 && npc.poison(severity, source)
    }

    private fun UnpackedObjType.severity(): Int? {
        val name = internalName ?: return null
        val match = NAME.matchEntire(name) ?: return null
        return when (match.groupValues[1]) {
            "++" -> 30
            "+" -> 25
            else -> 20
        }
    }

    private companion object {
        const val MELEE_CHANCE = 4
        const val RANGED_CHANCE = 8

        val NAME =
            Regex(
                "(?!cert_|placeholder_|dummy_)[a-z0-9_]*" +
                    "(?:dagger|spear|hasta|arrow|bolts?|dart|javelin|knife)_p([+]{0,2})"
            )
    }
}

/** Npc type id -> the damage its poison starts at, for monsters that poison on a landed attack. */
@Singleton
public class NpcPoisonTable {
    private val startDamage = hashMapOf<Int, Int>()

    public operator fun get(type: NpcType): Int? = startDamage[type.id]

    public operator fun set(type: NpcType, damage: Int) {
        startDamage[type.id] = damage
    }
}
