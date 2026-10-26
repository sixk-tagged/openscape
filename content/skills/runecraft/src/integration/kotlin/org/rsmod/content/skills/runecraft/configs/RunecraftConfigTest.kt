package org.rsmod.content.skills.runecraft.configs

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.rsmod.api.testing.GameTestState
import org.rsmod.game.interact.InteractionOp

class RunecraftConfigTest {
    @Test
    fun GameTestState.`every ruins is a multiloc on its enter bit`() = runBasicGameTest {
        for (altar in Altars.withRuins) {
            val ruins = checkNotNull(altar.ruins)
            val type = cacheTypes.locs[ruins.loc]
            assertEquals(cacheTypes.varbits[ruins.bit].id, type.multiVarBit, altar.name)
            // Child 0 is the old ruins without ops, child 1 the new one that says `Enter`.
            assertEquals(2, type.multiLoc.size, altar.name)
            val old = checkNotNull(cacheTypes.locs[type.multiLoc[0].toInt() and 0xFFFF])
            val new = checkNotNull(cacheTypes.locs[type.multiLoc[1].toInt() and 0xFFFF])
            assertFalse(old.hasOp(InteractionOp.Op1), altar.name)
            assertTrue(new.hasOp(InteractionOp.Op1), altar.name)
        }
    }

    @Test
    fun GameTestState.`altars and portals have their first option`() = runBasicGameTest {
        for (altar in Altars.all) {
            assertTrue(cacheTypes.locs[altar.loc].hasOp(InteractionOp.Op1), altar.name)
            altar.ruins?.let {
                assertTrue(cacheTypes.locs[it.portal].hasOp(InteractionOp.Op1), altar.name)
            }
        }
    }

    @Test
    fun GameTestState.`the vars the client reads are transmitted`() = runBasicGameTest {
        for (bit in Altars.withRuins.map { checkNotNull(it.ruins).bit }) {
            assertFalse(cacheTypes.varps[bit.baseVar].transmit.never, bit.internalNameValue)
        }
        assertFalse(cacheTypes.varps[RunecraftVarps.rune_mysteries].transmit.never)
    }

    @Test
    fun GameTestState.`multiples follow the wiki thresholds`() = runBasicGameTest {
        assertEquals(1, Altars.runesPerEssence(Altars.air, 10))
        assertEquals(2, Altars.runesPerEssence(Altars.air, 11))
        assertEquals(10, Altars.runesPerEssence(Altars.air, 99))
        assertEquals(8, Altars.runesPerEssence(Altars.mind, 98))
        // Earth reaches five runes only with a boost over 99.
        assertEquals(4, Altars.runesPerEssence(Altars.earth, 99))
        assertEquals(5, Altars.runesPerEssence(Altars.earth, 104))
        assertEquals(1, Altars.runesPerEssence(Altars.blood, 99))
    }
}
