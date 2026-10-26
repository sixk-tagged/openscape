package org.rsmod.content.skills.magic.spell.attacks

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.rsmod.api.config.refs.objs
import org.rsmod.api.testing.GameTestState

class RuneRefsTest {
    @Test
    fun GameTestState.`the death rune ref is the death rune`() {
        assertEquals("deathrune", cacheTypes.objs[objs.death_rune].internalName)
    }
}
