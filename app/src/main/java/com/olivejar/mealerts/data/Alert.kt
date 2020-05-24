package com.olivejar.mealerts.data

import com.olivejar.mealerts.database.MeDbHelper
import org.jetbrains.anko.db.insert
import org.jetbrains.anko.db.transaction
import org.jetbrains.anko.db.update

data class Alert(val id: Int = -1,
                 val date: String,
                 val time: String,
                 val name: String = "Unknown"){
    companion object {
        const val TABLE_NAME = "alerts"
        const val COL_ID = "_id"
        const val COL_DATE = "date"
        const val COL_TIME = "time"
        const val COL_NAME = "name"

        private fun insertAlerts(db: MeDbHelper, alert:Alert): Long {
            var clId: Long = -1
            db.use {
                transaction {
                    clId = insert(TABLE_NAME,
                            COL_DATE to alert.date,
                            COL_TIME to alert.time,
                            COL_NAME to alert.name)
                    }
            }
            return clId
        }
    }
}