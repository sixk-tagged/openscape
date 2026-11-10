package org.rsmod.content.generic.npcs.ranges

import org.rsmod.api.type.editors.npc.NpcEditor
import org.rsmod.api.type.refs.npc.NpcReferences
import org.rsmod.game.type.npc.NpcType

/**
 * Wander and chase ranges (`wanderrange` and `maxrange`) from LostCity's 2004 npc configs, for
 * every spawned npc that has them there. Where LostCity sets no chase range it uses the wander
 * range plus 2. Areas that set the same npc's range set the same value.
 */
internal object NpcRangeEditor : NpcEditor() {
    init {
        for (range in NpcRanges.ranges) {
            edit(range.npc) {
                wanderRange = range.wander
                maxRange = range.max
            }
        }
    }
}

internal object NpcRanges : NpcReferences() {
    val ranges =
        listOf(
            range("abbot_langley", 3, 5),
            range("aggressive_black_knight", 10, 12),
            range("aprilfoolshorsesalesman", 3, 5),
            range("artist1", 3, 5),
            range("artist2", 3, 5),
            range("banker1", 0, 2),
            range("banker2", 0, 2),
            range("baraek", 2, 4),
            range("bat", 8, 10),
            range("bearded_dark_wizard", 3, 5),
            range("bioguard1", 0, 2),
            range("black_knight", 10, 12),
            range("black_unicorn", 17, 19),
            range("bluemoon_bartender", 2, 4),
            range("borderguard1", 0, 2),
            range("borderguard2", 0, 2),
            range("brother_jered", 3, 5),
            range("brownbear", 13, 15),
            range("butterfly", 10, 12),
            range("butterfly_bluemorpo", 10, 12),
            range("butterfly_viceroy", 10, 12),
            range("chaosmonk2", 12, 14),
            range("customs_officer", 4, 6),
            range("darkbear", 8, 10),
            range("deadly_red_spider", 10, 12),
            range("donkey_bartender", 2, 4),
            range("drogo", 13, 15),
            range("drunk1", 3, 5),
            range("drunk2", 3, 5),
            range("dwarf_chaos", 20, 22),
            range("dwarf_normal", 13, 15),
            range("dwarven_shopkeeper", 13, 15),
            range("earthwarrior", 4, 6),
            range("farmer1", 20, 22),
            range("fat_tony", 3, 5),
            range("firegiant", 3, 5),
            range("gambler1", 3, 5),
            range("gambler2", 3, 5),
            range("gem_trader", 2, 4),
            range("ghost", 8, 10),
            range("giant", 3, 5),
            range("giantrat", 6, 8),
            range("giantrat1", 6, 8),
            range("giantskeleton", 9, 11),
            range("giantspider1", 10, 12),
            range("giantspider2", 10, 12),
            range("goblin_armed", 15, 17),
            range("green_dragon", 4, 6),
            range("head_chef", 3, 5),
            range("hellhound", 3, 5),
            range("highwayman", 12, 14),
            range("hobgoblin_armed", 3, 5),
            range("hobgoblin_unarmed", 15, 17),
            range("ice_spider", 10, 12),
            range("imp", 27, 27),
            range("jailguard", 11, 12),
            range("jollyboar_bartender", 2, 4),
            range("jonny_the_beard", 4, 6),
            range("kebab_seller", 2, 4),
            range("kharidbanker1", 0, 2),
            range("kharidbanker2", 0, 2),
            range("kingscorpion", 8, 10),
            range("kittens_mew", 0, 2),
            range("lesser_demon", 6, 16),
            range("magicaxe", 10, 12),
            range("monk", 3, 5),
            range("monkey", 11, 13),
            range("mossgiant", 3, 5),
            range("mugger", 12, 14),
            range("nasty_tree", 0, 2),
            range("noterazzo", 3, 5),
            range("nurmof", 13, 15),
            range("oziach", 3, 5),
            range("poisonspider", 10, 12),
            range("professor_oddenstein", 2, 4),
            range("rat", 15, 17),
            range("risingsun_barmaid", 4, 6),
            range("scorpion", 12, 14),
            range("shadow_spider", 10, 12),
            range("shantay", 3, 5),
            range("shantay_guard", 3, 5),
            range("shantay_guard_still", 0, 2),
            range("shilop", 2, 4),
            range("silk_trader", 2, 4),
            range("sir_vyvin", 8, 10),
            range("skeleton_armed", 9, 11),
            range("skeleton_unagressive", 8, 10),
            range("skeleton_unarmed", 3, 5),
            range("small_bat", 20, 32),
            range("spider", 8, 10),
            range("squire", 4, 6),
            range("tea_seller", 2, 4),
            range("thief1", 8, 10),
            range("tramp", 20, 22),
            range("tramppg", 2, 4),
            range("unicorn", 17, 19),
            range("veronica", 2, 4),
            range("whitewolf", 3, 5),
            range("wilough", 2, 4),
            range("wolf", 6, 8),
            range("wormbrain", 2, 4),
            range("young_dark_wizard", 3, 5),
            range("zombie2", 9, 11),
        )

    private fun range(npc: String, wander: Int, max: Int) = NpcRange(find(npc), wander, max)
}

internal data class NpcRange(val npc: NpcType, val wander: Int, val max: Int)
