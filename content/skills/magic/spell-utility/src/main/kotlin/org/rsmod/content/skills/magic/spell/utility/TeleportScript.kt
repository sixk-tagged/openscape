package org.rsmod.content.skills.magic.spell.utility

import jakarta.inject.Inject
import org.rsmod.api.combat.commons.magic.MagicSpell
import org.rsmod.api.combat.manager.MagicRuneManager
import org.rsmod.api.combat.manager.MagicRuneManager.Companion.isFailure
import org.rsmod.api.config.refs.objs
import org.rsmod.api.config.refs.params
import org.rsmod.api.config.refs.stats
import org.rsmod.api.player.protect.ProtectedAccessLauncher
import org.rsmod.api.script.onIfOverlayButton
import org.rsmod.api.spells.MagicSpellRegistry
import org.rsmod.content.other.consumables.endGoading
import org.rsmod.game.entity.Player
import org.rsmod.game.type.obj.ObjTypeList
import org.rsmod.map.CoordGrid
import org.rsmod.plugin.scripts.PluginScript
import org.rsmod.plugin.scripts.ScriptContext

/**
 * The standard spellbook teleports, Varrock to Ape Atoll. The destination is the spell's
 * `spell_telecoord` param and the runes (the banana of Ape Atoll included) are its cache
 * requirements. The runes are taken at the click; the player lands [CAST_TICKS] later.
 *
 * Note: the alternative destinations (Varrock's Grand Exchange, Camelot's Seers' Village...) need
 * diary state and a destination menu; the default destination is always used.
 */
class TeleportScript
@Inject
constructor(
    private val spells: MagicSpellRegistry,
    private val runes: MagicRuneManager,
    private val objTypes: ObjTypeList,
    private val launcher: ProtectedAccessLauncher,
) : PluginScript() {
    override fun ScriptContext.startup() {
        for (obj in TELEPORTS) {
            val spell = checkNotNull(spells.getObjSpell(obj)) { "No spell for teleport: $obj" }
            val dest =
                checkNotNull(objTypes[obj].paramOrNull(params.spell_telecoord)) {
                    "No destination for teleport: $obj"
                }
            onIfOverlayButton(spell.component) { cast(player, spell, dest) }
        }
    }

    private fun cast(player: Player, spell: MagicSpell, dest: CoordGrid) {
        launcher.launch(player) {
            if (runes.attemptCast(player, spell).isFailure()) {
                return@launch
            }
            anim(UtilitySeqs.teleport)
            spotanim(UtilitySpots.teleport, height = 92)
            soundSynth(UtilitySynths.teleport)
            statAdvance(stats.magic, spell.castXp)
            delay(CAST_TICKS)
            teleport(dest)
            player.endGoading()
            anim(UtilitySeqs.teleport_end)
        }
    }

    private companion object {
        // UNVERIFIED: the wiki gives no tick count; 3 ticks matches how the animation plays.
        const val CAST_TICKS = 3

        val TELEPORTS =
            listOf(
                objs.spell_varrock_teleport,
                objs.spell_lumbridge_teleport,
                objs.spell_falador_teleport,
                objs.spell_camelot_teleport,
                objs.spell_kourendcastle_teleport,
                objs.spell_ardougne_teleport,
                objs.spell_civitas_fortis_teleport,
                objs.spell_watchtower_teleport,
                objs.spell_trollheim_teleport,
                objs.spell_apeatoll_teleport,
            )
    }
}
