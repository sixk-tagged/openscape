package org.rsmod.content.skills.magic.spell.utility

import jakarta.inject.Inject
import org.rsmod.api.combat.commons.magic.MagicSpell
import org.rsmod.api.combat.manager.MagicRuneManager
import org.rsmod.api.combat.manager.MagicRuneManager.Companion.isFailure
import org.rsmod.api.config.refs.objs
import org.rsmod.api.config.refs.params
import org.rsmod.api.config.refs.stats
import org.rsmod.api.player.protect.ProtectedAccess
import org.rsmod.api.player.protect.ProtectedAccessLauncher
import org.rsmod.api.spells.MagicSpellRegistry
import org.rsmod.game.inv.isType
import org.rsmod.game.type.obj.ObjType
import org.rsmod.game.type.obj.ObjTypeList
import org.rsmod.game.type.obj.UnpackedObjType
import org.rsmod.game.type.obj.isType
import org.rsmod.game.type.seq.SeqType
import org.rsmod.game.type.spot.SpotanimType
import org.rsmod.game.type.synth.SynthType
import org.rsmod.plugin.scripts.PluginScript
import org.rsmod.plugin.scripts.ScriptContext

/**
 * Low and High Level Alchemy: the item is swapped for coins worth 40% (low) or 60% (high) of its
 * cache value, rounded down. Coins and items flagged `no_alchemy` cannot be alchemised. The cast
 * delay is shared with the weapon attack delay, as in OSRS.
 *
 * Note: noted items use the unnoted item's value; the "alchemy warning" prompts are client
 * side.
 */
class AlchemyScript
@Inject
constructor(
    private val spells: MagicSpellRegistry,
    private val runes: MagicRuneManager,
    private val objTypes: ObjTypeList,
    private val launcher: ProtectedAccessLauncher,
) : PluginScript() {
    private class Alchemy(
        val obj: ObjType,
        val ticks: Int,
        val seq: SeqType,
        val spot: SpotanimType,
        val sound: SynthType,
        val value: (UnpackedObjType) -> Int,
    )

    override fun ScriptContext.startup() {
        for (alchemy in ALCHEMY) {
            val spell = checkNotNull(spells.getObjSpell(alchemy.obj))
            onSpellOnItem(spell, launcher) { slot, obj -> alch(alchemy, spell, slot, obj) }
        }
    }

    private fun ProtectedAccess.alch(
        alchemy: Alchemy,
        spell: MagicSpell,
        slot: Int,
        obj: UnpackedObjType,
    ) {
        if (actionDelay > mapClock) {
            return
        }
        val held = inv[slot]
        if (held == null || !held.isType(obj)) {
            return
        }
        val base = if (obj.isCert) objTypes[obj.certlink] ?: obj else obj
        if (base.isType(objs.coins) || base.param(params.no_alchemy) != 0) {
            // UNVERIFIED: the wiki lists no message for refused items.
            mes("You can't cast this spell on that.")
            return
        }
        val coins = alchemy.value(base)
        val roomForCoins = coins == 0 || objs.coins in inv || inv.hasFreeSpace() || held.count == 1
        if (!roomForCoins) {
            mes("You don't have enough inventory space to do that.")
            return
        }
        if (runes.attemptCast(player, spell).isFailure()) {
            return
        }
        invDel(inv, obj, 1, slot = slot)
        if (coins > 0) {
            invAdd(inv, objs.coins, coins)
        }
        anim(alchemy.seq)
        spotanim(alchemy.spot, height = 92)
        soundSynth(alchemy.sound)
        statAdvance(stats.magic, spell.castXp)
        actionDelay = mapClock + alchemy.ticks
    }

    private companion object {
        val ALCHEMY =
            listOf(
                Alchemy(
                    objs.spell_low_alchemy,
                    ticks = 3,
                    UtilitySeqs.low_alchemy,
                    UtilitySpots.low_alchemy,
                    UtilitySynths.low_alchemy,
                    UnpackedObjType::lowAlch,
                ),
                Alchemy(
                    objs.spell_high_alchemy,
                    ticks = 5,
                    UtilitySeqs.high_alchemy,
                    UtilitySpots.high_alchemy,
                    UtilitySynths.high_alchemy,
                    UnpackedObjType::highAlch,
                ),
            )
    }
}
