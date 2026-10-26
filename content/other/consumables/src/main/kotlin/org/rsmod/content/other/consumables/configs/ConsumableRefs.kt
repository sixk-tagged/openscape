package org.rsmod.content.other.consumables.configs

import org.rsmod.api.type.builders.varp.VarpBuilder
import org.rsmod.api.type.refs.obj.ObjReferences
import org.rsmod.api.type.refs.seq.SeqReferences
import org.rsmod.api.type.refs.varp.VarpReferences

object ConsumableObjs : ObjReferences() {
    val shrimps = find("shrimp")
    val anchovies = find("anchovies")
    val sardine = find("sardine")
    val herring = find("herring")
    val trout = find("trout")
    val pike = find("pike")
    val salmon = find("salmon")
    val tuna = find("tuna")
    val cooked_meat = find("cooked_meat")
    val cooked_chicken = find("cooked_chicken")
    val bread = find("bread")
    val lobster = find("lobster")
    val swordfish = find("swordfish")
    val monkfish = find("monkfish")
    val shark = find("shark")
    val anglerfish = find("anglerfish")
    val karambwan = find("tbwt_cooked_karambwan")

    val super_combat4 = find("4dose2combat")
    val super_combat3 = find("3dose2combat")
    val super_combat2 = find("2dose2combat")
    val super_combat1 = find("1dose2combat")
    val ranging4 = find("4doserangerspotion")
    val ranging3 = find("3doserangerspotion")
    val ranging2 = find("2doserangerspotion")
    val ranging1 = find("1doserangerspotion")
    val magic4 = find("4dose1magic")
    val magic3 = find("3dose1magic")
    val magic2 = find("2dose1magic")
    val magic1 = find("1dose1magic")
    val brew4 = find("4dosepotionofsaradomin")
    val brew3 = find("3dosepotionofsaradomin")
    val brew2 = find("2dosepotionofsaradomin")
    val brew1 = find("1dosepotionofsaradomin")
    val restore4 = find("4dose2restore")
    val restore3 = find("3dose2restore")
    val restore2 = find("2dose2restore")
    val restore1 = find("1dose2restore")
    val prayer4 = find("4doseprayerrestore")
    val prayer3 = find("3doseprayerrestore")
    val prayer2 = find("2doseprayerrestore")
    val prayer1 = find("1doseprayerrestore")
    val vial = find("vial_empty")
}

object ConsumableSeqs : SeqReferences() {
    /** OSRS plays the same animation for eating and drinking. */
    val eat = find("human_eat")
}

/** Absolute map clock cycles at which the player may next eat, drink or combo-eat. */
object ConsumableVarps : VarpReferences() {
    val food_delay = find("consume_food_delay")
    val potion_delay = find("consume_potion_delay")
    val combo_delay = find("consume_combo_delay")
}

internal object ConsumableVarpBuilder : VarpBuilder() {
    init {
        build("consume_food_delay") { temporary = true }
        build("consume_potion_delay") { temporary = true }
        build("consume_combo_delay") { temporary = true }
    }
}
