package org.rsmod.content.other.consumables

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.parallel.Execution
import org.junit.jupiter.api.parallel.ExecutionMode
import org.rsmod.api.config.constants
import org.rsmod.api.config.refs.stats
import org.rsmod.api.config.refs.varbits
import org.rsmod.api.config.refs.varps
import org.rsmod.api.player.stat.hitpoints
import org.rsmod.api.player.stat.stat
import org.rsmod.api.testing.GameTestState
import org.rsmod.api.testing.scope.GameTestScope
import org.rsmod.content.skills.herblore.Potions
import org.rsmod.game.inv.InvObj
import org.rsmod.game.inv.isType
import org.rsmod.game.type.obj.ObjType

/* Obj transaction system is not thread-safe. */
@Execution(ExecutionMode.SAME_THREAD)
class HerblorePotionsTest {
    @Test
    fun GameTestState.`super attack boosts attack by 5 plus 15 percent`() =
        runGameTest(ConsumablesScript::class) {
            setUp(Potions.superAttack[0])
            player.withProtectedAccess { opHeld1(0) }
            assertEquals(99 + 5 + 14, player.stat(stats.attack))
            assertTrue(player.inv[0].isType(Potions.superAttack[1]))
            assertMessageSent("You drink some of your super attack potion.")
        }

    @Test
    fun GameTestState.`restore potion restores drained combat stats but not prayer`() =
        runGameTest(ConsumablesScript::class) {
            setUp(Potions.restore[0])
            player.setCurrentLevel(stats.attack, 40)
            player.setCurrentLevel(stats.prayer, 10)
            player.withProtectedAccess { opHeld1(0) }
            assertEquals(40 + 10 + 29, player.stat(stats.attack))
            assertEquals(10, player.stat(stats.prayer))
        }

    @Test
    fun GameTestState.`an energy potion restores 15 percent run energy`() =
        runGameTest(ConsumablesScript::class) {
            setUp(Potions.energy[0])
            player.runEnergy = 2000
            player.withProtectedAccess { opHeld1(0) }
            assertEquals(2000 + constants.run_max_energy * 15 / 100, player.runEnergy)
        }

    @Test
    fun GameTestState.`zamorak brew trades defence and hitpoints for offence`() =
        runGameTest(ConsumablesScript::class) {
            setUp(Potions.zamorak[0])
            player.withProtectedAccess { opHeld1(0) }
            assertEquals(99 + 2 + 19, player.stat(stats.attack))
            assertEquals(99 + 2 + 11, player.stat(stats.strength))
            assertEquals(99 - 11, player.stat(stats.defence))
            assertEquals(99 - 11, player.hitpoints)
        }

    @Test
    fun GameTestState.`a stamina potion slows energy loss for two minutes`() =
        runGameTest(ConsumablesScript::class, PotionEffects::class) {
            setUp(Potions.stamina[0])
            player.runEnergy = 5000
            player.withProtectedAccess { opHeld1(0) }
            assertEquals(7000, player.runEnergy)
            assertEquals(1, player.vars[varbits.stamina_active])
            advance(ticks = 205)
            assertEquals(0, player.vars[varbits.stamina_active])
        }

    @Test
    fun GameTestState.`an antifire potion warns before it runs out`() =
        runGameTest(ConsumablesScript::class, PotionEffects::class) {
            setUp(Potions.superAntifire[0])
            player.withProtectedAccess { opHeld1(0) }
            assertEquals(Antifire.SUPER, player.antifireKind)
            advanceUntil({ player.antifireKind == Antifire.NONE }, timeoutTicks = 320)
        }

    @Test
    fun GameTestState.`a divine potion costs 10 hitpoints and refuses at 10`() =
        runGameTest(ConsumablesScript::class, PotionEffects::class) {
            setUp(Potions.divineAttack[0])
            player.withProtectedAccess { opHeld1(0) }
            assertEquals(89, player.hitpoints)
            assertEquals(99 + 5 + 14, player.stat(stats.attack))

            setUp(Potions.divineAttack[0])
            player.setCurrentLevel(stats.hitpoints, 10)
            advance(ticks = 3) // The first sip's potion delay.
            player.withProtectedAccess { opHeld1(0) }
            assertMessageSent("You do not have enough Hitpoints to drink that.")
            assertTrue(player.inv[0].isType(Potions.divineAttack[0]))
        }

    @Test
    fun GameTestState.`a surge potion restores special energy once every five minutes`() =
        runGameTest(ConsumablesScript::class) {
            setUp(Potions.surge[0], Potions.surge[0])
            player.withProtectedAccess {
                vars[varps.sa_energy] = 100
                opHeld1(0)
                assertEquals(350, vars[varps.sa_energy])
            }
            advance(ticks = 4)
            player.withProtectedAccess { opHeld1(1) }
            assertMessageSent("You can only drink a surge potion every 5 minutes.")
            assertTrue(player.inv[1].isType(Potions.surge[0]))
        }

    /** 99 in every stat, full hitpoints, and [objs] from slot 0. */
    private fun GameTestScope.setUp(vararg objs: ObjType) {
        // A fresh test player's clock reads -1 until their first tick.
        advance()
        for (stat in COMBAT_STATS) {
            player.stats[stat] = 99
        }
        player.clearInv()
        objs.forEachIndexed { slot, obj -> player.inv[slot] = InvObj(obj) }
    }

    private companion object {
        val COMBAT_STATS =
            listOf(
                stats.attack,
                stats.strength,
                stats.defence,
                stats.ranged,
                stats.magic,
                stats.prayer,
                stats.hitpoints,
            )
    }
}
