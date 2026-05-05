package com.example.project_skebob

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "facts_db"
        private const val DATABASE_VERSION = 3
        const val TABLE_NAME = "facts"
        const val COLUMN_ID = "id"
        const val COLUMN_FACT = "fact_text"
        const val COLUMN_TIMESTAMP = "timestamp"
        const val COLUMN_IS_FAVORITE = "is_favorite"
    }

    override fun onCreate(db: SQLiteDatabase) {
        val createTable = "CREATE TABLE $TABLE_NAME (" +
                "$COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "$COLUMN_FACT TEXT, " +
                "$COLUMN_TIMESTAMP INTEGER, " +
                "$COLUMN_IS_FAVORITE INTEGER DEFAULT 0)"
        db.execSQL(createTable)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        if (oldVersion < 3) {
            db.execSQL("ALTER TABLE $TABLE_NAME ADD COLUMN $COLUMN_IS_FAVORITE INTEGER DEFAULT 0")
        }
    }

    fun insertFact(fact: String): Long {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_FACT, fact)
            put(COLUMN_TIMESTAMP, System.currentTimeMillis())
            put(COLUMN_IS_FAVORITE, 0)
        }
        return db.insert(TABLE_NAME, null, values)
    }

    fun toggleFavorite(id: Long, currentStatus: Boolean) {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_IS_FAVORITE, if (currentStatus) 0 else 1)
        }
        db.update(TABLE_NAME, values, "$COLUMN_ID=?", arrayOf(id.toString()))
    }

    fun deleteFact(id: Long) {
        val db = this.writableDatabase
        db.delete(TABLE_NAME, "$COLUMN_ID=?", arrayOf(id.toString()))
    }

    fun getFacts(onlyFavorites: Boolean = false, filterLast24h: Boolean = false): List<FactRecord> {
        val factList = mutableListOf<FactRecord>()
        val db = this.readableDatabase

        val conditions = mutableListOf<String>()
        if (onlyFavorites) conditions.add("$COLUMN_IS_FAVORITE = 1")
        if (filterLast24h) {
            val yesterday = System.currentTimeMillis() - (24 * 60 * 60 * 1000)
            conditions.add("$COLUMN_TIMESTAMP >= $yesterday")
        }

        val whereClause = if (conditions.isNotEmpty()) "WHERE ${conditions.joinToString(" AND ")}" else ""
        val query = "SELECT * FROM $TABLE_NAME $whereClause ORDER BY $COLUMN_TIMESTAMP DESC"

        val cursor = db.rawQuery(query, null)
        if (cursor.moveToFirst()) {
            do {
                val id = cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_ID))
                val text = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_FACT))
                val ts = cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_TIMESTAMP))
                val isFav = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_IS_FAVORITE)) == 1
                factList.add(FactRecord(id, text, ts, isFav))
            } while (cursor.moveToNext())
        }
        cursor.close()
        return factList
    }

    fun getAggregationStats(onlyFavorites: Boolean = false): String {
        val db = this.readableDatabase
        val filter = if (onlyFavorites) " WHERE $COLUMN_IS_FAVORITE = 1" else ""

        val cursor = db.rawQuery("SELECT COUNT(*), MAX(LENGTH($COLUMN_FACT)) FROM $TABLE_NAME$filter", null)
        var stats = "Нет данных"
        if (cursor.moveToFirst()) {
            stats = "Записей: ${cursor.getInt(0)} | Макс. длина: ${cursor.getInt(1)}"
        }
        cursor.close()
        return stats
    }
}