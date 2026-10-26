@file:Suppress("unused", "SpellCheckingInspection")

package org.rsmod.content.areas.city.alkharid.configs

import org.rsmod.api.type.editors.inv.InvEditor
import org.rsmod.api.type.refs.inv.InvReferences
import org.rsmod.api.type.refs.obj.ObjReferences
import org.rsmod.game.type.inv.InvScope
import org.rsmod.game.type.inv.InvStackType
import org.rsmod.game.type.inv.InvType
import org.rsmod.game.type.obj.ObjType

typealias alkharid_invs = AlKharidInvs

typealias alkharid_objs = AlKharidObjs

object AlKharidInvs : InvReferences() {
    val general = find("generalshop3")
    val crafting = find("craftingshop")
    val legs = find("legsshop")
    val skirts = find("skirtshop")
    val scimitars = find("scimitarshop")
    val gems = find("gemshop")
    val ali = find("feud_morrisanes")
    val shantay = find("shantayshop")
}

object AlKharidObjs : ObjReferences() {
    val pot = find("pot_empty")
    val jug = find("jug_empty")
    val jug_pack = find("pack_jug_empty")
    val shears = find("shears")
    val bucket = find("bucket_empty")
    val bucket_pack = find("pack_bucket")
    val bowl = find("bowl_empty")
    val cake_tin = find("cake_tin")
    val tinderbox = find("tinderbox")
    val chisel = find("chisel")
    val hammer = find("hammer")
    val newcomer_map = find("newcomer_map")
    val security_book = find("sos_security_book")

    val ring_mould = find("ring_mould")
    val necklace_mould = find("necklace_mould")
    val amulet_mould = find("amulet_mould")
    val needle = find("needle")
    val thread = find("thread")
    val holy_mould = find("holy_symbol_mould")
    val sickle_mould = find("sickle_mould")
    val tiara_mould = find("tiara_mould")
    val bolt_mould = find("xbows_silver_bolt_mould")
    val bracelet_mould = find("jewl_bracelet_mould")

    val bronze_platelegs = find("bronze_platelegs")
    val iron_platelegs = find("iron_platelegs")
    val steel_platelegs = find("steel_platelegs")
    val black_platelegs = find("black_platelegs")
    val mithril_platelegs = find("mithril_platelegs")
    val adamant_platelegs = find("adamant_platelegs")
    val bronze_plateskirt = find("bronze_plateskirt")
    val iron_plateskirt = find("iron_plateskirt")
    val steel_plateskirt = find("steel_plateskirt")
    val black_plateskirt = find("black_plateskirt")
    val mithril_plateskirt = find("mithril_plateskirt")
    val adamant_plateskirt = find("adamant_plateskirt")

    val bronze_scimitar = find("bronze_scimitar")
    val iron_scimitar = find("iron_scimitar")
    val steel_scimitar = find("steel_scimitar")
    val mithril_scimitar = find("mithril_scimitar")

    val uncut_sapphire = find("uncut_sapphire")
    val uncut_emerald = find("uncut_emerald")
    val uncut_ruby = find("uncut_ruby")
    val uncut_diamond = find("uncut_diamond")
    val sapphire = find("sapphire")
    val emerald = find("emerald")
    val ruby = find("ruby")
    val diamond = find("diamond")

    val waterskin_3 = find("water_skin3")
    val waterskin_4 = find("water_skin4")
    val waterskin_0 = find("water_skin0")
    val desert_shirt = find("desert_shirt")
    val desert_robe = find("desert_robe")
    val desert_boots = find("desert_boots")
    val fake_beard = find("feud_karidian_fakebeard")
    val kharidian_headpiece = find("feud_karidian_turban")
    val papyrus = find("papyrus")
    val knife = find("knife")
    val bronze_pickaxe = find("bronze_pickaxe")
    val raw_chicken = find("raw_chicken")
    val jug_water = find("jug_water")
    val bowl_water = find("bowl_water")
    val bucket_water = find("bucket_water")
    val bronze_bar = find("bronze_bar")
    val feather = find("feather")
    val feather_pack = find("pack_feather")
    val shantay_pass = find("shantay_pass")
    val rope = find("rope")

    val kebab = find("kebab")
    val silk = find("silk")
    val keris = find("contact_keris")
    val keris_partisan = find("keris_partisan")
}

