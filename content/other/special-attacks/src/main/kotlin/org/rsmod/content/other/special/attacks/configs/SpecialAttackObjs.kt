package org.rsmod.content.other.special.attacks.configs

import org.rsmod.api.type.refs.obj.ObjReferences

typealias special_objs = SpecialAttackObjs

/** Spec weapons that `BaseObjs` doesn't already reference. */
object SpecialAttackObjs : ObjReferences() {
    val dragon_dagger = find("dragon_dagger")
    val granite_maul = find("granite_maul")
    val vestas_longsword = find("vestas_longsword")
    val dragon_scimitar = find("dragon_scimitar")
    val dragon_mace = find("dragon_mace")
    val dragon_battleaxe = find("dragon_battleaxe")
    val magic_shortbow_i = find("magic_shortbow_i")
    val magic_longbow = find("magic_longbow")
}
