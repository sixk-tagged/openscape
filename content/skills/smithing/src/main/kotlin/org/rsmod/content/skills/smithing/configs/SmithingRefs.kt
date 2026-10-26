package org.rsmod.content.skills.smithing.configs

import org.rsmod.api.type.refs.comp.ComponentReferences
import org.rsmod.api.type.refs.content.ContentReferences
import org.rsmod.api.type.refs.enums.EnumReferences
import org.rsmod.api.type.refs.interf.InterfaceReferences
import org.rsmod.api.type.refs.loc.LocReferences
import org.rsmod.api.type.refs.obj.ObjReferences
import org.rsmod.api.type.refs.queue.QueueReferences
import org.rsmod.api.type.refs.seq.SeqReferences
import org.rsmod.api.type.refs.varbit.VarBitReferences
import org.rsmod.api.type.refs.varp.VarpReferences
import org.rsmod.game.type.obj.ObjType

object SmithingContent : ContentReferences() {
    val furnace = find("smithing_furnace")
    val anvil = find("smithing_anvil")
}

object SmithingLocs : LocReferences() {
    val furnace = find("furnace")
    val lumbridge_furnace = find("fai_falador_furnace")
    val anvil = find("anvil")
    /** Lumbridge's rusted anvil: bronze only. */
    val lumbridge_anvil = find("lumbridge_anvil")
}

object SmithingObjs : ObjReferences() {
    val copper_ore = find("copper_ore")
    val tin_ore = find("tin_ore")
    val blurite_ore = find("blurite_ore")
    val iron_ore = find("iron_ore")
    val silver_ore = find("silver_ore")
    val gold_ore = find("gold_ore")
    val lovakite_ore = find("lovakite_ore")
    val mithril_ore = find("mithril_ore")
    val adamantite_ore = find("adamantite_ore")
    val runite_ore = find("runite_ore")
    val coal = find("coal")

    val bronze_bar = find("bronze_bar")
    val blurite_bar = find("blurite_bar")
    val iron_bar = find("iron_bar")
    val silver_bar = find("silver_bar")
    val steel_bar = find("steel_bar")
    val gold_bar = find("gold_bar")
    val lovakite_bar = find("lovakite_bar")
    val mithril_bar = find("mithril_bar")
    val adamantite_bar = find("adamantite_bar")
    val runite_bar = find("runite_bar")

    val cannonball = find("mcannonball")
    val ammo_mould = find("ammo_mould")
    val hammer = find("hammer")
    val coal_bag = find("coal_bag")
    val ring_of_forging = find("ring_of_forging")
    val goldsmith_gauntlets = find("gauntlets_of_goldsmithing")
    val smiths_tunic = find("smithing_uniform_torso")
    val smiths_trousers = find("smithing_uniform_legs")
    val smiths_boots = find("smithing_uniform_boots")
    val smiths_gloves = find("smithing_uniform_gloves")
}

object SmithingSeqs : SeqReferences() {
    val furnace = find("human_furnace")
    val anvil = find("human_smithing")
}

object SmithingQueues : QueueReferences() {
    val smelt = find("smithing_smelt")
    val anvil = find("smithing_anvil")
}

object SmithingVarps : VarpReferences() {
    /** Anvil quantity: 1, 5, 10, 28 (All) or an X amount. Shared with the client's buttons. */
    val makexcrafting = find("makexcrafting")
    /** Tourist Trap progress: the anvil greys out dart tips below 16. */
    val desertrescue = find("desertrescue")
    /** Death Plateau progress: the anvil hides claws below 80. */
    val death_equiproom = find("death_equiproom")
    /** Custom: iron bars smelted on the currently worn ring of forging. */
    val ring_of_forging_charges = find("ring_of_forging_charges")
}

object SmithingVarBits : VarBitReferences() {
    /** Metal shown on the anvil: cache enum 1253, 1 bronze .. 6 rune (7 lovakite, unused). */
    val smithing_bar_type = find("smithing_bar_type")
}

object SmithingInterfaces : InterfaceReferences() {
    val smithing = find("smithing")
}

object SmithingComponents : ComponentReferences() {
    val make_1 = find("smithing:make_1")
    val make_5 = find("smithing:make_5")
    val make_10 = find("smithing:make_10")
    val make_x = find("smithing:make_x")
    val make_all = find("smithing:make_all")

    val dagger = find("smithing:dagger")
    val sword = find("smithing:sword")
    val scimitar = find("smithing:scimitar")
    val longsword = find("smithing:longsword")
    val twohanded = find("smithing:2h")
    val axe = find("smithing:axe")
    val mace = find("smithing:mace")
    val warhammer = find("smithing:warhammer")
    val battleaxe = find("smithing:battleaxe")
    val claws = find("smithing:claws")
    val chainbody = find("smithing:chainbody")
    val platelegs = find("smithing:platelegs")
    val plateskirt = find("smithing:plateskirt")
    val platebody = find("smithing:platebody")
    val nails = find("smithing:nails")
    val medhelm = find("smithing:medhelm")
    val fullhelm = find("smithing:fullhelm")
    val squareshield = find("smithing:squareshield")
    val kiteshield = find("smithing:kiteshield")
    val darttips = find("smithing:darttips")
    val arrowheads = find("smithing:arrowheads")
    val knives = find("smithing:knives")
    val bolts = find("smithing:bolts")
    val limbs = find("smithing:limbs")
    val other_1 = find("smithing:other_1")
    val other_2 = find("smithing:other_2")
    val other_3 = find("smithing:other_3")
}

object SmithingEnums : EnumReferences() {
    val product_level = find<ObjType, Int>("smithing_product_to_requirement")
    val product_bars = find<ObjType, Int>("smithing_product_to_bars_required")
    val product_quantity = find<ObjType, Int>("smithing_product_to_quantity")
}
