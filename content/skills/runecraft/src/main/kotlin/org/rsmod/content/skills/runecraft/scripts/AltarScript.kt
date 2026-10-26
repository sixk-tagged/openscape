package org.rsmod.content.skills.runecraft.scripts

import jakarta.inject.Inject
import org.rsmod.api.config.refs.stats
import org.rsmod.api.player.front
import org.rsmod.api.player.protect.ProtectedAccess
import org.rsmod.api.player.stat.runecraftingLvl
import org.rsmod.api.script.onOpLoc1
import org.rsmod.api.script.onOpLocU
import org.rsmod.api.stats.xpmod.XpModifiers
import org.rsmod.content.skills.runecraft.configs.Altar
import org.rsmod.content.skills.runecraft.configs.Altars
import org.rsmod.content.skills.runecraft.configs.CombinationRunes
import org.rsmod.content.skills.runecraft.configs.ComboRecipe
import org.rsmod.content.skills.runecraft.configs.RunecraftObjs
import org.rsmod.content.skills.runecraft.configs.RunecraftSeqs
import org.rsmod.content.skills.runecraft.configs.RunecraftSpotanims
import org.rsmod.game.type.obj.ObjType
import org.rsmod.plugin.scripts.PluginScript
import org.rsmod.plugin.scripts.ScriptContext

/**
 * Crafting at the altars: `Craft-rune` turns every essence carried into runes at once, with the
 * wiki's multiple-rune thresholds; tiaras are bound by using a tiara and its talisman on the altar;
 * combination runes by using the other element's runes (or talisman) on the altar.
 */
class AltarScript @Inject constructor(private val xpMods: XpModifiers) : PluginScript() {
    override fun ScriptContext.startup() {
        for (altar in Altars.all) {
            onOpLoc1(altar.loc) { craft(altar) }
            onOpLocU(altar.loc, RunecraftObjs.rune_essence) { craft(altar) }
            onOpLocU(altar.loc, RunecraftObjs.pure_essence) { craft(altar) }
            altar.core?.let { core -> onOpLocU(altar.loc, core) { craft(altar) } }
            val binding = altar.binding ?: continue
            onOpLocU(altar.loc, RunecraftObjs.tiara) { bind(altar, gold = false) }
            onOpLocU(altar.loc, binding.talisman) { bind(altar, gold = false) }
            onOpLocU(altar.loc, RunecraftObjs.tiara_gold) { bind(altar, gold = true) }
            onOpLocU(altar.loc, groupTalisman(altar)) { bind(altar, gold = true) }
        }
        for (recipe in CombinationRunes.all) {
            onOpLocU(recipe.altar.loc, recipe.otherRune) { combine(recipe) }
            onOpLocU(recipe.altar.loc, recipe.otherTalisman) { combine(recipe) }
        }
    }

    private fun ProtectedAccess.craft(altar: Altar) {
        if (altar.members && !player.members) {
            // UNVERIFIED: the wiki gives no wording for a free player at a members altar.
            mes("You need to be on a members' world to craft ${altar.name} runes.")
            return
        }
        val level = player.runecraftingLvl
        if (level < altar.level) {
            mes("You need a Runecrafting level of ${altar.level} to craft ${altar.name} runes.")
            return
        }
        val pure = invTotal(inv, RunecraftObjs.pure_essence)
        val rune = if (altar.pureOnly) 0 else invTotal(inv, RunecraftObjs.rune_essence)
        val cores = altar.core?.let { invTotal(inv, it) } ?: 0
        val essence = pure + rune
        if (essence + cores == 0) {
            // UNVERIFIED wording.
            mes(
                if (altar.pureOnly) {
                    "You need pure essence to craft ${altar.name} runes."
                } else {
                    "You need rune or pure essence to craft ${altar.name} runes."
                }
            )
            return
        }
        // The level before the action counts: a level-up mid-craft does not change the runes.
        val perEssence = Altars.runesPerEssence(altar, level)
        anim(RunecraftSeqs.craft)
        spotanim(RunecraftSpotanims.craft)
        if (pure > 0) invDel(inv, RunecraftObjs.pure_essence, pure)
        if (rune > 0) invDel(inv, RunecraftObjs.rune_essence, rune)
        if (cores > 0) invDel(inv, checkNotNull(altar.core), cores)
        invAdd(inv, altar.rune, (essence + cores * CORE_RUNES) * perEssence)
        addXp(altar.xp * (essence + cores * CORE_XP_FACTOR))
    }

