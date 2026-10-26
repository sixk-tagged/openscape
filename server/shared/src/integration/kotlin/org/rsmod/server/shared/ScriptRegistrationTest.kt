package org.rsmod.server.shared

import com.google.inject.Injector
import io.github.classgraph.ClassGraph
import jakarta.inject.Inject
import org.junit.jupiter.api.Test
import org.rsmod.api.testing.GameTestState
import org.rsmod.plugin.scripts.PluginScript
import org.rsmod.plugin.scripts.ScriptContext

/**
 * Starts every plugin script the way the server does at boot. Two scripts handling the same event
 * (one op on one npc or loc type, say) make the boot throw "Event with id already registered", and
 * each module's own tests only load that module's scripts. Scripts needing bindings that only the
 * server installs (network, combat maps, prayers) are skipped.
 */
class ScriptRegistrationTest {
    class Deps @Inject constructor(val injector: Injector, val context: ScriptContext)

    @Test
    fun GameTestState.`no two scripts register the same event`() =
        runInjectedGameTest(Deps::class) { deps ->
            val duplicates = mutableListOf<String>()
            var started = 0
            for (clazz in pluginScripts()) {
                val script =
                    runCatching { deps.injector.getInstance(clazz) }.getOrNull() ?: continue
                try {
                    with(script) { deps.context.startup() }
                    started++
                } catch (e: Exception) {
                    // Only duplicates count; other failures come from the skipped bindings.
                    if ("already registered" in e.message.orEmpty()) {
                        duplicates += "${clazz.name}: ${e.message}"
                    }
                }
            }
            assertEquals(emptyList<String>(), duplicates)
            assertTrue(started > 0)
        }

    private fun pluginScripts(): List<Class<PluginScript>> =
        ClassGraph().enableClassInfo().acceptPackages("org.rsmod").scan().use { result ->
            result
                .getSubclasses(PluginScript::class.java)
                .filter { !it.isAbstract }
                .map { it.loadClass(PluginScript::class.java) }
        }
}
