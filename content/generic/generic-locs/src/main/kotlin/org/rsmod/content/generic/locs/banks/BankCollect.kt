package org.rsmod.content.generic.locs.banks

import org.rsmod.api.config.refs.content
import org.rsmod.api.config.refs.interfaces
import org.rsmod.api.script.onOpLoc3
import org.rsmod.plugin.scripts.PluginScript
import org.rsmod.plugin.scripts.ScriptContext

/** "Collect" on a bank booth opens the Grand Exchange collection box. */
class BankCollect : PluginScript() {
    override fun ScriptContext.startup() {
        onOpLoc3(content.bank_booth) { ifOpenMainModal(interfaces.ge_collection_box) }
    }
}
