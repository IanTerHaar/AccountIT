package com.ianterhaar.accountit.data

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase

data class UserCredentials(
    val username: String,
    val password: String
)

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

        return newRowId != -1L
    }

    fun loginUser(username: String, password: String): Boolean {
        val db: SQLiteDatabase = dbHelper.readableDatabase
        val query = "SELECT * FROM ${DatabaseHelper.TABLE_USERS} WHERE ${DatabaseHelper.COLUMN_USER_NAME} = ? AND ${DatabaseHelper.COLUMN_PASSWORD} = ?"
        val cursor = db.rawQuery(query, arrayOf(username, password))

        val loggedIn = cursor.count > 0
        cursor.close()
        db.close()

        return loggedIn
    }

    fun getSecurityQuestion(username: String): String? {
        val db: SQLiteDatabase = dbHelper.readableDatabase
        val query = "SELECT ${DatabaseHelper.COLUMN_SECURITY_QUESTION} FROM ${DatabaseHelper.TABLE_USERS} WHERE ${DatabaseHelper.COLUMN_USER_NAME} = ?"
        val cursor = db.rawQuery(query, arrayOf(username))

        var question: String? = null
        if (cursor.moveToFirst()) {
            question = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_SECURITY_QUESTION))
        }
        cursor.close()
        db.close()

        return question
    }

    fun verifySecurityAnswer(username: String, securityAnswer: String): UserCredentials? {
        val db: SQLiteDatabase = dbHelper.readableDatabase
        // Using LOWER() SQL function to make comparison case-insensitive
        val query = "SELECT ${DatabaseHelper.COLUMN_USER_NAME}, ${DatabaseHelper.COLUMN_PASSWORD} FROM ${DatabaseHelper.TABLE_USERS} " +
                "WHERE ${DatabaseHelper.COLUMN_USER_NAME} = ? AND LOWER(${DatabaseHelper.COLUMN_SECURITY_ANSWER}) = LOWER(?)"
        val cursor = db.rawQuery(query, arrayOf(username, securityAnswer))

        var credentials: UserCredentials? = null
        if (cursor.moveToFirst()) {
            credentials = UserCredentials(
                cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_USER_NAME)),
                cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_PASSWORD))
            )
        }
        cursor.close()
        db.close()

        return credentials
    }
}