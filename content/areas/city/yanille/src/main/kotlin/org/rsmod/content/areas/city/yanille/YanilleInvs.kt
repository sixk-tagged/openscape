@file:Suppress("unused", "SpellCheckingInspection")

package org.rsmod.content.areas.city.yanille

import org.rsmod.api.type.editors.inv.InvEditor
import org.rsmod.api.type.refs.obj.ObjReferences
import org.rsmod.game.type.inv.InvScope
import org.rsmod.game.type.inv.InvStackType
import org.rsmod.game.type.inv.InvType
import org.rsmod.game.type.obj.ObjType

typealias yanille_objs = YanilleObjs

object YanilleObjs : ObjReferences() {
    val airrune = find("airrune")
    val battlestaff = find("battlestaff")
    val bloodrune = find("bloodrune")
    val bodyrune = find("bodyrune")
    val bowl_empty = find("bowl_empty")
    val butterfly_jar = find("butterfly_jar")
    val cake_tin = find("cake_tin")
    val chaosrune = find("chaosrune")
    val chocolate_bar = find("chocolate_bar")
    val cooking_apple = find("cooking_apple")
    val cup_empty = find("cup_empty")
    val deathrune = find("deathrune")
    val earthrune = find("earthrune")
    val firerune = find("firerune")
    val hunting_box_trap = find("hunting_box_trap")
    val hunting_butterfly_net = find("hunting_butterfly_net")
    val hunting_ojibway_bird_snare = find("hunting_ojibway_bird_snare")
    val hunting_snare = find("hunting_snare")
    val hunting_teasing_stick = find("hunting_teasing_stick")
    val jug_empty = find("jug_empty")
    val lawrune = find("lawrune")
    val magic_imp_box = find("magic_imp_box")
    val mindrune = find("mindrune")
    val mystic_boots = find("mystic_boots")
    val mystic_gloves = find("mystic_gloves")
    val mystic_hat = find("mystic_hat")
    val mystic_robe_bottom = find("mystic_robe_bottom")
    val mystic_robe_top = find("mystic_robe_top")
    val naturerune = find("naturerune")
    val noose_wand = find("noose_wand")
    val pack_airrune = find("pack_airrune")
    val pack_box_trap = find("pack_box_trap")
    val pack_chaosrune = find("pack_chaosrune")
    val pack_earthrune = find("pack_earthrune")
    val pack_firerune = find("pack_firerune")
    val pack_jug_empty = find("pack_jug_empty")
    val pack_magic_imp_box = find("pack_magic_imp_box")
    val pack_mindrune = find("pack_mindrune")
    val pack_ojibway_bird_snare = find("pack_ojibway_bird_snare")
    val pack_waterrune = find("pack_waterrune")
    val piedish = find("piedish")
    val pot_empty = find("pot_empty")
    val pot_flour = find("pot_flour")
    val potato = find("potato")
    val skillcape_magic = find("skillcape_magic")
    val skillcape_magic_trimmed = find("skillcape_magic_trimmed")
    val soulrune = find("soulrune")
    val staff_of_air = find("staff_of_air")
    val staff_of_earth = find("staff_of_earth")
    val staff_of_fire = find("staff_of_fire")
    val staff_of_water = find("staff_of_water")
    val tinderbox = find("tinderbox")
    val torch_unlit = find("torch_unlit")
    val waterrune = find("waterrune")
}

internal object YanilleInvEditor : InvEditor() {
    init {
        shop(
            yanille_invs.aleck,
            line(yanille_objs.hunting_butterfly_net, 5, 10),
            line(yanille_objs.butterfly_jar, 100, 10),
            line(yanille_objs.magic_imp_box, 30, 50),
            line(yanille_objs.noose_wand, 50, 5),
            line(yanille_objs.hunting_ojibway_bird_snare, 50, 5),
            line(yanille_objs.hunting_box_trap, 25, 10),
            line(yanille_objs.hunting_teasing_stick, 5, 10),
            line(yanille_objs.torch_unlit, 20, 10),
            line(yanille_objs.hunting_snare, 10, 10),
            line(yanille_objs.pack_ojibway_bird_snare, 3, 10),
            line(yanille_objs.pack_box_trap, 3, 10),
            line(yanille_objs.pack_magic_imp_box, 3, 10),
        )

        shop(
            yanille_invs.frenita,
            line(yanille_objs.piedish, 5, 200),
            line(yanille_objs.cooking_apple, 2, 200),
            line(yanille_objs.cake_tin, 2, 200),
            line(yanille_objs.bowl_empty, 2, 200),
            line(yanille_objs.potato, 5, 200),
            line(yanille_objs.tinderbox, 4, 200),
            line(yanille_objs.jug_empty, 1, 100),
            line(yanille_objs.pack_jug_empty, 3, 15),
            line(yanille_objs.pot_empty, 8, 100),
            line(yanille_objs.chocolate_bar, 2, 100),
            line(yanille_objs.pot_flour, 8, 100),
            line(yanille_objs.cup_empty, 20, 50),
        )

        shop(
            yanille_invs.akutha,
            line(yanille_objs.airrune, 5000, 10),
            line(yanille_objs.waterrune, 5000, 10),
            line(yanille_objs.earthrune, 5000, 10),
            line(yanille_objs.firerune, 5000, 10),
            line(yanille_objs.mindrune, 5000, 10),
            line(yanille_objs.bodyrune, 5000, 10),
            line(yanille_objs.chaosrune, 250, 10),
            line(yanille_objs.naturerune, 250, 30),
            line(yanille_objs.deathrune, 250, 15),
            line(yanille_objs.lawrune, 250, 30),
            line(yanille_objs.bloodrune, 250, 30),
            line(yanille_objs.soulrune, 250, 10),
            line(yanille_objs.pack_firerune, 80, 10),
            line(yanille_objs.pack_waterrune, 80, 10),
            line(yanille_objs.pack_airrune, 80, 10),
            line(yanille_objs.pack_earthrune, 80, 10),
            line(yanille_objs.pack_mindrune, 40, 10),
            line(yanille_objs.pack_chaosrune, 35, 10),
            line(yanille_objs.battlestaff, 5, 90),
            line(yanille_objs.staff_of_fire, 2, 1000),
            line(yanille_objs.staff_of_water, 2, 1000),
            line(yanille_objs.staff_of_air, 2, 1000),
            line(yanille_objs.staff_of_earth, 2, 1000),
        )

        shop(
            yanille_invs.sinina,
            line(yanille_objs.mystic_hat, 1000, 100),
            line(yanille_objs.mystic_robe_top, 1000, 100),
            line(yanille_objs.mystic_robe_bottom, 1000, 100),
            line(yanille_objs.mystic_gloves, 1000, 100),
            line(yanille_objs.mystic_boots, 1000, 100),
        )
    }

    private class Line(val obj: ObjType, val count: Int, val restockCycles: Int)

    private fun line(obj: ObjType, count: Int, restockCycles: Int) = Line(obj, count, restockCycles)

    private fun shop(inv: InvType, vararg lines: Line) {
        edit(inv) {
            scope = InvScope.Shared
            stack = InvStackType.Always
            restock = true
            autoSize = true
            for (line in lines) {
                stock += stock(line.obj, line.count, line.restockCycles)
            }
        }
    }
}