internal object AlKharidInvEditor : InvEditor() {
    init {
        // Shows the same stock as the Lumbridge General Store, minus the knife and spade.
        shop(
            alkharid_invs.general,
            general = true,
            line(alkharid_objs.pot, 5, 10),
            line(alkharid_objs.jug, 2, 100),
            line(alkharid_objs.jug_pack, 5, 20),
            line(alkharid_objs.shears, 2, 100),
            line(alkharid_objs.bucket, 3, 10),
            line(alkharid_objs.bucket_pack, 15, 10),
            line(alkharid_objs.bowl, 2, 50),
            line(alkharid_objs.cake_tin, 2, 50),
            line(alkharid_objs.tinderbox, 2, 100),
            line(alkharid_objs.chisel, 2, 100),
            line(alkharid_objs.hammer, 5, 100),
            line(alkharid_objs.newcomer_map, 5, 100),
            line(alkharid_objs.security_book, 5, 100),
        )

        shop(
            alkharid_invs.crafting,
            general = false,
            line(alkharid_objs.chisel, 2, 100),
            line(alkharid_objs.ring_mould, 4, 100),
            line(alkharid_objs.necklace_mould, 2, 100),
            line(alkharid_objs.amulet_mould, 2, 100),
            line(alkharid_objs.needle, 3, 100),
            line(alkharid_objs.thread, 100, 5),
            line(alkharid_objs.holy_mould, 3, 100),
            line(alkharid_objs.sickle_mould, 6, 15),
            line(alkharid_objs.tiara_mould, 10, 10),
            line(alkharid_objs.bolt_mould, 10, 10),
            line(alkharid_objs.bracelet_mould, 5, 100),
        )

        shop(
            alkharid_invs.legs,
            general = false,
            line(alkharid_objs.bronze_platelegs, 5, 100),
            line(alkharid_objs.iron_platelegs, 3, 400),
            line(alkharid_objs.steel_platelegs, 2, 900),
            line(alkharid_objs.black_platelegs, 1, 1200),
            line(alkharid_objs.mithril_platelegs, 1, 2000),
            line(alkharid_objs.adamant_platelegs, 1, 13000),
        )

        shop(
            alkharid_invs.skirts,
            general = false,
            line(alkharid_objs.bronze_plateskirt, 5, 100),
            line(alkharid_objs.iron_plateskirt, 3, 400),
            line(alkharid_objs.steel_plateskirt, 2, 900),
            line(alkharid_objs.black_plateskirt, 1, 1200),
            line(alkharid_objs.mithril_plateskirt, 1, 2000),
            line(alkharid_objs.adamant_plateskirt, 1, 13000),
        )

        shop(
            alkharid_invs.scimitars,
            general = false,
            line(alkharid_objs.bronze_scimitar, 5, 100),
            line(alkharid_objs.iron_scimitar, 3, 200),
            line(alkharid_objs.steel_scimitar, 2, 600),
            line(alkharid_objs.mithril_scimitar, 1, 4000),
        )

        // Rubies and diamonds have no base stock: they only appear once players sell them.
        shop(
            alkharid_invs.gems,
            general = false,
            line(alkharid_objs.uncut_sapphire, 1, 25000),
            line(alkharid_objs.uncut_emerald, 1, 40000),
            line(alkharid_objs.uncut_ruby, 0, 2000),
            line(alkharid_objs.uncut_diamond, 0, 4000),
            line(alkharid_objs.sapphire, 1, 15000),
            line(alkharid_objs.emerald, 1, 35000),
            line(alkharid_objs.ruby, 0, 2000),
            line(alkharid_objs.diamond, 0, 4000),
        )

        // Only the basic stock: the Rogue Trader blackjack, rune and clothing lines are skipped.
        shop(
            alkharid_invs.ali,
            general = false,
            line(alkharid_objs.pot, 3, 100),
            line(alkharid_objs.jug, 2, 100),
            line(alkharid_objs.jug_pack, 4, 25),
            line(alkharid_objs.waterskin_3, 10, 300),
            line(alkharid_objs.desert_shirt, 3, 12),
            line(alkharid_objs.desert_boots, 2, 100),
            line(alkharid_objs.bucket, 19, 400),
            line(alkharid_objs.bucket_pack, 10, 10),
            line(alkharid_objs.fake_beard, 11, 10),
            line(alkharid_objs.kharidian_headpiece, 12, 10),
            line(alkharid_objs.papyrus, 50, 10),
            line(alkharid_objs.knife, 5, 100),
            line(alkharid_objs.tinderbox, 11, 100),
            line(alkharid_objs.bronze_pickaxe, 23, 100),
            line(alkharid_objs.raw_chicken, 15, 100),
        )

        // UNVERIFIED: the wiki does not say whether the Shantay Pass Shop buys only its own stock.
        shop(
            alkharid_invs.shantay,
            general = false,
            line(alkharid_objs.waterskin_4, 100, 10),
            line(alkharid_objs.waterskin_0, 100, 10),
            line(alkharid_objs.jug_water, 10, 100),
            line(alkharid_objs.bowl_water, 10, 100),
            line(alkharid_objs.bucket_water, 10, 100),
            line(alkharid_objs.knife, 10, 100),
            line(alkharid_objs.desert_shirt, 10, 500),
            line(alkharid_objs.desert_robe, 10, 500),
            line(alkharid_objs.desert_boots, 10, 500),
            line(alkharid_objs.bronze_bar, 10, 500),
            line(alkharid_objs.feather, 500, 50),
            line(alkharid_objs.feather_pack, 50, 20),
            line(alkharid_objs.hammer, 10, 500),
            line(alkharid_objs.bucket_pack, 15, 10),
            line(alkharid_objs.bucket, 0, 100),
            line(alkharid_objs.bowl, 0, 100),
            line(alkharid_objs.jug, 0, 100),
            line(alkharid_objs.jug_pack, 0, 50),
            line(alkharid_objs.shantay_pass, 500, 1),
            line(alkharid_objs.rope, 20, 200),
        )
    }

    private class Line(val obj: ObjType, val count: Int, val restockCycles: Int)

    private fun line(obj: ObjType, count: Int, restockCycles: Int) = Line(obj, count, restockCycles)

    /** A general store keeps the cache's 40 slots so it has room for what players sell it. */
    private fun shop(inv: InvType, general: Boolean, vararg lines: Line) {
        edit(inv) {
            scope = InvScope.Shared
            stack = InvStackType.Always
            restock = true
            allStock = general
            autoSize = !general
            for (line in lines) {
                stock += stock(line.obj, line.count, line.restockCycles)
            }
        }
    }
}
