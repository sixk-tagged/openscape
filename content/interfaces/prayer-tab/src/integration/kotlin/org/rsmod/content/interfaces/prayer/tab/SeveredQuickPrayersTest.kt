package org.rsmod.content.interfaces.prayer.tab

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.rsmod.api.config.refs.stats
import org.rsmod.api.config.refs.varbits
import org.rsmod.api.player.severProtectionPrayers
import org.rsmod.api.testing.GameTestState
import org.rsmod.api.testing.scope.GameTestScope
import org.rsmod.content.interfaces.prayer.tab.configs.PrayerTabQueues
import org.rsmod.content.interfaces.prayer.tab.configs.PrayerTabVarBits
import org.rsmod.content.interfaces.prayer.tab.scripts.PrayerDrainScript
import org.rsmod.content.interfaces.prayer.tab.scripts.QuickPrayerScript
import org.rsmod.game.type.varbit.VarBitType

class SeveredQuickPrayersTest {
    @Test
    fun GameTestState.`severed protect-only quick prayers stay off instead of breaking`() =
        prayerTest {
            selectQuickPrayers(varbits.protect_from_melee)
            player.severProtectionPrayers(cycles = 8)
            player.strongQueue(PrayerTabQueues.quick_prayer, 1)
            advance()
            assertMessageSent(SEVERED_MESSAGE)
            assertEquals(0, player.vars[varbits.quickprayer_active])
            assertEquals(0, player.vars[varbits.enabled_prayers])
            advance(ticks = 3)
            assertEquals(0, player.vars[varbits.quickprayer_active])
        }

    @Test
    fun GameTestState.`severed quick prayers still turn on everything else`() = prayerTest {
        selectQuickPrayers(varbits.protect_from_melee, varbits.piety)
        player.severProtectionPrayers(cycles = 8)
        player.strongQueue(PrayerTabQueues.quick_prayer, 1)
        advance()
        assertEquals(1, player.vars[varbits.piety])
        assertEquals(0, player.vars[varbits.protect_from_melee])
        assertEquals(1, player.vars[varbits.quickprayer_active])
    }

    private fun GameTestState.prayerTest(test: GameTestScope.() -> Unit) =
        runInjectedGameTest(
            PrayerRepository::class,
            PrayerModule(),
            QuickPrayerScript::class,
            PrayerDrainScript::class,
        ) {
            test()
        }

    /** Selects [prayers] as the quick prayers, with every prayer currently off. */
    private fun GameTestScope.selectQuickPrayers(vararg prayers: VarBitType) {
        advance() // A fresh test player's clock reads -1 until their first tick.
        player.stats[stats.prayer] = 99
        player.stats[stats.defence] = 99
        player.setVarBit(varbits.kr_knightwaves_state, 8)
        for (prayer in prayers) {
            player.setVarBit(prayer, 1)
        }
        val selected = player.vars[varbits.enabled_prayers]
        player.setVarBit(varbits.enabled_prayers, 0)
        player.setVarBit(PrayerTabVarBits.quickprayer_selected, selected)
    }
}
