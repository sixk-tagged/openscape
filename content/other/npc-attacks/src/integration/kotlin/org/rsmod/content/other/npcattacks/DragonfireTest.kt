package org.rsmod.content.other.npcattacks

import jakarta.inject.Inject
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.rsmod.api.combat.commons.npc.NpcAttackStyle
import org.rsmod.api.combat.commons.npc.NpcAttackTable
import org.rsmod.api.config.refs.varbits
import org.rsmod.api.config.refs.varps
import org.rsmod.api.player.lefthand
import org.rsmod.api.player.magic.isFrozen
import org.rsmod.api.random.GameRandom
import org.rsmod.api.testing.GameTestState
import org.rsmod.api.testing.scope.GameTestScope
import org.rsmod.api.type.refs.npc.NpcReferences
import org.rsmod.content.other.consumables.Antifire
import org.rsmod.content.other.consumables.configs.PotionVarps
import org.rsmod.game.inv.InvObj

class DragonfireTest {
    @Test
    fun GameTestState.`an unprotected player is burnt for up to 50, or 30 if the roll is won`() =
        runGameTest {
            dress()
            assertEquals(50, Dragonfire.roll(player, accurate = false, TopRandom))
            assertMessageSent("You're horribly burnt by the dragon fire!")

            assertEquals(30, Dragonfire.roll(player, accurate = true, TopRandom))
            assertMessageSent("You manage to resist some of the dragon fire!")
        }

    @Test
    fun GameTestState.`a shield and an antifire potion together stop the fire`() = runGameTest {
        dress(shield = true)
        assertEquals(5, Dragonfire.roll(player, accurate = false, TopRandom))

        dress(shield = true, potion = Antifire.NORMAL)
        assertEquals(0, Dragonfire.roll(player, accurate = false, TopRandom))

        dress(shield = true, potion = Antifire.EXTENDED)
        assertEquals(0, Dragonfire.roll(player, accurate = false, TopRandom))
    }

    @Test
    fun GameTestState.`an antifire potion alone takes 15 off, a super one stops it`() =
        runGameTest {
            dress(potion = Antifire.NORMAL)
            assertEquals(35, Dragonfire.roll(player, accurate = false, TopRandom))
            assertEquals(15, Dragonfire.roll(player, accurate = true, TopRandom))

            dress(potion = Antifire.SUPER)
            assertEquals(0, Dragonfire.roll(player, accurate = false, TopRandom))
            assertMessageSent("Your super antifire potion helps you resist the dragon fire!")
        }

    @Test
    fun GameTestState.`Protect from Magic caps the fire at 10 and a shield at 5`() = runGameTest {
        dress(prayer = true)
        assertEquals(10, Dragonfire.roll(player, accurate = false, TopRandom))

        dress(prayer = true, shield = true)
        assertEquals(5, Dragonfire.roll(player, accurate = false, TopRandom))

        dress(prayer = true, potion = Antifire.NORMAL)
        assertEquals(0, Dragonfire.roll(player, accurate = false, TopRandom))
    }

    @Test
    fun GameTestState.`the adult dragons breathe fire one attack in six and baby dragons do not`() =
        runInjectedGameTest(Deps::class, null, NpcAttackProfileScript::class) { deps ->
            val adults =
                listOf(
                    NpcAttackNpcs.green_dragon5,
                    NpcAttackNpcs.black_dragon4,
                    NpcAttackNpcs.lava_dragon,
                )
            for (adult in adults) {
                val attacks = checkNotNull(deps.table[adult]).attacks
                val breath = attacks.single { it.rollDamage != null }
                assertEquals(NpcAttackStyle.Magic, breath.style)
                assertEquals(NpcAttackStyle.Melee, attacks.single { it.rollDamage == null }.style)
                assertEquals(6, attacks.sumOf { it.weight })
                assertEquals(1, breath.weight)
            }

            val babies = listOf(BabyDragons.blue, BabyDragons.red, BabyDragons.green)
            for (baby in babies + BabyDragons.black) {
                assertNull(deps.table[baby])
            }
        }

    @Test
    fun GameTestState.`a chaos druid binds for a moment and does no damage`() =
        runInjectedGameTest(Deps::class, null, NpcAttackProfileScript::class) { deps ->
            val druid = checkNotNull(deps.table[NpcAttackNpcs.chaos_druid])
            val bind = druid.attacks.single { it.onLand != null }
            assertEquals(NpcAttackStyle.Magic, bind.style)
            assertEquals(0, bind.maxHit)
            assertTrue(druid.attacks.any { it.style == NpcAttackStyle.Melee && it.maxHit == 2 })
            // A fresh test player's clock reads -1 (so frozen) until their first tick.
            advance()
            assertFalse(player.isFrozen())

            bind.onLand?.invoke(player)
            assertTrue(player.isFrozen())
        }

    @Test
    fun GameTestState.`an elder chaos druid tele blocks for half as long under the prayer`() =
        runInjectedGameTest(Deps::class, null, NpcAttackProfileScript::class) { deps ->
            val druid = checkNotNull(deps.table[NpcAttackNpcs.elder_chaos_druid])
            assertTrue(druid.attacks.any { it.style == NpcAttackStyle.Magic && it.maxHit == 17 })
            val block = druid.attacks.single { it.onLand != null }

            block.onLand?.invoke(player)
            assertEquals(500, player.vars[varps.teleblock_ticks])
            assertMessageSent(
                "<col=800080>A Tele Block has been cast on you by the Elder Chaos druid. " +
                    "It will expire in 5 minutes.</col>"
            )

            player.setVarp(varps.teleblock_ticks, 0)
            player.setVarBit(varbits.protect_from_magic, 1)
            block.onLand?.invoke(player)
            assertEquals(250, player.vars[varps.teleblock_ticks])
        }

    /** What the player wears (an anti-dragon shield), prays and drinks against dragonfire. */
    private fun GameTestScope.dress(
        shield: Boolean = false,
        prayer: Boolean = false,
        potion: Int = Antifire.NONE,
    ) {
        player.lefthand = if (shield) InvObj(NpcAttackObjs.anti_dragon_shield) else null
        player.setVarBit(varbits.protect_from_magic, if (prayer) 1 else 0)
        player.setVarp(PotionVarps.antifire_kind, potion)
    }

    class Deps @Inject constructor(val table: NpcAttackTable)

    /** Always rolls the top of the range: the most a breath can do. */
    private object TopRandom : GameRandom {
        override fun of(maxExclusive: Int): Int = maxExclusive - 1

        override fun of(minInclusive: Int, maxInclusive: Int): Int = maxInclusive

        override fun randomDouble(): Double = 0.0
    }
}

internal object BabyDragons : NpcReferences() {
    val blue = find("babybluedragon")
    val red = find("babyreddragon")
    val green = find("babygreendragon1")
    val black = find("baby_black_dragon_strongholdcave")
}
