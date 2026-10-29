package org.rsmod.content.skills.magic.spell.utility

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.parallel.Execution
import org.junit.jupiter.api.parallel.ExecutionMode
import org.rsmod.api.config.refs.components
import org.rsmod.api.config.refs.objs
import org.rsmod.api.config.refs.stats
import org.rsmod.api.testing.GameTestState
import org.rsmod.api.testing.scope.GameTestScope

/* Obj transaction system is not thread-safe. */
@Execution(ExecutionMode.SAME_THREAD)
class ItemSpellTest {
    @Test
    fun GameTestState.`high alchemy pays 60 percent of the value and gives the xp`() =
        utilityTest { deps ->
            setUp(
                objs.nature_rune to 5,
                objs.fire_rune to 50,
                UtilityObjs.adamantite_bar to 1,
                UtilityObjs.adamantite_bar to 1,
            )
            castOnItem(deps, objs.spell_high_alchemy, slot = 2)
            // 640 * 60 / 100
            assertEquals(384, player.inv.count(objs.coins))
            assertEquals(1, player.inv.count(UtilityObjs.adamantite_bar))
            assertEquals(4, player.inv.count(objs.nature_rune))
            assertEquals(45, player.inv.count(objs.fire_rune))
            assertEquals(fineXp(65.0), gained(stats.magic))

            // The next cast waits out the five tick delay.
            castOnItem(deps, objs.spell_high_alchemy, slot = 3)
            assertEquals(384, player.inv.count(objs.coins))
            advance(ticks = 5)
            castOnItem(deps, objs.spell_high_alchemy, slot = 3)
            assertEquals(768, player.inv.count(objs.coins))
        }

    @Test
    fun GameTestState.`low alchemy pays 40 percent`() = utilityTest { deps ->
        setUp(objs.nature_rune to 5, objs.fire_rune to 50, UtilityObjs.adamantite_bar to 1)
        castOnItem(deps, objs.spell_low_alchemy, slot = 2)
        assertEquals(256, player.inv.count(objs.coins))
        assertEquals(fineXp(31.0), gained(stats.magic))
    }

    @Test
    fun GameTestState.`coins cannot be alchemised`() = utilityTest { deps ->
        setUp(objs.nature_rune to 5, objs.fire_rune to 50, objs.coins to 1000)
        castOnItem(deps, objs.spell_high_alchemy, slot = 2)
        assertEquals(1000, player.inv.count(objs.coins))
        assertEquals(5, player.inv.count(objs.nature_rune))
        assertMessageSent("You can't cast this spell on that.")
    }

    @Test
    fun GameTestState.`superheat turns iron ore and coal into a steel bar`() = utilityTest { deps ->
        setUp(
            objs.nature_rune to 5,
            objs.fire_rune to 50,
            UtilityObjs.iron_ore to 1,
            UtilityObjs.coal to 3,
        )
        castOnItem(deps, objs.spell_superheat, slot = 2)
        assertEquals(1, player.inv.count(UtilityObjs.steel_bar))
        assertEquals(0, player.inv.count(UtilityObjs.iron_ore))
        assertEquals(1, player.inv.count(UtilityObjs.coal))
        assertEquals(fineXp(53.0), gained(stats.magic))
        assertEquals(fineXp(17.5), gained(stats.smithing))
    }

    @Test
    fun GameTestState.`superheat makes an iron bar from a lone iron ore`() = utilityTest { deps ->
        setUp(objs.nature_rune to 5, objs.fire_rune to 50, UtilityObjs.iron_ore to 1)
        castOnItem(deps, objs.spell_superheat, slot = 2)
        assertEquals(1, player.inv.count(UtilityObjs.iron_bar))
        assertEquals(fineXp(12.5), gained(stats.smithing))
    }

    @Test
    fun GameTestState.`superheat keeps the runes when it has nothing to smelt`() =
        utilityTest { deps ->
            setUp(objs.nature_rune to 5, objs.fire_rune to 50, UtilityObjs.mithril_ore to 1)
            castOnItem(deps, objs.spell_superheat, slot = 2)
            assertEquals(1, player.inv.count(UtilityObjs.mithril_ore))
            assertEquals(5, player.inv.count(objs.nature_rune))
            assertEquals(0, gained(stats.magic))
        }

