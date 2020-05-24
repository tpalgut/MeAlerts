package com.olivejar.mealerts.database

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import com.olivejar.mealerts.data.Alert
import org.jetbrains.anko.db.*

class MeDbHelper(ctx: Context) : ManagedSQLiteOpenHelper(ctx, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "meAlerts"
        private const val DATABASE_VERSION = 20180513

        private var instance: MeDbHelper? = null

        @Synchronized
        fun getInstance(ctx: Context): MeDbHelper {
            if (instance == null) {
                instance = MeDbHelper(ctx.applicationContext)
            }
            return instance!!
        }
    }

    override fun onCreate(db: SQLiteDatabase) {
        // Here you create tables
        createAlertsTable(db)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        deleteAlertsTable(db)
        onCreate(db)
    }

    override fun onDowngrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        onUpgrade(db, oldVersion, newVersion)
    }

    private fun createAlertsTable(db: SQLiteDatabase) {
        db.createTable(Alert.TABLE_NAME, true,
                Alert.COL_ID to INTEGER + PRIMARY_KEY + AUTOINCREMENT,
                Alert.COL_DATE to TEXT + DEFAULT("''"),
                Alert.COL_TIME to TEXT + DEFAULT("''"),
                Alert.COL_NAME to TEXT + NOT_NULL + UNIQUE + DEFAULT("'Unknown'")
        )
    }

    fun clearAlerts() {
        val db = writableDatabase
        deleteAlertsTable(db)
        createAlertsTable(db)
        db.close()
    }

    private fun deleteAlertsTable(db: SQLiteDatabase) {
        db.dropTable(Alert.TABLE_NAME, true)
    }
}

// Access property for Context
val Context.database: MeDbHelper
    get() = MeDbHelper.getInstance(applicationContext)
