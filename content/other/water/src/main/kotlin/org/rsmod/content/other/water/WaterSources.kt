package org.rsmod.content.other.water

import org.rsmod.api.player.protect.ProtectedAccess
import org.rsmod.api.script.onOpLocU
import org.rsmod.api.script.onPlayerQueueWithArgs
import org.rsmod.api.type.refs.loc.LocReferences
import org.rsmod.api.type.refs.obj.ObjReferences
import org.rsmod.api.type.refs.queue.QueueReferences
import org.rsmod.api.type.refs.seq.SeqReferences
import org.rsmod.game.type.obj.ObjType
import org.rsmod.plugin.scripts.PluginScript
import org.rsmod.plugin.scripts.ScriptContext

/** Sinks and fountains fill every empty vessel of the kind used on them; wells take buckets. */
class WaterSources : PluginScript() {
    override fun ScriptContext.startup() {
        for (source in WaterLocs.taps) {
            for (vessel in WaterVessels.all) {
                onOpLocU(source, vessel.empty) {
                    fill(FillTask(vessel, it.type.name.lowercase()), FILL_TICKS)
                }
            }
        }
        val bucket = WaterVessels.all.first()
        onOpLocU(WaterLocs.well, bucket.empty) { fill(FillTask(bucket, "well"), FILL_TICKS) }
        // A weak queue added while the queue runs is ticked down that same cycle: ask for one more.
        onPlayerQueueWithArgs<FillTask>(WaterQueues.fill) { fill(it.args, FILL_TICKS + 1) }
    }

    private fun ProtectedAccess.fill(task: FillTask, nextDelay: Int) {
        val vessel = task.vessel
        if (!invReplace(inv, vessel.empty, 1, vessel.full).success) {
            return
        }
        anim(WaterSeqs.fill)
        spam("You fill the ${ocName(vessel.empty).lowercase()} from the ${task.source}.")
        if (vessel.empty in inv) {
            weakQueue(WaterQueues.fill, nextDelay, task)
        }
    }

    private data class FillTask(val vessel: WaterVessels.Vessel, val source: String)

    private companion object {
        // Note: 2 ticks per vessel is unverified; the OSRS wiki gives no timing.
        const val FILL_TICKS = 2
    }
}

object WaterVessels {
    data class Vessel(val empty: ObjType, val full: ObjType)

    val all =
        with(WaterObjs) {
            listOf(
                Vessel(bucket, bucket_of_water),
                Vessel(jug, jug_of_water),
                Vessel(bowl, bowl_of_water),
                Vessel(vial, vial_of_water),
            )
        }
}

object WaterLocs : LocReferences() {
    val sink = find("sink")
    val kitchen_sink = find("qip_cook_sink")
    val farm_sink = find("qip_sheep_shearer_sink")
    val cellar_sink = find("aide_sink")
    val fountain = find("fountain")
    val well = find("well")

    /** Every sink and fountain in Lumbridge (map squares 49-50, 49-51 and the castle cellar). */
    val taps
        get() = listOf(sink, kitchen_sink, farm_sink, cellar_sink, fountain)
}

object WaterObjs : ObjReferences() {
    val bucket = find("bucket_empty")
    val bucket_of_water = find("bucket_water")
    val jug = find("jug_empty")
    val jug_of_water = find("jug_water")
    val bowl = find("bowl_empty")
    val bowl_of_water = find("bowl_water")
    val vial = find("vial_empty")
    val vial_of_water = find("vial_water")
}

object WaterSeqs : SeqReferences() {
    val fill = find("human_pickuptable")
}

object WaterQueues : QueueReferences() {
    val fill = find("water_fill")
}
