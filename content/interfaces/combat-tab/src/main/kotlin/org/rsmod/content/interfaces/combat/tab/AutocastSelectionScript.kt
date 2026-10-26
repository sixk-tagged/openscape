package org.rsmod.content.interfaces.combat.tab

import jakarta.inject.Inject
import org.rsmod.api.combat.commons.magic.Spellbook
import org.rsmod.api.config.refs.components
import org.rsmod.api.config.refs.interfaces
import org.rsmod.api.config.refs.varbits
import org.rsmod.api.player.output.mes
import org.rsmod.api.player.righthand
import org.rsmod.api.player.ui.ifOpenSub
import org.rsmod.api.player.ui.ifSetEvents
import org.rsmod.api.player.vars.boolVarBit
import org.rsmod.api.player.vars.enumVarBit
import org.rsmod.api.player.vars.intVarBit
import org.rsmod.api.script.onIfOverlayButton
import org.rsmod.api.script.onPlayerLogout
import org.rsmod.api.spells.MagicSpellRegistry
import org.rsmod.api.spells.autocast.AutocastWeapons
import org.rsmod.content.interfaces.combat.tab.configs.AutocastComponents
import org.rsmod.content.interfaces.combat.tab.configs.AutocastInterfaces
import org.rsmod.events.EventBus
import org.rsmod.game.entity.Player
import org.rsmod.game.type.interf.IfEvent
import org.rsmod.game.type.interf.IfSubType
import org.rsmod.game.type.obj.ObjTypeList
import org.rsmod.game.type.obj.WeaponCategory
import org.rsmod.plugin.scripts.PluginScript
import org.rsmod.plugin.scripts.ScriptContext

/**
 * openscape: the combat tab's "Choose spell" buttons. They swap the combat tab for the `autocast`
 * spell list; picking a spell stores it through [AutocastWeapons] (the same storage that is loaded
 * back when a staff is wielded) and puts the combat tab back.
 */
class AutocastSelectionScript
@Inject
constructor(
    private val eventBus: EventBus,
    private val objTypes: ObjTypeList,
    private val spells: MagicSpellRegistry,
    private val autocast: AutocastWeapons,
) : PluginScript() {
    private var Player.autocastEnabled by boolVarBit(varbits.autocast_set)
    private var Player.autocastSpell by intVarBit(varbits.autocast_spell)
    private var Player.defensiveCasting by boolVarBit(varbits.autocast_defmode)
    private val Player.spellbook by enumVarBit<Spellbook>(varbits.spellbook)

    /** Players whose open spell list was started from the defensive button. */
    private val defensivePicks = HashSet<Player>()

    override fun ScriptContext.startup() {
        onIfOverlayButton(AutocastComponents.choose_spell) { player.openSpells(defensive = false) }
        onIfOverlayButton(AutocastComponents.choose_defensive_spell) {
            player.openSpells(defensive = true)
        }
        onIfOverlayButton(AutocastComponents.spells) { player.selectSpell(comsub) }
        onIfOverlayButton(AutocastComponents.cancel_3) { player.closeSpells() }
        onIfOverlayButton(AutocastComponents.cancel_4) { player.closeSpells() }
        onIfOverlayButton(AutocastComponents.cancel_5) { player.closeSpells() }
        // Logging out with the list open would otherwise keep the player in the set forever.
        onPlayerLogout { defensivePicks -= player }
    }

    private fun Player.openSpells(defensive: Boolean) {
        if (defensive) defensivePicks += this else defensivePicks -= this
        ifOpenSub(
            AutocastInterfaces.autocast,
            components.toplevel_target_side0,
            IfSubType.Overlay,
            eventBus,
        )
        ifSetEvents(AutocastComponents.spells, 0..MAX_AUTOCAST_ID, IfEvent.Op1)
    }

    private fun Player.closeSpells() {
        defensivePicks -= this
        ifOpenSub(
            interfaces.combat_interface,
            components.toplevel_target_side0,
            IfSubType.Overlay,
            eventBus,
        )
    }

    private fun Player.selectSpell(autocastId: Int) {
        val defensive = this in defensivePicks
        val spell = spells.getAutocastSpell(autocastId)
        val weapon = objTypes.getOrNull(righthand)
        closeSpells()
        if (spell == null || weapon == null) {
            return
        }
        if (spell.spellbook != spellbook) {
            mes("You can't autocast that spell with your current active spellbook.")
            return
        }
        if (!autocast.canStaffAutocast(this, weapon, autocastId)) {
            return
        }
        val saved =
            autocast.getVarBits(WeaponCategory.getOrUnarmed(weapon.weaponCategory)) ?: return
        autocast.set(this, saved, autocastId, defensive)
        autocastSpell = autocastId
        defensiveCasting = defensive
        autocastEnabled = true
    }

    private companion object {
        const val MAX_AUTOCAST_ID = 255
    }
}