    /** The gold tiara takes the elemental talisman at elemental altars, else the catalytic one. */
    private fun groupTalisman(altar: Altar): ObjType =
        if (checkNotNull(altar.binding).elemental) {
            RunecraftObjs.elemental_talisman
        } else {
            RunecraftObjs.catalytic_talisman
        }

    private fun ProtectedAccess.bind(altar: Altar, gold: Boolean) {
        val binding = checkNotNull(altar.binding)
        val tiara = if (gold) RunecraftObjs.tiara_gold else RunecraftObjs.tiara
        val talisman = if (gold) groupTalisman(altar) else binding.talisman
        val result =
            when {
                !gold -> binding.tiara
                binding.elemental -> RunecraftObjs.tiara_elemental
                else -> RunecraftObjs.tiara_catalytic
            }
        val xp =
            when {
                !gold -> binding.tiaraXp
                binding.elemental -> Altars.ELEMENTAL_TIARA_XP
                else -> Altars.CATALYTIC_TIARA_XP
            }
        if (tiara !in inv || talisman !in inv) {
            mes("Nothing interesting happens.")
            return
        }
        invDel(inv, tiara, 1, talisman, 1)
        invAdd(inv, result)
        // UNVERIFIED wording.
        mes("You bind the talisman's power into the tiara.")
        addXp(xp)
    }

    private fun ProtectedAccess.combine(recipe: ComboRecipe) {
        if (!player.members) {
            mes("You need to be on a members' world to craft combination runes.")
            return
        }
        val level = player.runecraftingLvl
        if (level < recipe.level) {
            mes(
                "You need a Runecrafting level of ${recipe.level} to craft " +
                    "${ocName(recipe.rune).lowercase()}s."
            )
            return
        }
        val essence = invTotal(inv, RunecraftObjs.pure_essence)
        val runes = invTotal(inv, recipe.otherRune)
        if (essence == 0 || runes == 0 || recipe.otherTalisman !in inv) {
            // UNVERIFIED wording.
            mes(
                "You need pure essence, ${recipe.other.name} runes and a ${recipe.other.name} " +
                    "talisman to craft ${ocName(recipe.rune).lowercase()}s here."
            )
            return
        }
        val count = minOf(essence, runes)
        // The necklace skips the 50% roll for a charge; the talisman is spent either way.
        val bound = RunecraftGear.wearsBindingNecklace(player)
        val made = if (bound) count else (1..count).count { random.randomBoolean() }
        anim(RunecraftSeqs.craft)
        spotanim(RunecraftSpotanims.craft)
        invDel(inv, RunecraftObjs.pure_essence, count)
        invDel(inv, recipe.otherRune, count)
        invDel(inv, recipe.otherTalisman)
        if (made > 0) {
            invAdd(inv, recipe.rune, made)
            addXp(recipe.xp * made)
        }
        if (bound) spendNecklaceCharge()
    }

    private fun ProtectedAccess.spendNecklaceCharge() {
        player.bindingNecklaceUsed++
        if (player.bindingNecklaceUsed >= CombinationRunes.NECKLACE_CHARGES) {
            player.front = null
            player.bindingNecklaceUsed = 0
            // UNVERIFIED wording.
            mes("Your binding necklace has crumbled to dust.")
        }
    }

    private fun ProtectedAccess.addXp(xp: Double) {
        statAdvance(stats.runecrafting, xp * xpMods.get(player, stats.runecrafting))
    }

    private companion object {
        const val CORE_RUNES = 11
        const val CORE_XP_FACTOR = 10
    }
}
