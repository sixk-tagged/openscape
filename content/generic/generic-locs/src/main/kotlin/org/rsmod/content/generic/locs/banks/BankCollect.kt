package org.rsmod.content.generic.locs.banks

import org.rsmod.api.config.refs.content
import org.rsmod.api.script.onOpLoc3
import org.rsmod.plugin.scripts.PluginScript
import org.rsmod.plugin.scripts.ScriptContext

class BankCollect : PluginScript() {
    override fun ScriptContext.startup() {
        // Note: there is no Grand Exchange, so Collect only shows a made-up message.
        onOpLoc3(content.bank_booth) { mes("You have nothing to collect.") }
    }
}
