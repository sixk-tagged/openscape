package org.rsmod.content.skills.mining

import org.rsmod.api.stats.xpmod.XpMod
import org.rsmod.plugin.module.PluginModule

class MiningModule : PluginModule() {
    override fun bind() {
        addSetBinding<XpMod>(ProspectorXpMod::class.java)
    }
}
