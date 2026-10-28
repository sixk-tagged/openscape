package org.rsmod.content.skills.herblore

import org.rsmod.api.type.refs.obj.ObjReferences
import org.rsmod.api.type.refs.seq.SeqReferences
import org.rsmod.game.type.obj.ObjType

/**
 * The potions Herblore makes, each listed from 4 doses down to 1 (so `attack[1]` is the 3-dose
 * potion a recipe gives). Shared with Consumables for the drink effects and used here for
 * decanting.
 */
object Potions : ObjReferences() {
    private fun doses(stem: String): List<ObjType> = (4 downTo 1).map { find("$it$stem") }

    private fun suffixed(prefix: String, suffix: String = ""): List<ObjType> =
        (4 downTo 1).map { find("$prefix$it$suffix") }

    val attack = doses("dose1attack")
    val antipoison = doses("doseantipoison")
    val superAntipoison = doses("dose2antipoison")
    val strength = listOf(find("strength4")) + (3 downTo 1).map { find("${it}dose1strength") }
    val defence = doses("dose1defense")
    val restore = doses("dosestatrestore")
    val energy = doses("dose1energy")
    val agility = doses("dose1agility")
    val combat = doses("dosecombat")
    val prayer = doses("doseprayerrestore")
    val superAttack = doses("dose2attack")
    val superStrength = doses("dose2strength")
    val superDefence = doses("dose2defense")
    val superCombat = doses("dose2combat")
    val superEnergy = doses("dose2energy")
    val superRestore = doses("dose2restore")
    val fishing = doses("dosefisherspotion")
    val hunter = doses("dosehunting")
    val ranging = doses("doserangerspotion")
    val magic = doses("dose1magic")
    val magicEssence = doses("dosemagicess")
    val zamorak = doses("dosepotionofzamorak")
    val saradomin = doses("dosepotionofsaradomin")
    val antifire = doses("dose1antidragon")
    val extendedAntifire = doses("dose2antidragon")
    val superAntifire = doses("dose3antidragon")
    val extendedSuperAntifire = doses("dose4antidragon")
    val stamina = doses("dosestamina")
    val bastion = doses("dosebastion")
    val battlemage = doses("dosebattlemage")
    val ancientBrew = doses("doseancientbrew")
    val forgottenBrew = doses("doseforgottenbrew")
    val menaphiteRemedy = doses("dosestatrenewal")
    val prayerRegeneration = doses("dose1prayer_regeneration")
    val goading = doses("dosegoading")
    val surge = doses("dosesurge")
    val divineAttack = doses("dosedivineattack")
    val divineStrength = doses("dosedivinestrength")
    val divineDefence = doses("dosedivinedefence")
    val divineRanging = doses("dosedivinerange")
    val divineMagic = doses("dosedivinemagic")
    val divineCombat = doses("dosedivinecombat")
    val divineBastion = doses("dosedivinebastion")
    val divineBattlemage = doses("dosedivinebattlemage")
    val compost = suffixed("supercompost_potion_")
    val antidotePlus = suffixed("antidote+")
    val antidotePlusPlus = suffixed("antidote++")
    val sanfew = suffixed("sanfew_salve_", "_dose")
    val sanfewStep1 = suffixed("sanfew_salve_step_1_", "_dose")
    val sanfewStep2 = suffixed("sanfew_salve_step_2_", "_dose")

    /** The Sanfew mixtures aren't drinkable, so they join [all] only for the recipes. */
    val all: List<List<ObjType>> =
        listOf(
            attack,
            antipoison,
            superAntipoison,
            strength,
            defence,
            restore,
            energy,
            agility,
            combat,
            prayer,
            superAttack,
            superStrength,
            superDefence,
            superCombat,
            superEnergy,
            superRestore,
            fishing,
            hunter,
            ranging,
            magic,
            magicEssence,
            zamorak,
            saradomin,
            antifire,
            extendedAntifire,
            superAntifire,
            extendedSuperAntifire,
            stamina,
            bastion,
            battlemage,
            ancientBrew,
            forgottenBrew,
            menaphiteRemedy,
            prayerRegeneration,
            goading,
            surge,
            divineAttack,
            divineStrength,
            divineDefence,
            divineRanging,
            divineMagic,
            divineCombat,
            divineBastion,
            divineBattlemage,
            compost,
            antidotePlus,
            antidotePlusPlus,
            sanfew,
        )
}

object HerbloreObjs : ObjReferences() {
    val vial_water = find("vial_water")
    val vial_empty = find("vial_empty")
    val pestle_and_mortar = find("pestle_and_mortar")
    val weapon_poison = find("weapon_poison")
    val weapon_poison_plus = find("weapon_poison+")
    val weapon_poison_plus_plus = find("weapon_poison++")
    val coconut_milk = find("vial_coconut_milk")
    val red_spiders_eggs = find("red_spiders_eggs")
    val unicorn_horn_dust = find("unicorn_horn_dust")
    val dragon_scale_dust = find("dragon_scale_dust")
    val lava_shard = find("lava_shard")
}

object HerbloreSeqs : SeqReferences() {
    val mix = find("human_herbing_vial")
    val grind = find("human_herbing_grind")
}
