package org.rsmod.content.other.consumables

import org.rsmod.api.config.refs.stats
import org.rsmod.api.player.stat.stat
import org.rsmod.api.player.stat.statBoost
import org.rsmod.api.player.stat.statHeal
import org.rsmod.api.player.stat.statSub
import org.rsmod.content.other.consumables.configs.ConsumableObjs
import org.rsmod.game.entity.Player
import org.rsmod.game.type.obj.ObjType
import org.rsmod.game.type.stat.StatTypeList

/**
 * [heal] gets the base Hitpoints level. [overheal] lets the heal go above max HP by the same
 * amount. [combo] food (karambwan) has its own delay, so it can follow normal food and a potion in
 * the same tick. [attackDelay] is added to the player's next attack.
 */
internal class Food(
    val obj: ObjType,
    val label: String,
    val overheal: Boolean = false,
    val combo: Boolean = false,
    val attackDelay: Int = 3,
    val heal: (baseHitpoints: Int) -> Int,
)

internal val FOODS =
    listOf(
        Food(ConsumableObjs.shrimps, "shrimps") { 3 },
        Food(ConsumableObjs.anchovies, "anchovies") { 1 },
        Food(ConsumableObjs.sardine, "sardine") { 4 },
        Food(ConsumableObjs.herring, "herring") { 5 },
        Food(ConsumableObjs.trout, "trout") { 7 },
        Food(ConsumableObjs.pike, "pike") { 8 },
        Food(ConsumableObjs.salmon, "salmon") { 9 },
        Food(ConsumableObjs.tuna, "tuna") { 10 },
        Food(ConsumableObjs.cooked_meat, "meat") { 3 },
        Food(ConsumableObjs.cooked_chicken, "chicken") { 3 },
        Food(ConsumableObjs.bread, "bread") { 5 },
        Food(ConsumableObjs.lobster, "lobster") { 12 },
        Food(ConsumableObjs.swordfish, "swordfish") { 14 },
        Food(ConsumableObjs.monkfish, "monkfish") { 16 },
        Food(ConsumableObjs.shark, "shark") { 20 },
        Food(ConsumableObjs.anglerfish, "anglerfish", overheal = true) { hp ->
            hp / 10 + 2 * (hp / 25) + 5 * (hp / 93) + 2
        },
        Food(ConsumableObjs.karambwan, "karambwan", combo = true, attackDelay = 2) { 18 },
    )

/** [doses] runs from 4 doses down to 1; drinking the last one leaves an empty vial. */
internal class Potion(
    val doses: List<ObjType>,
    val label: String,
    val effect: Player.(StatTypeList) -> Unit,
)

internal val POTIONS =
    listOf(
        Potion(
            listOf(
                ConsumableObjs.super_combat4,
                ConsumableObjs.super_combat3,
                ConsumableObjs.super_combat2,
                ConsumableObjs.super_combat1,
            ),
            "super combat potion",
        ) {
            statBoost(stats.attack, 5, 15)
            statBoost(stats.strength, 5, 15)
            statBoost(stats.defence, 5, 15)
        },
        Potion(
            listOf(
                ConsumableObjs.ranging4,
                ConsumableObjs.ranging3,
                ConsumableObjs.ranging2,
                ConsumableObjs.ranging1,
            ),
            "ranging potion",
        ) {
            statBoost(stats.ranged, 4, 10)
        },
        Potion(
            listOf(
                ConsumableObjs.magic4,
                ConsumableObjs.magic3,
                ConsumableObjs.magic2,
                ConsumableObjs.magic1,
            ),
            "magic potion",
        ) {
            statBoost(stats.magic, 4, 0)
        },
        Potion(
            listOf(
                ConsumableObjs.brew4,
                ConsumableObjs.brew3,
                ConsumableObjs.brew2,
                ConsumableObjs.brew1,
            ),
            "Saradomin brew",
        ) {
            statBoost(stats.hitpoints, 2, 15)
            statBoost(stats.defence, 2, 20)
            // The drain is 10% of the *current* level, so `statSub`'s base percent can't be used.
            for (stat in listOf(stats.attack, stats.strength, stats.ranged, stats.magic)) {
                statSub(stat, stat(stat) / 10 + 2, 0)
            }
        },
        Potion(
            listOf(
                ConsumableObjs.restore4,
                ConsumableObjs.restore3,
                ConsumableObjs.restore2,
                ConsumableObjs.restore1,
            ),
            "super restore",
        ) { statTypes ->
            for (stat in statTypes.values) {
                if (!stat.isType(stats.hitpoints)) {
                    statHeal(stat, 8, 25)
                }
            }
        },
        Potion(
            listOf(
                ConsumableObjs.prayer4,
                ConsumableObjs.prayer3,
                ConsumableObjs.prayer2,
                ConsumableObjs.prayer1,
            ),
            "prayer potion",
        ) {
            statHeal(stats.prayer, 7, 25)
        },
    )
