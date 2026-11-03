package org.rsmod.content.other.npcattacks

import kotlin.math.max
import org.rsmod.api.config.refs.varbits
import org.rsmod.api.player.lefthand
import org.rsmod.api.player.output.mes
import org.rsmod.api.random.GameRandom
import org.rsmod.content.other.consumables.Antifire
import org.rsmod.content.other.consumables.antifireKind
import org.rsmod.game.entity.Player
import org.rsmod.game.inv.isType

/**
 * Regular dragonfire, as the green, black and lava dragons breathe it (max hit 50). It never
 * misses: what the target wears, prays and drinks decides how much gets through, from the wiki's
 * table. A super antifire potion stops all of it. Otherwise an anti-dragon shield (or one of its
 * upgrades) caps the roll at 5 and Protect from Magic at 10, the shield winning when both are on;
 * with neither, a magic accuracy roll caps it at 30 when won and 50 when lost. A plain antifire
 * potion then takes 15 off whatever was rolled, so with a shield or the prayer it stops all of it
 * too.
 *
 * The wiki asks for Dragon Slayer I to have been started before the shield and potions work; quest
 * requirements count as met here.
 */
object Dragonfire {
    const val MAX_HIT = 50
    private const val RESISTED_MAX = 30
    private const val PRAYER_MAX = 10
    private const val SHIELD_MAX = 5
    private const val ANTIFIRE_REDUCTION = 15

    private val SHIELDS =
        listOf(
            NpcAttackObjs.anti_dragon_shield,
            NpcAttackObjs.dragonfire_shield,
            NpcAttackObjs.dragonfire_shield_uncharged,
            NpcAttackObjs.dragonfire_ward,
            NpcAttackObjs.dragonfire_ward_uncharged,
            NpcAttackObjs.wyvern_shield,
            NpcAttackObjs.wyvern_shield_uncharged,
        )

    /** The damage [target] takes from one breath, and the message they get. */
    fun roll(target: Player, accurate: Boolean, random: GameRandom): Int {
        val kind = target.antifireKind
        val superPotion = kind == Antifire.SUPER || kind == Antifire.EXTENDED_SUPER
        val potion = kind == Antifire.NORMAL || kind == Antifire.EXTENDED
        val shield = target.lefthand
        val cap =
            when {
                superPotion -> 0
                SHIELDS.any { shield.isType(it) } -> SHIELD_MAX
                target.vars[varbits.protect_from_magic] == 1 -> PRAYER_MAX
                accurate -> RESISTED_MAX
                else -> MAX_HIT
            }
        // UNVERIFIED: the wiki gives only the start of the super antifire message ("Your super
        // antifire potion helps..."), the rest is a guess. The other two are the wiki's.
        target.mes(
            when {
                superPotion -> "Your super antifire potion helps you resist the dragon fire!"
                accurate -> "You manage to resist some of the dragon fire!"
                else -> "You're horribly burnt by the dragon fire!"
            }
        )
        if (cap == 0) {
            return 0
        }
        return max(0, random.of(0..cap) - if (potion) ANTIFIRE_REDUCTION else 0)
    }
}
