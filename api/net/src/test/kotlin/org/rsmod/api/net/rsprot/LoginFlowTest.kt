package org.rsmod.api.net.rsprot

import java.util.concurrent.ForkJoinPool
import org.junit.jupiter.api.Assertions.assertNotSame
import org.junit.jupiter.api.Test

class LoginFlowTest {
    /**
     * A login hashes its password on the login flow executor. On the common pool (rsprot's default)
     * it shares the pool's one worker (on two cores) with player info, and the game cycle waits a
     * second for every login.
     */
    @Test
    fun `logins never run on the common pool`() {
        assertNotSame(ForkJoinPool.commonPool(), LoginFlow.handlers().loginFlowExecutor)
    }
}
