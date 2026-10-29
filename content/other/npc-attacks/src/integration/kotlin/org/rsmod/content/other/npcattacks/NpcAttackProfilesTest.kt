package org.rsmod.content.other.npcattacks

import jakarta.inject.Inject
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.rsmod.api.combat.commons.npc.NpcAttackStyle
import org.rsmod.api.combat.commons.npc.NpcAttackTable
import org.rsmod.api.player.lefthand
import org.rsmod.api.player.magic.isFrozen
import org.rsmod.api.testing.GameTestState
import org.rsmod.game.inv.InvObj

class NpcAttackProfilesTest {
    @Test
    fun GameTestState.`every profiled npc is registered and stands off at its range`() =
        runInjectedGameTest(Deps::class, null, NpcAttackProfileScript::class) { deps ->
            for ((npcs, profile) in NpcAttackProfiles.PROFILES) {
                for (npc in npcs) {
                    assertEquals(profile, deps.table[npc])
                    assertEquals(profile.range, npcTypes[npc].attackRange)
                }
            }
        }

    @Test
    fun GameTestState.`dark wizards cast their curse as a magic attack that does no damage`() =
        runInjectedGameTest(Deps::class, null, NpcAttackProfileScript::class) { deps ->
            val low = checkNotNull(deps.table[NpcAttackNpcs.young_dark_wizard])
            val curse = low.attacks.single { it.drain != null }
            assertEquals(NpcAttackStyle.Magic, curse.style)
            assertEquals(0, curse.maxHit)
            assertTrue(low.attacks.any { it.drain == null && it.maxHit == 2 })
        }

    @Test
    fun GameTestState.`the icy breath freezes unless the ancient wyvern shield is worn`() =
        runInjectedGameTest(Deps::class, null, NpcAttackProfileScript::class) { deps ->
            val wyvern = checkNotNull(deps.table[NpcAttackNpcs.skeletal_wyvern1])
            val breath = wyvern.attacks.single { it.onLand != null }
            assertEquals(NpcAttackStyle.Magic, breath.style)
            // A fresh test player's clock reads -1 (so frozen) until their first tick.
            advance()

            player.lefthand = InvObj(NpcAttackObjs.wyvern_shield)
            breath.onLand?.invoke(player)
            assertFalse(player.isFrozen())

            player.lefthand = null
            breath.onLand?.invoke(player)
            assertTrue(player.isFrozen())
        }

    class Deps @Inject constructor(val table: NpcAttackTable)
}
