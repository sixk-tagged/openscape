package org.rsmod.content.interfaces.combat.tab

import com.google.inject.AbstractModule
import jakarta.inject.Inject
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.parallel.Execution
import org.junit.jupiter.api.parallel.ExecutionMode
import org.rsmod.api.config.refs.objs
import org.rsmod.api.config.refs.varbits
import org.rsmod.api.player.ui.ifSetEvents
import org.rsmod.api.spells.MagicSpellModule
import org.rsmod.api.spells.MagicSpellRegistry
import org.rsmod.api.spells.autocast.scripts.AutocastScript
import org.rsmod.api.testing.GameTestState
import org.rsmod.content.interfaces.combat.tab.configs.AutocastComponents
import org.rsmod.game.inv.InvObj
import org.rsmod.game.type.interf.IfEvent
import org.rsmod.game.type.obj.ObjType
import org.rsmod.game.type.obj.Wearpos

/* Obj transaction system is not thread-safe. */
@Execution(ExecutionMode.SAME_THREAD)
class AutocastSelectionTest {
    @Test
    fun GameTestState.`picking a spell in the list autocasts it`() = autocastTest { deps ->
        player.worn[Wearpos.RightHand.slot] = InvObj(objs.air_staff)
        val id = deps.autocastId(objs.spell_wind_strike)
        player.ifOpenOverlay(
            org.rsmod.content.interfaces.combat.tab.configs.AutocastInterfaces.autocast,
            org.rsmod.api.config.refs.components.toplevel_target_side0,
        )
        player.ifSetEvents(AutocastComponents.spells, 0..255, IfEvent.Op1)
        player.ifButton(AutocastComponents.spells, comsub = id)
        advance()
        val got =
            listOf(
                id,
                player.vars[varbits.autocast_spell],
                player.vars[varbits.autocast_set],
                player.vars[varbits.saved_autocast_spell_staff],
            )
        assertEquals(listOf(id, id, 1, id), got)
    }

    @Test
    fun GameTestState.`a spell from another spellbook is refused`() = autocastTest { deps ->
        player.worn[Wearpos.RightHand.slot] = InvObj(objs.air_staff)
        val id = deps.autocastId(objs.spell_ice_rush)
        player.ifOpenOverlay(
            org.rsmod.content.interfaces.combat.tab.configs.AutocastInterfaces.autocast,
            org.rsmod.api.config.refs.components.toplevel_target_side0,
        )
        player.ifSetEvents(AutocastComponents.spells, 0..255, IfEvent.Op1)
        player.ifButton(AutocastComponents.spells, comsub = id)
        advance()
        assertFalse(player.vars[varbits.autocast_set] == 1)
    }

    class Deps @Inject constructor(private val spells: MagicSpellRegistry) {
        fun autocastId(spell: ObjType): Int =
            (0..255).first { spells.getAutocastSpell(it)?.obj?.id == spell.id }
    }

    private object Module : AbstractModule() {
        override fun configure() {
            install(MagicSpellModule())
        }
    }

    private fun GameTestState.autocastTest(
        test: org.rsmod.api.testing.scope.GameTestScope.(Deps) -> Unit
    ) =
        runInjectedGameTest(
            Deps::class,
            Module,
            AutocastScript::class,
            AutocastSelectionScript::class,
            testBody = test,
        )
}