    @Test
    fun GameTestState.`bones to bananas converts every bone for one cast`() = utilityTest { deps ->
        setUp(
            objs.nature_rune to 5,
            objs.water_rune to 10,
            objs.earth_rune to 10,
            UtilityObjs.bones to 1,
            UtilityObjs.big_bones to 1,
            objs.coins to 1,
        )
        val component = checkNotNull(deps.spells.getObjSpell(objs.spell_bones_to_bananas)).component
        player.ifButton(component)
        advance()
        assertEquals(2, player.inv.count(UtilityObjs.banana))
        assertEquals(0, player.inv.count(UtilityObjs.bones))
        assertEquals(fineXp(25.0), gained(stats.magic))
        assertEquals(4, player.inv.count(objs.nature_rune))
    }

    @Test
    fun GameTestState.`lvl 1 enchant turns a sapphire ring into a ring of recoil`() =
        utilityTest { deps ->
            setUp(
                UtilityObjs.cosmic_rune to 5,
                objs.water_rune to 5,
                EnchantObjs.levels[0].enchants[0].input to 1,
            )
            castOnItem(deps, objs.spell_enchant_lvl_1, slot = 2)
            val ring = EnchantObjs.levels[0].enchants[0]
            assertEquals(1, player.inv.count(ring.output))
            assertEquals(0, player.inv.count(ring.input))
            assertEquals(fineXp(17.5), gained(stats.magic))
        }

    @Test
    fun GameTestState.`an enchant only works on its own gems`() = utilityTest { deps ->
        setUp(
            UtilityObjs.cosmic_rune to 5,
            objs.air_rune to 20,
            EnchantObjs.levels[0].enchants[0].input to 1,
        )
        castOnItem(deps, objs.spell_enchant_lvl_2, slot = 2)
        assertEquals(1, player.inv.count(EnchantObjs.levels[0].enchants[0].input))
        assertEquals(5, player.inv.count(UtilityObjs.cosmic_rune))
    }

    @Test
    fun GameTestState.`no enchant input is listed twice`() = utilityTest { _ ->
        val inputs = EnchantObjs.levels.flatMap { level -> level.enchants.map { it.input.id } }
        assertEquals(inputs.size, inputs.toSet().size)
        assertEquals(40, inputs.size)
    }

    @Test
    fun GameTestState.`enchant bolt spell converts ten of the stack`() = utilityTest { deps ->
        val bolt = EnchantObjs.bolts[0]
        setUp(UtilityObjs.cosmic_rune to 5, objs.air_rune to 10, bolt.input to 15)
        openBoltMenu(deps)
        player.resumePauseButton(components.skillmulti_a, sub = 1)
        advanceUntil(max = 6) { player.inv.count(bolt.output) == 10 }
        assertEquals(5, player.inv.count(bolt.input))
        assertEquals(4, player.inv.count(UtilityObjs.cosmic_rune))
        assertEquals(fineXp(9.0), gained(stats.magic))
        assertMessageSent("The magic of the runes coaxes out the true nature of the gem tips.")
    }

    @Test
    fun GameTestState.`the bolt menu only offers the bolts the Magic level allows`() =
        utilityTest { deps ->
            val jade = EnchantObjs.bolts[2]
            setUp(UtilityObjs.cosmic_rune to 5, objs.earth_rune to 10, jade.input to 12)
            player.stats[stats.magic] = 14
            openBoltMenu(deps)

            // Opal, sapphire and jade are on offer: the fourth button does nothing.
            player.resumePauseButton(components.skillmulti_d, sub = 1)
            advance(ticks = 2)
            assertEquals(12, player.inv.count(jade.input))

            player.resumePauseButton(components.skillmulti_c, sub = 1)
            advanceUntil(max = 6) { player.inv.count(jade.output) == 10 }
            assertEquals(2, player.inv.count(jade.input))
        }

    @Test
    fun GameTestState.`a bolt menu quantity enchants that many sets`() = utilityTest { deps ->
        val bolt = EnchantObjs.bolts[0]
        setUp(UtilityObjs.cosmic_rune to 5, objs.air_rune to 10, bolt.input to 25)
        openBoltMenu(deps)
        player.resumePauseButton(components.skillmulti_a, sub = 2)
        advanceUntil(max = 6) { player.inv.count(bolt.output) == 10 }
        advanceUntil(max = 6) { player.inv.count(bolt.output) == 20 }
        assertEquals(5, player.inv.count(bolt.input))
        assertEquals(3, player.inv.count(UtilityObjs.cosmic_rune))
    }

    /** Clicks the spellbook's bolt enchantments button, as the client sends it. */
    private fun GameTestScope.openBoltMenu(deps: Deps) {
        // A fresh test player's clock reads -1 until their first tick.
        advance()
        val menu = checkNotNull(deps.spells.getObjSpell(objs.view_bolt_enchantment_spells))
        player.ifButton(menu.component)
        advance(ticks = 2)
    }
}
