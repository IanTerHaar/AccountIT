//SQLiteOpenHelper.kt
package com.ianterhaar.accountit.data

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.util.Date

// Database class to manage SQLite database
class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        const val DATABASE_NAME = "accountit.db"
        const val DATABASE_VERSION = 5

        // User Table
        const val TABLE_USERS = "users"
        const val COLUMN_USER_ID = "id"
        const val COLUMN_USER_NAME = "username"
        const val COLUMN_PASSWORD = "password"
        const val COLUMN_SECURITY_QUESTION = "security_question"
        const val COLUMN_SECURITY_ANSWER = "security_answer"

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
                $COLUMN_SECURITY_ANSWER TEXT NOT NULL
            )
        """.trimIndent()

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

        db.execSQL(createUserTable)

        db.execSQL(createSavingsTable)
    }

        // Create budgets table
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
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        // Drop all existing tables if they exist
        db.execSQL("DROP TABLE IF EXISTS $TABLE_CATEGORIES")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_BUDGETS")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_SAVINGS")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_USERS")
        onCreate(db) // Recreate all tables
    }

    fun getUserTotalSavings(userId: Long): Double {
        val db = readableDatabase
        return db.query(
            TABLE_SAVINGS,
            arrayOf("SUM($COLUMN_AMOUNT) as total"),
            "$COLUMN_SAVING_USER_ID = ? AND $COLUMN_TYPE = ?",
            arrayOf(userId.toString(), "deposit"),
            null,
            null,
            null
        ).use { cursor ->
            if (cursor.moveToFirst()) {
                cursor.getDouble(0)
            } else {
                0.0
            }
        }
    }
}

data class SavingsGoal(
    val name: String,
    val targetAmount: Double,
    val currentAmount: Double,
    val deadline: String?
)

// Repository class
class SavingsRepository(private val dbHelper: DatabaseHelper) {

    fun addSavingsGoal(userId: Long, name: String, targetAmount: Double, deadline: String?) {
        val db = dbHelper.writableDatabase
        val values = ContentValues().apply {
            put(DatabaseHelper.COLUMN_SAVING_USER_ID, userId)
            put(DatabaseHelper.COLUMN_AMOUNT, 0.0)  // Initial amount is 0
            put(DatabaseHelper.COLUMN_DESCRIPTION, name)
            put(DatabaseHelper.COLUMN_TYPE, "goal")
            put(DatabaseHelper.COLUMN_TARGET_AMOUNT, targetAmount)
            put(DatabaseHelper.COLUMN_TARGET_DATE, deadline)
            put(DatabaseHelper.COLUMN_DATE, Date().toString())
        }
        db.insert(DatabaseHelper.TABLE_SAVINGS, null, values)
    }

    fun addSavingsAmount(userId: Long, goalName: String, amount: Double) {
        val db = dbHelper.writableDatabase
        val values = ContentValues().apply {
            put(DatabaseHelper.COLUMN_SAVING_USER_ID, userId)
            put(DatabaseHelper.COLUMN_AMOUNT, amount)
            put(DatabaseHelper.COLUMN_DESCRIPTION, goalName)
            put(DatabaseHelper.COLUMN_TYPE, "deposit")
            put(DatabaseHelper.COLUMN_DATE, Date().toString())
        }
        db.insert(DatabaseHelper.TABLE_SAVINGS, null, values)
    }

    fun getSavingsGoals(userId: Long): List<SavingsGoal> {
        val db = dbHelper.readableDatabase
        val goals = mutableMapOf<String, SavingsGoal>()

        // First get all goals
        val goalsCursor = db.query(
            DatabaseHelper.TABLE_SAVINGS,
            null,
            "${DatabaseHelper.COLUMN_SAVING_USER_ID} = ? AND ${DatabaseHelper.COLUMN_TYPE} = ?",
            arrayOf(userId.toString(), "goal"),
            null,
            null,
            null
        )

        goalsCursor.use { cursor ->
            while (cursor.moveToNext()) {
                val name = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_DESCRIPTION))
                val targetAmount = cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_TARGET_AMOUNT))
                val deadline = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_TARGET_DATE))
                goals[name] = SavingsGoal(name, targetAmount, 0.0, deadline)
            }
        }

        // Then get all deposits for each goal
        goals.keys.forEach { goalName ->
            val depositsCursor = db.query(
                DatabaseHelper.TABLE_SAVINGS,
                arrayOf("SUM(${DatabaseHelper.COLUMN_AMOUNT}) as total"),
                "${DatabaseHelper.COLUMN_SAVING_USER_ID} = ? AND ${DatabaseHelper.COLUMN_DESCRIPTION} = ? AND ${DatabaseHelper.COLUMN_TYPE} = ?",
                arrayOf(userId.toString(), goalName, "deposit"),
                null,
                null,
                null
            )

            depositsCursor.use { cursor ->
                if (cursor.moveToFirst()) {
                    val currentAmount = cursor.getDouble(0)
                    goals[goalName] = goals[goalName]!!.copy(currentAmount = currentAmount)
                }
            }
        }

        return goals.values.toList()
    }

    fun getTotalSavings(userId: Long): Double {
        return dbHelper.getUserTotalSavings(userId)
    }
}