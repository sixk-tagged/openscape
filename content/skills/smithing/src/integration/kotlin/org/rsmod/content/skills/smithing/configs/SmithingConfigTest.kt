package org.rsmod.content.skills.smithing.configs

import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.rsmod.api.testing.GameTestState

class SmithingConfigTest {
    @Test
    fun GameTestState.`furnace and anvil have content groups`() = runBasicGameTest {
        assertTrue(cacheTypes.locs[SmithingLocs.furnace].isContentType(SmithingContent.furnace))
        assertTrue(cacheTypes.locs[SmithingLocs.anvil].isContentType(SmithingContent.anvil))
    }
}
