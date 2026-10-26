package org.rsmod.api.net.rsprot

import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicInteger
import net.rsprot.protocol.api.handlers.LoginHandlers

/**
 * Logins get threads of their own. A login hashes its password on the login flow executor, and
 * rsprot's default is the common pool: the pool player info is computed on every cycle. With two
 * cores that pool has one worker, so a login's hash held up the game cycle for about a second.
 */
internal object LoginFlow {
    private val threads = AtomicInteger()

    /**
     * As `ConnectionHandler` advises: `(cores - 1) * 2` threads, so hashing can't take every core.
     */
    private val executor: ExecutorService by lazy {
        val count = ((Runtime.getRuntime().availableProcessors() - 1) * 2).coerceAtLeast(1)
        Executors.newFixedThreadPool(count) { task ->
            Thread(task, "login-flow-${threads.incrementAndGet()}").apply { isDaemon = true }
        }
    }

    fun handlers(): LoginHandlers = LoginHandlers(loginFlowExecutor = executor)
}
