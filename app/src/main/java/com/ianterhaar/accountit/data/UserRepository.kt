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

    fun authenticateUser(username: String, password: String): Int {
        val db = dbHelper.readableDatabase
        val cursor = db.query(
            DatabaseHelper.TABLE_USERS,
            arrayOf(DatabaseHelper.COLUMN_USER_ID),
            "${DatabaseHelper.COLUMN_USER_NAME} = ? AND ${DatabaseHelper.COLUMN_PASSWORD} = ?",
            arrayOf(username, password),
            null,
            null,
            null
        )

        return if (cursor.moveToFirst()) {
            val userId = cursor.getInt(0)
            cursor.close()
            userId
        } else {
            cursor.close()
            -1
        }
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

    fun registerUser(username: String, password: String, securityQuestion: String, securityAnswer: String): Boolean {
        val db: SQLiteDatabase = dbHelper.writableDatabase
        db.beginTransaction()
        try {
            // Insert user
            val userValues = ContentValues().apply {
                put(DatabaseHelper.COLUMN_USER_NAME, username)
                put(DatabaseHelper.COLUMN_PASSWORD, password)
                put(DatabaseHelper.COLUMN_SECURITY_QUESTION, securityQuestion)
                put(DatabaseHelper.COLUMN_SECURITY_ANSWER, securityAnswer)
            }

            val userId = db.insert(DatabaseHelper.TABLE_USERS, null, userValues)

            if (userId != -1L) {
                // Initialize budget record for the new user
                val budgetValues = ContentValues().apply {
                    put(DatabaseHelper.COLUMN_USER_ID_FK, userId)
                    put(DatabaseHelper.COLUMN_TOTAL_BUDGET, 0.0) // Initial budget of 0
                    put(DatabaseHelper.COLUMN_INCOME, 0.0)      // Initial income of 0
                }

                val budgetResult = db.insert(DatabaseHelper.TABLE_BUDGETS, null, budgetValues)

                if (budgetResult != -1L) {
                    db.setTransactionSuccessful()
                    return true
                }
            }
            return false
        } finally {
            db.endTransaction()
            db.close()
        }
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

    fun getCurrency(userID: Int): String? {
        // Mapping of currency codes to symbols
        val currencySymbols = mapOf(
            "USD" to "$",        // United States
            "EUR" to "€",        // European Union
            "GBP" to "£",        // United Kingdom
            "JPY" to "¥",        // Japan
            "CAD" to "$",        // Canada
            "AUD" to "$",        // Australia
            "CHF" to "Fr",       // Switzerland
            "CNY" to "¥",        // China
            "KRW" to "₩",        // South Korea
            "ZAR" to "R"         // South Africa
        )

        val db: SQLiteDatabase = dbHelper.readableDatabase
        val query = "SELECT ${DatabaseHelper.COLUMN_CURRENCY} FROM ${DatabaseHelper.TABLE_USERS} " +
                "WHERE ${DatabaseHelper.COLUMN_USER_ID} = ?"
        val cursor = db.rawQuery(query, arrayOf(userID.toString()))

        var currencySymbol: String? = null
        if (cursor.moveToFirst()) {
            val currencyCode = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_CURRENCY))
            currencySymbol = currencySymbols[currencyCode] // Get the symbol from the map
        }
        cursor.close()
        db.close()

        return currencySymbol
    }

    fun updateCurrency(userId: Int, newCurrency: String) {
        val db = dbHelper.writableDatabase // Correctly access writableDatabase from dbHelper
        val values = ContentValues().apply {
            put(DatabaseHelper.COLUMN_CURRENCY, newCurrency) // Use DatabaseHelper to prefix the constant
        }
        db.update(
            DatabaseHelper.TABLE_USERS, // Use DatabaseHelper to prefix the constant
            values,
            "${DatabaseHelper.COLUMN_USER_ID} = ?", // Use DatabaseHelper to prefix the constant
            arrayOf(userId.toString())
        )
        db.close() // Always close the database after operations
    }

    fun getCurrencyCode(userId: Int): String? {
        val db = dbHelper.readableDatabase
        val query = "SELECT ${DatabaseHelper.COLUMN_CURRENCY} FROM ${DatabaseHelper.TABLE_USERS} WHERE ${DatabaseHelper.COLUMN_USER_ID} = ?"
        val cursor = db.rawQuery(query, arrayOf(userId.toString()))

        var currency: String? = null
        if (cursor.moveToFirst()) {
            // Retrieve the currency value from the database
            currency = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_CURRENCY))
        }

        // Close cursor and database to prevent memory leaks
        cursor.close()
        db.close()

        return currency
    }
}