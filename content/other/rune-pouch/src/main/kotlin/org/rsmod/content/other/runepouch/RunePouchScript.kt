package org.rsmod.content.other.runepouch

import jakarta.inject.Inject
import org.rsmod.api.config.refs.objs
import org.rsmod.api.script.onOpHeld1
import org.rsmod.api.script.onOpHeld4
import org.rsmod.api.script.onOpHeldU
import org.rsmod.api.type.refs.obj.ObjReferences
import org.rsmod.plugin.scripts.PluginScript
import org.rsmod.plugin.scripts.ScriptContext

/** The rune pouch's options: use a rune on it, "Open" (lists it) and "Empty". */
class RunePouchScript @Inject constructor(private val pouch: RunePouch) : PluginScript() {
    override fun ScriptContext.startup() {
        for (bag in listOf(objs.rune_pouch, objs.divine_rune_pouch)) {
            onOpHeld1(bag) { mes(pouch.describe(player)) }
            onOpHeld4(bag) {
                if (pouch.contents(player).isEmpty()) {
                    mes("Your rune pouch is empty.")
                } else if (!pouch.empty(player)) {
                    mes("You don't have enough inventory space to empty the rune pouch.")
                }
            }
            for (rune in PouchRunes.all) {
                onOpHeldU(rune, bag) {
                    if (pouch.store(player, it.firstSlot) == 0) {
                        mes("Your rune pouch has no room for that.")
                    }
                }
            }
        }
    }
}

/** Every rune a pouch accepts: the elemental, catalytic and combination runes. */
internal object PouchRunes : ObjReferences() {
    val all =
        listOf(
            find("airrune"),
            find("waterrune"),
            find("earthrune"),
            find("firerune"),
            find("mindrune"),
            find("bodyrune"),
            find("cosmicrune"),
            find("chaosrune"),
            find("naturerune"),
            find("lawrune"),
            find("deathrune"),
            find("bloodrune"),
            find("soulrune"),
            find("astralrune"),
            find("wrathrune"),
            find("sunfirerune"),
            find("aetherrune"),
            find("mistrune"),
            find("dustrune"),
            find("mudrune"),
            find("smokerune"),
            find("steamrune"),
            find("lavarune"),
        )
}
