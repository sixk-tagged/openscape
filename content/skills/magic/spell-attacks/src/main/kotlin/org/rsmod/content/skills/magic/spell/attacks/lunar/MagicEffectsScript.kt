package org.rsmod.content.skills.magic.spell.attacks.lunar

import jakarta.inject.Inject
import kotlin.math.max
import org.rsmod.api.combat.manager.MagicRuneManager
import org.rsmod.api.combat.manager.MagicRuneManager.Companion.isFailure
import org.rsmod.api.config.refs.objs
import org.rsmod.api.config.refs.stats
import org.rsmod.api.config.refs.walktriggers
import org.rsmod.api.player.hit.PlayerDamaged
import org.rsmod.api.player.hit.processor.InstantPlayerHitProcessor
import org.rsmod.api.player.hit.takeInstantHit
import org.rsmod.api.player.magic.hasVengeance
import org.rsmod.api.player.magic.isFrozen
import org.rsmod.api.player.magic.isVengeanceOnCooldown
import org.rsmod.api.player.magic.setVengeance
import org.rsmod.api.player.magic.startVengeanceCooldown
import org.rsmod.api.player.output.clearMapFlag
import org.rsmod.api.player.output.mes
import org.rsmod.api.player.protect.ProtectedAccessLauncher
import org.rsmod.api.player.stat.hitpoints
import org.rsmod.api.player.stat.statAdvance
import org.rsmod.api.script.onEvent
import org.rsmod.api.script.onIfOverlayButton
import org.rsmod.api.script.onPlayerWalkTrigger
import org.rsmod.api.spells.MagicSpellRegistry
import org.rsmod.content.skills.magic.spell.attacks.configs.AncientSeqs
import org.rsmod.content.skills.magic.spell.attacks.configs.AncientSpotanims
import org.rsmod.content.skills.magic.spell.attacks.configs.LunarComponents
import org.rsmod.game.entity.Player
import org.rsmod.game.entity.PlayerList
import org.rsmod.game.hit.HitType
import org.rsmod.plugin.scripts.PluginScript
import org.rsmod.plugin.scripts.ScriptContext

/**
 * openscape: the movement lock of freezes (the `frozen` walk trigger) and Lunar Vengeance.
 * Vengeance reflects 75% of the next damaging player hit; the reflect uses `takeInstantHit`, whose
 * default processor does not publish [PlayerDamaged], so two Vengeances cannot loop.
 */
class MagicEffectsScript
@Inject
constructor(
    private val spells: MagicSpellRegistry,
    private val runes: MagicRuneManager,
    private val launcher: ProtectedAccessLauncher,
    private val players: PlayerList,
    private val instantHits: InstantPlayerHitProcessor,
) : PluginScript() {
    override fun ScriptContext.startup() {
        onPlayerWalkTrigger(walktriggers.frozen) { player.blockWalk() }
        onIfOverlayButton(LunarComponents.vengeance) { castVengeance(player) }
        onEvent<PlayerDamaged> { reflect(target, hit, damage) }
    }

    private fun Player.blockWalk() {
        if (!isFrozen()) {
            return
        }
        routeDestination.clear()
        clearMapFlag()
        walkTrigger(walktriggers.frozen)
        mes("A magical force stops you from moving.")
    }

    private fun castVengeance(player: Player) {
        launcher.launch(player) {
            val spell = spells.getObjSpell(objs.spell_vengeance) ?: return@launch
            if (player.hasVengeance()) {
                mes("You already have Vengeance cast.")
                return@launch
            }
            if (player.isVengeanceOnCooldown()) {
                mes("You can only cast vengeance spells every 30 seconds.")
                return@launch
            }
            if (runes.attemptCast(player, spell).isFailure()) {
                return@launch
            }
            player.anim(AncientSeqs.vengeance_cast)
            player.spotanim(AncientSpotanims.vengeance_cast, height = 92)
            player.statAdvance(stats.magic, spell.castXp)
            player.setVengeance(true)
            player.startVengeanceCooldown()
        }
    }

    private fun reflect(target: Player, hit: org.rsmod.game.hit.Hit, damage: Int) {
        if (damage <= 0 || !hit.isFromPlayer || !target.hasVengeance()) {
            return
        }
        val attacker = hit.resolvePlayerSource(players) ?: return
        target.setVengeance(false)
        target.say("Taste vengeance!")
        if (attacker.hitpoints > 0) {
            attacker.takeInstantHit(HitType.Typeless, max(1, damage * 3 / 4), instantHits)
        }
    }
}
