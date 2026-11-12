package org.rsmod.content.skills.fishing

import org.rsmod.api.stats.levelmod.InvisibleLevelMod
import org.rsmod.plugin.module.PluginModule

class FishingModule : PluginModule() {
    override fun bind() {
        addSetBinding<InvisibleLevelMod>(FishingGuildBoost::class.java)
    }
}
