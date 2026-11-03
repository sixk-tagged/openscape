package org.rsmod.content.skills.magic.spell.utility

import jakarta.inject.Inject
import org.rsmod.api.config.refs.objs
import org.rsmod.api.config.refs.varbits
import org.rsmod.api.config.refs.varps
import org.rsmod.api.player.isTeleblocked
import org.rsmod.api.player.output.mes
import org.rsmod.api.player.protect.ProtectedAccess
import org.rsmod.api.player.vars.intVarp
import org.rsmod.api.script.onPlayerLogin
import org.rsmod.api.script.onPlayerLogout
import org.rsmod.api.script.onPlayerSoftTimer
import org.rsmod.api.spells.attack.SpellAttackManager
import org.rsmod.api.spells.attack.SpellAttackMap
import org.rsmod.api.spells.attack.SpellAttackRepository
import org.rsmod.api.type.refs.seq.SeqReferences
import org.rsmod.api.type.refs.spot.SpotanimReferences
import org.rsmod.api.type.refs.timer.TimerReferences
import org.rsmod.game.entity.PathingEntity
import org.rsmod.game.entity.Player
import org.rsmod.game.type.obj.ObjTypeList
import org.rsmod.plugin.scripts.PluginScript
import org.rsmod.plugin.scripts.ScriptContext

private var Player.teleblockTicks: Int by intVarp(varps.teleblock_ticks)

/**
 * Tele Block, on a player in the Wilderness: they cannot teleport for 5 minutes, or 2 minutes 30
 * seconds if Protect from Magic is up when the spell lands. The spell rolls the normal magic
 * accuracy, so a splash does nothing. The runes, the level and the xp come from the cache.
 *
 * Note: the short immunity after a Tele Block wears off, the Tele Block HUD timer and the block
 * ending when its caster dies are not built.
 */
class TeleBlockSpell @Inject constructor(private val objTypes: ObjTypeList) : SpellAttackMap {
    override fun SpellAttackRepository.register(manager: SpellAttackManager) {
        register(
            objs.spell_tele_block,
            TargetedSpell(
                manager,
                objTypes,
                FX,
                allowed = { target -> canBlock(target) },
                onHit = { target, _ -> block(target) },
            ),
        )
    }

    private fun ProtectedAccess.canBlock(target: PathingEntity): Boolean {
        // No Tele Block on npcs since Oct 2019.
        if (target !is Player) {
            return false
        }
        if (target.isTeleblocked()) {
            // UNVERIFIED: the wiki gives no wording; this is recalled from the live client.
            mes("This player is already effected by this spell.")
            return false
        }
        return true
    }

    private fun ProtectedAccess.block(target: PathingEntity) {
        if (target !is Player) {
            return
        }
        val ticks = if (target.vars[varbits.protect_from_magic] == 1) HALF_TICKS else FULL_TICKS
        target.teleblockTicks = ticks
        target.softTimer(UtilityTimers.teleblock, 1)
        // UNVERIFIED: the wiki gives the wording and says purple, but not the colour or how the
        // time is written.
        target.mes(
            "<col=800080>A Tele Block has been cast on you by ${player.displayName}. " +
                "It will expire in ${duration(ticks)}.</col>"
        )
    }

    private fun duration(ticks: Int): String {
        val seconds = ticks * 6 / 10
        val minutes = seconds / 60
        val rest = seconds % 60
        return if (rest == 0) "$minutes minutes" else "$minutes minutes, $rest seconds"
    }

    private companion object {
        const val FULL_TICKS = 500
        const val HALF_TICKS = 250

        val FX =
            SpellFx(
                staffAnim = TeleBlockSeqs.staff,
                unarmedAnim = TeleBlockSeqs.unarmed,
                // UNVERIFIED: 344 is the Tele Block projectile in the live client; the symbol
                // table names it after another use of the same graphic.
                travel = TeleBlockSpots.travel,
                impact = TeleBlockSpots.impact,
            )
    }
}

/**
 * The Tele Block clock: it runs only while logged in. A block ends on logout (since 2018); one
 * still saved at login, from a crash that skipped the logout, picks up where it left off.
 */
class TeleBlockScript : PluginScript() {
    override fun ScriptContext.startup() {
        onPlayerLogin { player.resumeBlock() }
        onPlayerLogout { player.endBlock() }
        onPlayerSoftTimer(UtilityTimers.teleblock) { player.tickBlock() }
    }

    private fun Player.resumeBlock() {
        if (teleblockTicks > 0) {
            softTimer(UtilityTimers.teleblock, 1)
        }
    }

    private fun Player.endBlock() {
        teleblockTicks = 0
        clearSoftTimer(UtilityTimers.teleblock)
    }

    private fun Player.tickBlock() {
        val left = teleblockTicks
        if (left <= 0) {
            clearSoftTimer(UtilityTimers.teleblock)
            return
        }
        teleblockTicks = left - 1
        if (left == 1) {
            clearSoftTimer(UtilityTimers.teleblock)
            // UNVERIFIED: the wiki says the message exists but not its wording.
            mes("Your Tele Block has expired.")
        }
    }
}

object UtilityTimers : TimerReferences() {
    val teleblock = find("wild_teleblock")
}

object TeleBlockSeqs : SeqReferences() {
    val unarmed = find("human_casting_tele_block")
    val staff = find("human_casting_tele_block_staff")
}

object TeleBlockSpots : SpotanimReferences() {
    val travel = find("ballista_special")
    val impact = find("tele_block_impact")
}
