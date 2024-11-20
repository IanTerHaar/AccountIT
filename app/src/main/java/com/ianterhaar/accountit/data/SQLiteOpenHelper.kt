//SQLiteOpenHelper.kt
package com.ianterhaar.accountit.data

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper


// Database class to manage SQLite database
class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        const val DATABASE_NAME = "accountit.db"
        const val DATABASE_VERSION = 8

        // User Table
        const val TABLE_USERS = "users"
        const val COLUMN_USER_ID = "id"
        const val COLUMN_USER_NAME = "username"
        const val COLUMN_PASSWORD = "password"
        const val COLUMN_SECURITY_QUESTION = "security_question"
        const val COLUMN_SECURITY_ANSWER = "security_answer"
        const val COLUMN_CURRENCY = "currency"

        // Savings Table
        const val TABLE_SAVINGS = "savings"
        const val COLUMN_SAVING_ID = "id"
        const val COLUMN_SAVING_USER_ID = "user_id"
        const val COLUMN_AMOUNT = "amount"
        const val COLUMN_DESCRIPTION = "description"
        const val COLUMN_CATEGORY = "category"
        const val COLUMN_DATE = "date"
        const val COLUMN_TYPE = "type"
        const val COLUMN_TARGET_AMOUNT = "target_amount"
        const val COLUMN_TARGET_DATE = "target_date"
      
        // Budgets Table
        const val TABLE_BUDGETS = "budgets"
        const val COLUMN_BUDGET_ID = "id"
        const val COLUMN_USER_ID_FK = "user_id"  // Foreign key to users table
        const val COLUMN_TOTAL_BUDGET = "total_budget"
        const val COLUMN_INCOME = "income"

        // Categories Table
        const val TABLE_CATEGORIES = "categories"
        const val COLUMN_CATEGORY_ID = "id"
        const val COLUMN_CATEGORY_USER_ID_FK = "user_id"  // Foreign key to users table
        const val COLUMN_CATEGORY_NAME = "name"
        const val COLUMN_BUDGET_AMOUNT = "budget"  // Total budget for the category
        const val COLUMN_SPENT_AMOUNT = "spent"    // Amount spent in the category
        const val COLUMN_IS_PINNED = "is_pinned"

    }

    override fun onCreate(db: SQLiteDatabase) {
        // Create users table
        val createUserTable = """
            CREATE TABLE $TABLE_USERS (
                $COLUMN_USER_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_USER_NAME TEXT NOT NULL,
                $COLUMN_PASSWORD TEXT NOT NULL,
                $COLUMN_SECURITY_QUESTION TEXT NOT NULL,
                $COLUMN_SECURITY_ANSWER TEXT NOT NULL,
                $COLUMN_CURRENCY TEXT NOT NULL DEFAULT "ZAR"
            )
        """.trimIndent()

        db.execSQL(createUserTable)

        val createBudgetsTable = """
            CREATE TABLE $TABLE_BUDGETS (
                $COLUMN_BUDGET_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_USER_ID_FK INTEGER NOT NULL,
                $COLUMN_TOTAL_BUDGET REAL NOT NULL,
                $COLUMN_INCOME REAL NOT NULL,
                FOREIGN KEY ($COLUMN_USER_ID_FK) REFERENCES $TABLE_USERS($COLUMN_USER_ID) ON DELETE CASCADE
            )
        """
        db.execSQL(createBudgetsTable)

        // Create categories table
        val createCategoriesTable = """
            CREATE TABLE $TABLE_CATEGORIES (
                $COLUMN_CATEGORY_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_CATEGORY_USER_ID_FK INTEGER NOT NULL,
                $COLUMN_CATEGORY_NAME TEXT NOT NULL,
                $COLUMN_BUDGET_AMOUNT REAL NOT NULL,
                $COLUMN_SPENT_AMOUNT REAL NOT NULL DEFAULT 0,
                $COLUMN_IS_PINNED INTEGER NOT NULL DEFAULT 0,
                FOREIGN KEY ($COLUMN_CATEGORY_USER_ID_FK) REFERENCES $TABLE_USERS($COLUMN_USER_ID) ON DELETE CASCADE
            )
        """
        db.execSQL(createCategoriesTable)

        // Create savings table
        val createSavingsTable = """
            CREATE TABLE $TABLE_SAVINGS (
                $COLUMN_SAVING_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_SAVING_USER_ID INTEGER NOT NULL,
                $COLUMN_AMOUNT REAL NOT NULL,
                $COLUMN_DESCRIPTION TEXT NOT NULL,
                $COLUMN_CATEGORY TEXT,
                $COLUMN_DATE TEXT NOT NULL,
                $COLUMN_TYPE TEXT NOT NULL,
                $COLUMN_TARGET_AMOUNT REAL,
                $COLUMN_TARGET_DATE TEXT,
                FOREIGN KEY($COLUMN_SAVING_USER_ID) REFERENCES $TABLE_USERS($COLUMN_USER_ID)
            )
        """.trimIndent()

        db.execSQL(createSavingsTable)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        // Drop all existing tables if they exist
        db.execSQL("DROP TABLE IF EXISTS $TABLE_CATEGORIES")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_BUDGETS")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_SAVINGS")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_USERS")
        onCreate(db) // Recreate all tables
    }
    
}
