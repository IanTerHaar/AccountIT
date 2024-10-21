package com.ianterhaar.accountit.data

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase

class UserRepository(context: Context) {
    private val dbHelper = DatabaseHelper(context)

    fun registerUser(username: String, password: String, securityQuestion: String, securityAnswer: String): Boolean {
        val db: SQLiteDatabase = dbHelper.writableDatabase
        val values = ContentValues().apply {
            put(DatabaseHelper.COLUMN_USER_NAME, username)
            put(DatabaseHelper.COLUMN_PASSWORD, password)
            put(DatabaseHelper.COLUMN_SECURITY_QUESTION, securityQuestion)
            put(DatabaseHelper.COLUMN_SECURITY_ANSWER, securityAnswer)
        }

        val newRowId = db.insert(DatabaseHelper.TABLE_USERS, null, values)
        db.close()

        return newRowId != -1L // Return true if the user is successfully inserted
    }

    // Function to check user login
    fun loginUser(username: String, password: String): Boolean {
        val db: SQLiteDatabase = dbHelper.readableDatabase
        val query = "SELECT * FROM ${DatabaseHelper.TABLE_USERS} WHERE ${DatabaseHelper.COLUMN_USER_NAME} = ? AND ${DatabaseHelper.COLUMN_PASSWORD} = ?"
        val cursor = db.rawQuery(query, arrayOf(username, password))

        val loggedIn = cursor.count > 0
        cursor.close()
        db.close()

        return loggedIn // Return true if login is successful
    }
}
