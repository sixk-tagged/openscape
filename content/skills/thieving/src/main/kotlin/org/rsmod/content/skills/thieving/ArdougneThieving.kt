package org.rsmod.content.skills.thieving

import org.rsmod.api.config.refs.objs
import org.rsmod.api.random.GameRandom
import org.rsmod.api.type.builders.varp.VarpBuilder
import org.rsmod.api.type.refs.loc.LocReferences
import org.rsmod.api.type.refs.npc.NpcReferences
import org.rsmod.api.type.refs.obj.ObjReferences
import org.rsmod.api.type.refs.varp.VarpReferences
import org.rsmod.content.skills.thieving.ThievingStalls.Stall
import org.rsmod.game.type.obj.ObjType

/** The East Ardougne market stalls (OSRS wiki); the stall owners shout and the guards attack. */
internal object ArdougneStalls {
    val all =
        with(ArdougneStallObjs) {
            // Who comes to a thief's aid grows with the stall's value (OSRS wiki).
            val guards = listOf(ArdougneNpcs.ardougne_guard)
            val withKnights = guards + ArdougneNpcs.knights
            val withPaladins = withKnights + ArdougneNpcs.paladins
            val withHeroes = withPaladins + ArdougneNpcs.heroes
            listOf(
                Stall(
                    ArdougneStallLocs.baker,
                    5,
                    16.0,
                    4,
                    loot(cake to 13, bread to 5, chocolate_slice to 2),
                    owners = ArdougneNpcs.bakers,
                    grudge = ArdougneVarps.baker_angry_until,
                    grudgeTicks = 1500,
                    guards = guards,
                    guardsStop = true,
                ),
                Stall(
                    ArdougneStallLocs.silk,
                    20,
                    24.0,
                    8,
                    loot(silk to 1),
                    owners = listOf(ArdougneNpcs.silk_merchant),
                    guards = withKnights,
                    guardsStop = true,
                ),
                Stall(
                    ArdougneStallLocs.fur,
                    35,
                    45.0,
                    12,
                    loot(grey_wolf_fur to 1),
                    owners = listOf(ArdougneNpcs.fur_trader),
                    guards = withKnights,
                    guardsStop = true,
                ),
                Stall(
                    ArdougneStallLocs.silver,
                    50,
                    205.0,
                    32,
                    loot(silver_ore to 16, silver_bar to 3, tiara to 1),
                    owners = listOf(ArdougneNpcs.silver_merchant),
                    guards = withPaladins,
                    guardsStop = true,
                ),
                Stall(
                    ArdougneStallLocs.spice,
                    65,
                    92.0,
                    10,
                    loot(spice to 1),
                    owners = listOf(ArdougneNpcs.spice_seller),
                    grudge = ArdougneVarps.spice_angry_until,
                    grudgeTicks = 600,
                    guards = withPaladins,
                    guardsStop = true,
                ),
                Stall(
                    ArdougneStallLocs.gem,
                    75,
                    408.0,
                    100,
                    loot(
                        uncut_sapphire to 105,
                        uncut_emerald to 17,
                        uncut_ruby to 5,
                        uncut_diamond to 1,
                    ),
                    owners = listOf(ArdougneNpcs.gem_merchant),
                    guards = withHeroes,
                    guardsStop = true,
                ),
            )
        }
}

/** The wiki's 105/128 coin pouch (200-300 coins); the rest is one table. */
private fun heroLoot(random: GameRandom): Pair<ObjType, Int> =
    with(ArdougneStallObjs) {
        when (random.of(128)) {
            in 0 until 105 -> objs.coins to random.of(200..300)
            in 105 until 113 -> death_rune to 2
            in 113 until 119 -> jug_of_wine to 1
            in 119 until 124 -> blood_rune to 1
            in 124 until 126 -> fire_orb to 1
            126 -> diamond to 1
            else -> gold_ore to 1
        }
    }

