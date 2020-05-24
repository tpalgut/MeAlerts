package com.olivejar.mealerts.data

import android.location.Location
import com.olivejar.mealerts.database.MeDbHelper
import org.jetbrains.anko.db.*

// location, map, species list (checklist, date, who)
// species, location list (map,

// Location name                 // Common Name
// latest date  (count)          // latest date (count)

// Species (count)               // Location
// checklist                     // Map

data class Summary(
        var name: String = "Unknown",
        var date: String = "",
        var count: Int = 0,
        var alertId: Long = -1) {

    companion object {

       fun getSummariesBySpecies(db: MeDbHelper, alertId: Long): ArrayList<Summary> {
            val list = ArrayList<Summary>()
            db.use {
                val sightings = select(Sighting.TABLE_NAME,
                                                    Sighting.COL_SPECIES,
                                                    "max(${Sighting.COL_DATE}) as date",
                                                   "count(${Sighting.COL_LOCATION}) as count",
                                                    Sighting.COL_ALERT_ID)
                        .whereArgs("(${Sighting.COL_ALERT_ID} = ${alertId})")
                        .groupBy(Sighting.COL_SPECIES)
                        .orderBy(Sighting.COL_SPECIES, SqlOrderDirection.ASC)
                        .parseList(classParser<Summary>())
                list.addAll(sightings)
            }
            return list
        }

        fun getSummariesByLocation(db: MeDbHelper, alertId: Long): ArrayList<Summary> {
            val list = ArrayList<Summary>()
            db.use {
                val sightings = select(Sighting.TABLE_NAME,
                                                    Sighting.COL_LOCATION,
                                                    "max(${Sighting.COL_DATE}) as date",
                                                   "count(${Sighting.COL_SPECIES}) as count",
                                                    Sighting.COL_ALERT_ID)
                        .whereArgs("(${Sighting.COL_ALERT_ID} = ${alertId})")
                        .groupBy(Sighting.COL_LOCATION)
                        .orderBy(Sighting.COL_LOCATION, SqlOrderDirection.ASC)
                        .parseList(classParser<Summary>())
                list.addAll(sightings)
            }
            return list
        }
        val latLng = Regex("http:.*ll=(.*),(.*)\$")
        private fun getLocFromMap(map: String): Location? {
            var m = latLng.find(map)
            if (m != null) {
                var loc = Location("")
                try {
                    loc.latitude = m.groupValues[1].toDouble()
                    loc.longitude = m.groupValues[2].toDouble()
                }
                catch (_:Exception ){
                    return null
                }
                return loc
            }
            return null
        }

        private data class DistTo(
            val dist:Float,
            val sight: Sighting)
        {
        }

        fun getClosest(database: MeDbHelper, alertId: Long, loc: Location?): ArrayList<Summary> {
            var distMap= ArrayList<DistTo>()
            var minS = ArrayList<Summary>()
            if (loc!= null) {
                var sightings = Sighting.getSightingsGroupByLocation(database, alertId)
                for (s in sightings) {
                    var sLoc = getLocFromMap(s.map)
                    if (sLoc != null) {
                        var dist = loc.distanceTo(sLoc)
                        distMap.add(DistTo(dist, s))
                    }
                }
                val sortedMap = distMap.sortedWith(compareBy({it.dist}))
                var num = Math.min(10, sortedMap.count())
                for (i in 0..num)
                {
                    val s = sortedMap[i].sight
                    minS.add(Summary(s.location,s.date,s.count,s.alertId))
                }
            }
            return minS
        }
    }
}
