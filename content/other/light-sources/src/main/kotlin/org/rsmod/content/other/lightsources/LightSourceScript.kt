package org.rsmod.content.other.lightsources

import org.rsmod.api.config.refs.stats
import org.rsmod.api.player.protect.ProtectedAccess
import org.rsmod.api.player.stat.statBase
import org.rsmod.api.script.onOpHeld1
import org.rsmod.api.script.onOpHeld2
import org.rsmod.api.script.onOpHeld3
import org.rsmod.api.script.onOpHeld4
import org.rsmod.api.script.onOpHeldU
import org.rsmod.game.type.obj.ObjType
import org.rsmod.plugin.scripts.PluginScript
import org.rsmod.plugin.scripts.ScriptContext

/** Lighting light sources with a tinderbox, and their Extinguish op. */
class LightSourceScript : PluginScript() {
    override fun ScriptContext.startup() {
        for (source in LightSources.all) {
            val unlit = source.unlit ?: continue
            onOpHeldU(LightSourceObjs.tinderbox, unlit) { light(source, unlit) }
        }
        // Extinguish sits on a different op per item (cache iops).
        with(LightSourceObjs) {
            onOpHeld1(candle_lit) { extinguish(candle_lit, candle_unlit) }
            onOpHeld1(black_candle_lit) { extinguish(black_candle_lit, black_candle_unlit) }
            onOpHeld1(sapphire_lantern_lit) {
                extinguish(sapphire_lantern_lit, sapphire_lantern_unlit)
            }
            onOpHeld4(torch_lit) { extinguish(torch_lit, torch_unlit) }
            onOpHeld2(oil_lamp_lit) { extinguish(oil_lamp_lit, oil_lamp_unlit) }
            onOpHeld2(candle_lantern_lit) { extinguish(candle_lantern_lit, candle_lantern_unlit) }
            onOpHeld2(black_candle_lantern_lit) {
                extinguish(black_candle_lantern_lit, black_candle_lantern_unlit)
            }
            onOpHeld2(oil_lantern_lit) { extinguish(oil_lantern_lit, oil_lantern_unlit) }
            onOpHeld2(bullseye_lantern_lit) {
                extinguish(bullseye_lantern_lit, bullseye_lantern_unlit)
            }
            onOpHeld3(mining_helmet_lit) { extinguish(mining_helmet_lit, mining_helmet_unlit) }
        }
    }

    private fun ProtectedAccess.light(source: LightSource, unlit: ObjType) {
        if (player.statBase(stats.firemaking) < source.level) {
            // UNVERIFIED: the wording; the levels are the wiki's.
            mes("You need a Firemaking level of ${source.level} to light this.")
            return
        }
        invReplace(inv, unlit, 1, source.lit)
    }

    private fun ProtectedAccess.extinguish(lit: ObjType, unlit: ObjType) {
        invReplace(inv, lit, 1, unlit)
    }
}
