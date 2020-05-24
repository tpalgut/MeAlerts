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

data class Sighting(
        var species: String = "Unknown",
        var checklist: String = "",
        var comments: String = "",
        var location: String = "",
        var county: String = "",
        var state: String = "",
        var date: String = "",
        var map: String = "",
        var who: String = "",
        var count: Int = 0,
        var alertId: Long = -1) {

    companion object {
        const val TABLE_NAME = "sightings"
        //const val COL_ID = "_id"
        const val COL_SPECIES = "species"
        const val COL_COUNT = "count"
        const val COL_DATE = "date"
        const val COL_WHO = "who"
        const val COL_LOCATION = "location"
        const val COL_COUNTY = "county"
        const val COL_STATE = "state"
        const val COL_MAP = "map"
        const val COL_CHECKLIST = "checklist"
        const val COL_COMMENTS = "comments"
        const val COL_ALERT_ID = "alertId"

        fun getSightingsByLocation(db: MeDbHelper, alertId: Long, name: String): ArrayList<Sighting> {
            val list = ArrayList<Sighting>()
            db.use {
                val sightings = select(TABLE_NAME)
                        .whereArgs("(${Sighting.COL_ALERT_ID} = ${alertId}) and ${Sighting.COL_LOCATION} = '${name}'")
                        .orderBy(COL_LOCATION, SqlOrderDirection.ASC)
                        .parseList(classParser<Sighting>())
                list.addAll(sightings)
            }
            return list
        }
        fun getSightingsGroupByLocation(db: MeDbHelper, alertId: Long): ArrayList<Sighting> {
            val list = ArrayList<Sighting>()
            db.use {
                val sightings = select(TABLE_NAME)
                        .whereArgs("(${Sighting.COL_ALERT_ID} = ${alertId})")
                        .groupBy(Sighting.COL_LOCATION)
                        .distinct()
                        .orderBy(COL_LOCATION, SqlOrderDirection.ASC)
                        .parseList(classParser<Sighting>())
                list.addAll(sightings)
            }
            return list
        }
        fun getSightingsBySpecies(db: MeDbHelper, alertId: Long, name: String): ArrayList<Sighting> {
            val list = ArrayList<Sighting>()
            db.use {
                val sightings = select(TABLE_NAME)
                        .whereArgs("(${Sighting.COL_ALERT_ID} = ${alertId}) and ${Sighting.COL_SPECIES} = '${name}' ")
                        .orderBy(COL_SPECIES, SqlOrderDirection.ASC)
                        .parseList(classParser<Sighting>())
                list.addAll(sightings)
            }
            return list
        }

        fun getSightings(db: MeDbHelper, alertId: Long): ArrayList<Sighting> {
            val list = ArrayList<Sighting>()
            db.use {
                val sightings = select(TABLE_NAME)
                        .whereArgs("(${Sighting.COL_ALERT_ID} = ${alertId})")
                        .orderBy(COL_SPECIES, SqlOrderDirection.ASC)
                        .parseList(classParser<Sighting>())
                list.addAll(sightings)
            }
            return list
        }

        fun Insert(db: MeDbHelper, alertId: Long, birds: List<Sighting>) {
            for (bird in birds) {

                val inDb = getFromDb(db, alertId, bird)
                if (inDb == null) {
                    db.use {
                        insert(Sighting.TABLE_NAME,
                                Sighting.COL_ALERT_ID to alertId,
                                Sighting.COL_CHECKLIST to bird.checklist,
                                Sighting.COL_COMMENTS to bird.comments,
                                Sighting.COL_COUNT to bird.count,
                                Sighting.COL_DATE to bird.date,
                                Sighting.COL_LOCATION to bird.location,
                                Sighting.COL_MAP to bird.map,
                                Sighting.COL_SPECIES to bird.species,
                                Sighting.COL_WHO to bird.who
                        )
                    }
                } else {
                    if (inDb.date < bird.date) {
                        db.use {

                            update(Sighting.TABLE_NAME,
                                    Sighting.COL_CHECKLIST to bird.checklist,
                                    Sighting.COL_COMMENTS to bird.comments,
                                    Sighting.COL_COUNT to bird.count,
                                    Sighting.COL_DATE to bird.date,
                                    Sighting.COL_MAP to bird.map,
                                    Sighting.COL_WHO to bird.who
                            ).whereArgs("(${Sighting.COL_ALERT_ID} = ${alertId} and ${Sighting.COL_SPECIES} = '${bird.species}' and ${Sighting.COL_LOCATION} = '${bird.location}') ")
                                    .exec()
                        }
                    }
                }
            }
        }

        private var existing: Sighting? = null
        private fun getFromDb(db: MeDbHelper, alertId: Long, bird: Sighting): Sighting? {
            db.use {
                if ((existing == null) || (existing!!.alertId != alertId) || (existing!!.species != bird.species) || (existing!!.location != bird.location)) {
                    existing = select(Sighting.TABLE_NAME)
                            .whereArgs("(${Sighting.COL_ALERT_ID} = ${alertId} and ${Sighting.COL_SPECIES} = '${bird.species}' and ${Sighting.COL_LOCATION} = '${bird.location}') ")
                            .limit(1)
                            .parseOpt(classParser<Sighting>())
                }

            }
            return existing
        }
    }
}
