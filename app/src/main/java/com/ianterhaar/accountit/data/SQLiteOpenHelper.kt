package com.ianterhaar.accountit.data

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

// Database class to manage SQLite database
class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        const val DATABASE_NAME = "accountit.db"
        const val DATABASE_VERSION = 1

        // User Table
        const val TABLE_USERS = "users"
        const val COLUMN_USER_ID = "id"
        const val COLUMN_USER_NAME = "username"
        const val COLUMN_PASSWORD = "password"
        const val COLUMN_SECURITY_QUESTION = "security_question"
        const val COLUMN_SECURITY_ANSWER = "security_answer"

    }

    override fun onCreate(db: SQLiteDatabase) {
        // Create users table
        val createUserTable = """
            CREATE TABLE $TABLE_USERS (
                $COLUMN_USER_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_USER_NAME TEXT NOT NULL,
                $COLUMN_PASSWORD TEXT NOT NULL,
                $COLUMN_SECURITY_QUESTION TEXT NOT NULL,
                $COLUMN_SECURITY_ANSWER TEXT NOT NULL
            )
        """
        db.execSQL(createUserTable)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        // Upgrade the database if needed (e.g., add new tables or columns)
        db.execSQL("DROP TABLE IF EXISTS $TABLE_USERS")
        onCreate(db)
    }
}

