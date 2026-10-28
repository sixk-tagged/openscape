package org.rsmod.content.other.npcattacks

import jakarta.inject.Inject
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.rsmod.api.combat.commons.npc.NpcAttackStyle
import org.rsmod.api.combat.commons.npc.NpcAttackTable
import org.rsmod.api.testing.GameTestState

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

    class Deps @Inject constructor(val table: NpcAttackTable)
}