/** The wiki's gnome pocket table, out of 128. */
private val gnomeLoot =
    with(ArdougneStallObjs) {
        Loot(
            listOf(
                Loot.Entry(arrow_shaft, 56, 2..4),
                Loot.Entry(objs.coins, 30, 300..300),
                Loot.Entry(swamp_toad, 24),
                Loot.Entry(gold_ore, 8),
                Loot.Entry(earth_rune, 5),
                Loot.Entry(king_worm, 3),
                Loot.Entry(fire_orb, 2),
            )
        )
    }

/**
 * Knights of Ardougne, paladins and heroes (OSRS wiki); 3 damage, the knight stuns for 10 ticks.
 */
internal object ArdougnePickpocketTargets {
    val all =
        with(ArdougneNpcs) {
            listOf(
                PickpocketTargets.Target("Knight of Ardougne", knights, 55, 84.3, 50, 240, 10, 3) {
                    _,
                    _ ->
                    objs.coins to 50
                },
                PickpocketTargets.Target(
                    "paladin",
                    paladins,
                    70,
                    131.8,
                    40,
                    170,
                    8,
                    3,
                    extra = ArdougneStallObjs.chaos_rune to 2,
                ) { _, _ ->
                    objs.coins to 80
                },
                PickpocketTargets.Target("hero", heroes, 80, 163.3, 39, 160, 10, 3) { random, _ ->
                    heroLoot(random)
                },
                // The wiki's medium clue scroll (1/150) is left out.
                PickpocketTargets.Target("gnome", gnomes, 75, 133.5, 43, 175, 8) { random, _ ->
                    gnomeLoot.roll(random)
                },
            )
        }
}

object ArdougneNpcs : NpcReferences() {
    val bakers = listOf(find("baker_merchant_ardougne"), find("baker_merchant_ardougne2"))
    val silk_merchant = find("silk_merchant_ardougne")
    val fur_trader = find("fur_merchant_ardougne")
    val silver_merchant = find("silver_merchant_ardougne")
    val spice_seller = find("spice_merchant_ardougne")
    val gem_merchant = find("gem_merchant_ardougne")

    // The West Ardougne knights and paladins only show once Sins of the Father has begun.
    val ardougne_guard = find("ardougne_guard")
    val knights = listOf(find("knight_of_ardougne"))
    val paladins = listOf(find("paladin"), find("paladin2"))
    val heroes = listOf(find("hero"))
    val gnomes = listOf(find("gnome"), find("browclothedgnome"), find("darkskinned_gnome"))
}

object ArdougneStallLocs : LocReferences() {
    val baker = find("cakethiefstall")
    val silk = find("silkthiefstall")
    val fur = find("furthiefstall")
    val silver = find("silverthiefstall")
    val spice = find("spicethiefstall")
    val gem = find("gemthiefstall")
}

object ArdougneStallObjs : ObjReferences() {
    val cake = find("cake")
    val bread = find("bread")
    val chocolate_slice = find("chocolate_slice")
    val silk = find("silk")
    val grey_wolf_fur = find("grey_wolf_fur")
    val silver_ore = find("silver_ore")
    val silver_bar = find("silver_bar")
    val tiara = find("tiara")
    val spice = find("spicespot")
    val uncut_sapphire = find("uncut_sapphire")
    val uncut_emerald = find("uncut_emerald")
    val uncut_ruby = find("uncut_ruby")
    val uncut_diamond = find("uncut_diamond")
    val diamond = find("diamond")
    val chaos_rune = find("chaosrune")
    val death_rune = find("deathrune")
    val blood_rune = find("bloodrune")
    val jug_of_wine = find("jug_wine")
    val fire_orb = find("fire_orb")
    val gold_ore = find("gold_ore")
    val arrow_shaft = find("arrow_shaft")
    val swamp_toad = find("swamp_toad")
    val earth_rune = find("earthrune")
    val king_worm = find("king_worm")
}

object ArdougneVarps : VarpReferences() {
    /** Custom: the playtime until which the bakers refuse to trade with a stall thief. */
    val baker_angry_until = find("ardougne_baker_angry_until")
    /** Custom: the same for the spice seller. */
    val spice_angry_until = find("ardougne_spice_angry_until")
}

internal object ArdougneVarpBuilder : VarpBuilder() {
    init {
        build("ardougne_baker_angry_until")
        build("ardougne_spice_angry_until")
    }
}
