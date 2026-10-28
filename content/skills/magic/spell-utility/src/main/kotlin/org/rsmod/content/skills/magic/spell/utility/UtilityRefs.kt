package org.rsmod.content.skills.magic.spell.utility

import org.rsmod.api.type.builders.varn.VarnBuilder
import org.rsmod.api.type.builders.varp.VarpBuilder
import org.rsmod.api.type.refs.obj.ObjReferences
import org.rsmod.api.type.refs.queue.QueueReferences
import org.rsmod.api.type.refs.seq.SeqReferences
import org.rsmod.api.type.refs.spot.SpotanimReferences
import org.rsmod.api.type.refs.synth.SynthReferences
import org.rsmod.api.type.refs.varn.VarnReferences
import org.rsmod.api.type.refs.varp.VarpReferences

object UtilityObjs : ObjReferences() {
    val cosmic_rune = find("cosmicrune")
    val coal = find("coal")
    val iron_ore = find("iron_ore")
    val copper_ore = find("copper_ore")
    val tin_ore = find("tin_ore")
    val silver_ore = find("silver_ore")
    val gold_ore = find("gold_ore")
    val mithril_ore = find("mithril_ore")
    val adamantite_ore = find("adamantite_ore")
    val runite_ore = find("runite_ore")
    val lovakite_ore = find("lovakite_ore")
    val elemental_ore = find("elemental_workshop_ore")
    val bronze_bar = find("bronze_bar")
    val iron_bar = find("iron_bar")
    val steel_bar = find("steel_bar")
    val silver_bar = find("silver_bar")
    val gold_bar = find("gold_bar")
    val mithril_bar = find("mithril_bar")
    val adamantite_bar = find("adamantite_bar")
    val runite_bar = find("runite_bar")
    val lovakite_bar = find("lovakite_bar")
    val saradomin_cape = find("saradomin_cape")
    val guthix_cape = find("guthix_cape")
    val zamorak_cape = find("zamorak_cape")
    val imbued_saradomin_cape = find("ma2_saradomin_cape")
    val imbued_guthix_cape = find("ma2_guthix_cape")
    val imbued_zamorak_cape = find("ma2_zamorak_cape")
    val banana = find("banana")
    val peach = find("peach")
    val bones = find("bones")
    val bones_burnt = find("bones_burnt")
    val bat_bones = find("bat_bones")
    val big_bones = find("big_bones")
    val wolf_bones = find("wolf_bones")
    val jogre_bones = find("tbwt_jogre_bones")
    val monkey_bones = find("mm_normal_monkey_bones")
    val bleached_bones = find("shade_bleached_bones")
    val animals_bones_1 = find("magictraining_bones1")
    val animals_bones_2 = find("magictraining_bones2")
    val animals_bones_3 = find("magictraining_bones3")
    val animals_bones_4 = find("magictraining_bones4")
}

object UtilitySeqs : SeqReferences() {
    val teleport = find("human_castteleport")
    val teleport_end = find("human_castteleport_reverse")
    val low_alchemy = find("human_castlowlvlalchemy")
    val high_alchemy = find("human_casthighlvlalchemy")
    val superheat = find("human_castsuperheatitem")
    val bones_to_fruit = find("human_castbonestobananas")
    val enchant_1 = find("human_enchantamuletlvl1")
    val enchant_2 = find("human_enchantamuletlvl2")
    val enchant_3 = find("human_enchantamuletlvl3")
    val charge = find("human_casting")
}

object UtilitySpots : SpotanimReferences() {
    val teleport = find("teleport_casting")
    val low_alchemy = find("lowlvlalchemy_casting")
    val high_alchemy = find("highlvlalchemy_casting")
    val superheat = find("superheatitem_casting")
    val bones_to_fruit = find("bonestobananas_casting")
    val enchant_1 = find("enchant_amulet_lvl1")
    val enchant_2 = find("enchant_amulet_lvl2")
    val enchant_3 = find("enchant_amulet_lvl3")
    val enchant_4 = find("enchant_amulet2_lvl4")
    val enchant_5 = find("enchant_amulet2_lvl5")
    val enchant_6 = find("enchant_amulet2_lvl6")
    val failed_spell = find("failedspell_impact")
}

object UtilitySynths : SynthReferences() {
    val teleport = find("quick_teleport")
    val high_alchemy = find("high_alchemy")
    val low_alchemy = find("low_alchemy")
    val superheat = find("superheat_all")
    val superheat_fail = find("superheat_fail")
    val bones_to_fruit = find("bones_to_bananas_all")
}

object UtilityVarps : VarpReferences() {
    /** The map clock until which Charge boosts the god spells. */
    val charge_until = find("magic_charge_until")
}

object UtilityVarns : VarnReferences() {
    /** The map clock until which a bind holds the npc in place. */
    val bind_until = find("magic_bind_until")

    /** Binds cannot be chained: the npc is immune from the cast until a few ticks after it ends. */
    val bind_immune_until = find("magic_bind_immune_until")
}

object UtilityQueues : QueueReferences() {
    val charge_end = find("magic_charge_end")
}

internal object UtilityVarpBuilder : VarpBuilder() {
    init {
        build("magic_charge_until") { temporary = true }
    }
}

internal object UtilityVarnBuilder : VarnBuilder() {
    init {
        build("magic_bind_until")
        build("magic_bind_immune_until")
    }
}
