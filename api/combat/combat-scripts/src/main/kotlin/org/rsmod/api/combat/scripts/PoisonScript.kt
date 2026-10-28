package org.rsmod.api.combat.scripts

import jakarta.inject.Inject
import kotlin.math.max
import kotlin.math.min
import org.rsmod.api.combat.commons.poison.Poison
import org.rsmod.api.combat.commons.poison.poisonSeverity
import org.rsmod.api.combat.commons.poison.poisonState
import org.rsmod.api.combat.commons.poison.poisonedBy
import org.rsmod.api.combat.commons.poison.resumePoison
import org.rsmod.api.combat.npc.lastAttack
import org.rsmod.api.combat.npc.lastCombat
import org.rsmod.api.config.refs.hitmark_groups
import org.rsmod.api.config.refs.timers
import org.rsmod.api.npc.access.StandardNpcAccess
import org.rsmod.api.npc.hit.modifier.NpcHitModifier
import org.rsmod.api.npc.hit.queueHit
import org.rsmod.api.npc.isInCombat
import org.rsmod.api.player.protect.ProtectedAccess
import org.rsmod.api.player.stat.hitpoints
import org.rsmod.api.script.onNpcTimer
import org.rsmod.api.script.onPlayerLogin
import org.rsmod.api.script.onPlayerTimer
import org.rsmod.game.entity.PlayerList
import org.rsmod.game.hit.HitType
import org.rsmod.plugin.scripts.PluginScript
import org.rsmod.plugin.scripts.ScriptContext

/**
 * The poison clock: every [Poison.INTERVAL] ticks a poisoned player or npc takes [Poison.damage]
 * and loses a point of severity, and an immune player loses a point of immunity. The player timer
 * is a normal one, so nothing happens while an interface is open and the hit lands as soon as it
 * closes.
 */
public class PoisonScript @Inject constructor(private val players: PlayerList) : PluginScript() {
    private val unmodified = NpcHitModifier {}

    override fun ScriptContext.startup() {
        onPlayerLogin { player.resumePoison() }
        onPlayerTimer(timers.toxins) { playerTick() }
        onNpcTimer(timers.toxins) { npcTick() }
    }

    private fun ProtectedAccess.playerTick() {
        val state = player.poisonState
        if (state > 0 && player.hitpoints > 0) {
            takeInstantHit(
                HitType.Typeless,
                Poison.damage(state),
                hitmark = hitmark_groups.poison_damage,
            )
            player.poisonState = state - 1
        } else if (state < 0) {
            player.poisonState = state + 1
        }
        if (player.poisonState == 0) {
            player.clearTimer(timers.toxins)
        }
    }

    private fun StandardNpcAccess.npcTick() {
        val severity = npc.poisonSeverity
        val lastFought = max(npc.lastCombat, npc.lastAttack)
        val outOfCombat = !npc.isInCombat() && mapClock - lastFought > Poison.NPC_OUT_OF_COMBAT
        if (severity <= 0 || npc.hitpoints == 0 || outOfCombat) {
            npc.poisonSeverity = 0
            npc.poisonedBy = null
            npc.clearTimer(timers.toxins)
            return
        }

        val damage = Poison.damage(severity)
        npc.queueHit(1, HitType.Typeless, damage, unmodified, hitmark_groups.poison_damage)
        npc.poisonedBy
            ?.let { it.resolve(players) }
            ?.let { heroPoints(it, min(damage, npc.hitpoints)) }

        npc.poisonSeverity = severity - 1
        if (severity == 1) {
            npc.poisonedBy = null
            npc.clearTimer(timers.toxins)
        }
    }
}
