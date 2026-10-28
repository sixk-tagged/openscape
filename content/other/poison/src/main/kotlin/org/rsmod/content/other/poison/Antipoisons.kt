package org.rsmod.content.other.poison

import org.rsmod.api.type.refs.obj.ObjReferences
import org.rsmod.game.type.obj.ObjType

/**
 * [doses] runs from 4 doses down to 1. One drink cures the poison and gives [immunity] poison
 * clocks (18 seconds each) of immunity. A [restores] potion (Sanfew serum) also restores every stat
 * but Hitpoints by 4 + 30% of its base level.
 */
internal class Antipoison(
    val doses: List<ObjType>,
    val label: String,
    val immunity: Int,
    val restores: Boolean = false,
)

internal val ANTIPOISONS =
    listOf(
        Antipoison(
            listOf(
                AntipoisonObjs.antipoison4,
                AntipoisonObjs.antipoison3,
                AntipoisonObjs.antipoison2,
                AntipoisonObjs.antipoison1,
            ),
            "antipoison potion",
            immunity = 5,
        ),
        Antipoison(
            listOf(
                AntipoisonObjs.super4,
                AntipoisonObjs.super3,
                AntipoisonObjs.super2,
                AntipoisonObjs.super1,
            ),
            "superantipoison potion",
            immunity = 20,
        ),
        Antipoison(
            listOf(
                AntipoisonObjs.antidote_plus4,
                AntipoisonObjs.antidote_plus3,
                AntipoisonObjs.antidote_plus2,
                AntipoisonObjs.antidote_plus1,
            ),
            "antidote+ potion",
            immunity = 30,
        ),
        Antipoison(
            listOf(
                AntipoisonObjs.antidote_plusplus4,
                AntipoisonObjs.antidote_plusplus3,
                AntipoisonObjs.antidote_plusplus2,
                AntipoisonObjs.antidote_plusplus1,
            ),
            "antidote++ potion",
            immunity = 40,
        ),
        // Also cures disease and gives 15 minutes of disease immunity on the wiki: no disease
        // system.
        Antipoison(
            listOf(
                AntipoisonObjs.sanfew4,
                AntipoisonObjs.sanfew3,
                AntipoisonObjs.sanfew2,
                AntipoisonObjs.sanfew1,
            ),
            "Sanfew serum",
            immunity = 20,
            restores = true,
        ),
    )

object AntipoisonObjs : ObjReferences() {
    val antipoison4 = find("4doseantipoison")
    val antipoison3 = find("3doseantipoison")
    val antipoison2 = find("2doseantipoison")
    val antipoison1 = find("1doseantipoison")
    val super4 = find("4dose2antipoison")
    val super3 = find("3dose2antipoison")
    val super2 = find("2dose2antipoison")
    val super1 = find("1dose2antipoison")
    val antidote_plus4 = find("antidote+4")
    val antidote_plus3 = find("antidote+3")
    val antidote_plus2 = find("antidote+2")
    val antidote_plus1 = find("antidote+1")
    val antidote_plusplus4 = find("antidote++4")
    val antidote_plusplus3 = find("antidote++3")
    val antidote_plusplus2 = find("antidote++2")
    val antidote_plusplus1 = find("antidote++1")
    val sanfew4 = find("sanfew_salve_4_dose")
    val sanfew3 = find("sanfew_salve_3_dose")
    val sanfew2 = find("sanfew_salve_2_dose")
    val sanfew1 = find("sanfew_salve_1_dose")
}
