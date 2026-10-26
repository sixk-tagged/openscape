package org.rsmod.content.skills.mining.configs

import org.rsmod.api.config.refs.params
import org.rsmod.api.type.editors.loc.LocEditor
import org.rsmod.game.stat.PlayerStatMap
import org.rsmod.game.type.loc.LocType
import org.rsmod.game.type.obj.ObjType

private typealias locs = MiningLocs

private typealias ores = MiningObjs

internal object MiningRocks : LocEditor() {
    /** `deplete_chance` value for rocks that never run out. */
    const val NEVER_DEPLETES = 255

    init {
        // Tier 1: levels 1-15.
        rocks(
            locs.clay_1 to locs.empty_1,
            locs.clay_2 to locs.empty_2,
            level = 1,
            xp = 5.0,
            product = ores.clay,
            respawn = 2,
            low = 128,
            high = 400,
        )
        rocks(
            locs.copper_1 to locs.empty_1,
            locs.copper_2 to locs.empty_2,
            level = 1,
            xp = 17.5,
            product = ores.copper_ore,
            respawn = 4,
            low = 100,
            high = 350,
        )
        rocks(
            locs.tin_1 to locs.empty_1,
            locs.tin_2 to locs.empty_2,
            level = 1,
            xp = 17.5,
            product = ores.tin_ore,
            respawn = 4,
            low = 100,
            high = 350,
        )
        rocks(
            locs.essence to locs.empty_1,
            locs.essence_mine to locs.empty_1,
            level = 1,
            xp = 5.0,
            product = ores.rune_essence,
            respawn = 0,
            low = 255,
            high = 255,
            deplete = NEVER_DEPLETES,
            roll = ProductRoll.ESSENCE,
            bonus = false,
        )
        rocks(
            locs.blurite_1 to locs.empty_1,
            locs.blurite_2 to locs.empty_2,
            level = 10,
            xp = 17.5,
            product = ores.blurite_ore,
            respawn = 42,
            low = 90,
            high = 350,
        )
        rocks(
            locs.limestone_1 to locs.limestone_empty,
            locs.limestone_2 to locs.limestone_empty,
            level = 10,
            xp = 26.5,
            product = ores.limestone,
            respawn = 9,
            low = 95,
            high = 350,
            deplete = 170,
        )
        rocks(
            locs.barronite_1 to locs.barronite_empty_1,
            locs.barronite_2 to locs.barronite_empty_2,
            level = 14,
            xp = 16.0,
            product = ores.barronite_shard,
            respawn = 100,
            low = 80,
            high = 100,
        )
        rocks(
            locs.iron_1 to locs.empty_1,
            locs.iron_2 to locs.empty_2,
            level = 15,
            xp = 35.0,
            product = ores.iron_ore,
            respawn = 9,
            low = 96,
            high = 350,
        )

        // Tier 2: levels 20-45.
        rocks(
            locs.silver_1 to locs.empty_1,
            locs.silver_2 to locs.empty_2,
            level = 20,
            xp = 40.0,
            product = ores.silver_ore,
            respawn = 100,
            low = 25,
            high = 200,
        )
        // Note: estimated success rate (wiki has none); tune if ash feels off.
        rocks(
            locs.ash to locs.ash_empty,
            level = 22,
            xp = 10.0,
            product = ores.volcanic_ash,
            respawn = 50,
            low = 100,
            high = 300,
            deplete = 191,
        )
        rocks(
            locs.coal_1 to locs.empty_1,
            locs.coal_2 to locs.empty_2,
            level = 30,
            xp = 50.0,
            product = ores.coal,
            respawn = 50,
            low = 16,
            high = 100,
        )
        rocks(
            locs.sandstone to locs.empty_1,
            level = 35,
            xp = 30.0,
            product = ores.sandstone_1kg,
            respawn = 9,
            low = 25,
            high = 200,
            roll = ProductRoll.SANDSTONE,
            bonus = false,
        )
        rocks(
            locs.gold_1 to locs.empty_1,
            locs.gold_2 to locs.empty_2,
            level = 40,
            xp = 65.0,
            product = ores.gold_ore,
            respawn = 100,
            low = 7,
            high = 75,
        )
        rocks(
            locs.gem_1 to locs.empty_1,
            locs.gem_2 to locs.empty_2,
            level = 40,
            xp = 65.0,
            product = ores.uncut_opal,
            respawn = 99,
            low = 28,
            high = 70,
            roll = ProductRoll.GEM_ROCK,
            bonus = false,
        )
        // Note: estimated success rate (wiki has none).
        rocks(
            locs.calcified_1 to locs.calcified_empty_1,
            locs.calcified_2 to locs.calcified_empty_2,
            level = 41,
            xp = 33.0,
            product = ores.blessed_bone_shard,
            respawn = 50,
            low = 60,
            high = 190,
        )
        rocks(
            locs.sulphur_1 to locs.sulphur_1,
            locs.sulphur_2 to locs.sulphur_2,
            level = 42,
            xp = 35.0,
            product = ores.volcanic_sulphur,
            respawn = 42,
            low = 100,
            high = 220,
            deplete = NEVER_DEPLETES,
            bonus = false,
        )
        rocks(
            locs.granite to locs.empty_1,
            level = 45,
            xp = 50.0,
            product = ores.granite_500g,
            respawn = 9,
            low = 16,
            high = 100,
            roll = ProductRoll.GRANITE,
            bonus = false,
        )

        // Tier 3: levels 55-78.
        rocks(
            locs.mithril_1 to locs.empty_1,
            locs.mithril_2 to locs.empty_2,
            level = 55,
            xp = 80.0,
            product = ores.mithril_ore,
            respawn = 200,
            low = 4,
            high = 50,
        )
        // Note: estimated success rate (wiki has none).
        rocks(
            locs.lovakite_1 to locs.empty_1,
            locs.lovakite_2 to locs.empty_2,
            level = 65,
            xp = 60.0,
            product = ores.lovakite_ore,
            respawn = 58,
            low = 3,
            high = 40,
        )
        rocks(
            locs.adamantite_1 to locs.empty_1,
            locs.adamantite_2 to locs.empty_2,
            level = 70,
            xp = 95.0,
            product = ores.adamantite_ore,
            respawn = 400,
            low = 2,
            high = 25,
        )
        rocks(
            locs.soft_clay_1 to locs.empty_1,
            locs.soft_clay_2 to locs.empty_2,
            level = 70,
            xp = 5.0,
            product = ores.soft_clay,
            respawn = 2,
            low = 255,
            high = 255,
        )
        // Note: basalt/salt/ancient essence/infernal shale success rates are estimates.
        rocks(
            locs.basalt to locs.salt_empty,
            level = 72,
            xp = 5.0,
            product = ores.basalt,
            respawn = 9,
            low = 128,
            high = 400,
            deplete = 219,
            bonus = false,
        )
        rocks(
            locs.te_salt to locs.salt_empty,
            level = 72,
            xp = 5.0,
            product = ores.te_salt,
            respawn = 9,
            low = 128,
            high = 400,
            deplete = 219,
            bonus = false,
        )
        rocks(
            locs.efh_salt to locs.salt_empty,
            level = 72,
            xp = 5.0,
            product = ores.efh_salt,
            respawn = 9,
            low = 128,
            high = 400,
            deplete = 219,
            bonus = false,
        )
        rocks(
            locs.urt_salt to locs.salt_empty,
            level = 72,
            xp = 5.0,
            product = ores.urt_salt,
            respawn = 9,
            low = 128,
            high = 400,
            deplete = 219,
            bonus = false,
        )
        rocks(
            locs.ancient_essence to locs.ancient_essence,
            level = 75,
            xp = 13.5,
            product = ores.ancient_essence,
            respawn = 0,
            low = 50,
            high = 150,
            deplete = NEVER_DEPLETES,
            bonus = false,
        )
        // Note: infernal shale gives 13.2-100 xp in OSRS; a fixed 40 until it matters.
        rocks(
            locs.infernal_shale_1 to locs.infernal_shale_1,
            locs.infernal_shale_2 to locs.infernal_shale_2,
            level = 78,
            xp = 40.0,
            product = ores.infernal_shale,
            respawn = 9,
            low = 40,
            high = 130,
            deplete = NEVER_DEPLETES,
        )

        // Tier 4: levels 85-92.
        rocks(
            locs.runite_1 to locs.empty_1,
            locs.runite_2 to locs.empty_2,
            level = 85,
            xp = 125.0,
            product = ores.runite_ore,
            respawn = 1200,
            low = 1,
            high = 18,
        )
        // OSRS amethyst is -18/10; packed rates must be non-negative, and 0/10 is within 0.4%.
        rocks(
            locs.amethyst_1 to locs.amethyst_empty,
            locs.amethyst_2 to locs.amethyst_empty,
            level = 92,
            xp = 240.0,
            product = ores.amethyst,
            respawn = 125,
            low = 0,
            high = 10,
            deplete = 153,
        )

        depleted(
            locs.empty_1,
            locs.empty_2,
            locs.limestone_empty,
            locs.barronite_empty_1,
            locs.barronite_empty_2,
            locs.ash_empty,
            locs.calcified_empty_1,
            locs.calcified_empty_2,
            locs.salt_empty,
            locs.amethyst_empty,
        )
    }

    private fun rocks(
        vararg rocks: Pair<LocType, LocType>,
        level: Int,
        xp: Double,
        product: ObjType,
        respawn: Int,
        low: Int,
        high: Int,
        deplete: Int = 0,
        roll: Int = ProductRoll.FIXED,
        bonus: Boolean = true,
    ) {
        for ((rock, empty) in rocks) {
            edit(rock) {
                contentGroup = MiningContent.rock
                param[params.levelrequire] = level
                param[params.skill_xp] = PlayerStatMap.toFineXP(xp).toInt()
                param[params.skill_productitem] = product
                param[params.next_loc_stage] = empty
                param[params.respawn_time] = respawn
                param[params.deplete_chance] = deplete
                param[MiningParams.success_rate] = (low shl 16) or high
                param[MiningParams.product_roll] = roll
                param[MiningParams.bonus_rolls] = bonus
            }
        }
    }

    private fun depleted(vararg types: LocType) {
        for (type in types) {
            edit(type) { contentGroup = MiningContent.depleted_rock }
        }
    }
}
