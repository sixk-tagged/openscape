package org.rsmod.content.skills.magic.spell.utility

import jakarta.inject.Inject
import org.rsmod.api.combat.commons.magic.MagicSpell
import org.rsmod.api.combat.manager.MagicRuneManager
import org.rsmod.api.combat.manager.MagicRuneManager.Companion.isFailure
import org.rsmod.api.config.refs.objs
import org.rsmod.api.config.refs.stats
import org.rsmod.api.player.protect.ProtectedAccess
import org.rsmod.api.player.protect.ProtectedAccessLauncher
import org.rsmod.api.script.onIfOverlayButton
import org.rsmod.api.spells.MagicSpellRegistry
import org.rsmod.game.inv.isType
import org.rsmod.game.type.obj.ObjType
import org.rsmod.plugin.scripts.PluginScript
import org.rsmod.plugin.scripts.ScriptContext

/**
 * Bones to Bananas and Bones to Peaches: every bone in the inventory becomes the fruit for one
 * cast. Long and curved bones are left alone, as the wiki says.
 */
class BonesToFruitScript
@Inject
constructor(
    private val spells: MagicSpellRegistry,
    private val runes: MagicRuneManager,
    private val launcher: ProtectedAccessLauncher,
) : PluginScript() {
    override fun ScriptContext.startup() {
        cast(objs.spell_bones_to_bananas, UtilityObjs.banana)
        cast(objs.spell_bones_to_peaches, UtilityObjs.peach)
    }

    private fun ScriptContext.cast(obj: ObjType, fruit: ObjType) {
        val spell = checkNotNull(spells.getObjSpell(obj))
        onIfOverlayButton(spell.component) {
            launcher.launch(player) {
                if (actionDelay <= mapClock) {
                    convert(spell, fruit)
                }
            }
        }
    }

    private fun ProtectedAccess.convert(spell: MagicSpell, fruit: ObjType) {
        val slots = inv.mapSlots { _, held -> BONES.any { held.isType(it) } }
        if (slots.isEmpty()) {
            // UNVERIFIED: the wiki lists no message for casting without bones.
            mes("You aren't holding any bones!")
            return
        }
        if (runes.attemptCast(player, spell).isFailure()) {
            return
        }
        for (slot in slots) {
            invReplaceSlot(inv, slot, 1, fruit)
        }
        anim(UtilitySeqs.bones_to_fruit)
        spotanim(UtilitySpots.bones_to_fruit, height = 92)
        soundSynth(UtilitySynths.bones_to_fruit)
        statAdvance(stats.magic, spell.castXp)
        actionDelay = mapClock + CAST_TICKS
    }

    private companion object {
        const val CAST_TICKS = 1

        val BONES =
            listOf(
                UtilityObjs.bones,
                UtilityObjs.bones_burnt,
                UtilityObjs.bat_bones,
                UtilityObjs.big_bones,
                UtilityObjs.wolf_bones,
                UtilityObjs.jogre_bones,
                UtilityObjs.monkey_bones,
                UtilityObjs.bleached_bones,
                UtilityObjs.animals_bones_1,
                UtilityObjs.animals_bones_2,
                UtilityObjs.animals_bones_3,
                UtilityObjs.animals_bones_4,
            )
    }
}
