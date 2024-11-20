package com.ianterhaar.accountit.data

import android.content.ContentValues
import android.content.Context
import com.ianterhaar.accountit.BudgetCategory

class BudgetTrackingRepository(context: Context) {
    private val dbHelper = DatabaseHelper(context)

    private fun ensureBudgetExists(userId: Int) {
        val db = dbHelper.writableDatabase
        val cursor = db.query(
            DatabaseHelper.TABLE_BUDGETS,
            null,
            "${DatabaseHelper.COLUMN_USER_ID_FK} = ?",
            arrayOf(userId.toString()),
            null,
            null,
            null
        )

        if (!cursor.moveToFirst()) {
            // Create initial budget record if it doesn't exist
            val values = ContentValues().apply {
                put(DatabaseHelper.COLUMN_USER_ID_FK, userId)
                put(DatabaseHelper.COLUMN_TOTAL_BUDGET, 0.0)
                put(DatabaseHelper.COLUMN_INCOME, 0.0)
            }
            db.insert(DatabaseHelper.TABLE_BUDGETS, null, values)
        }
        cursor.close()
    }

    // Update your existing methods to call ensureBudgetExists
    fun getTotalBudget(userId: Int): Double {
        ensureBudgetExists(userId)
        val db = dbHelper.readableDatabase
        val cursor = db.rawQuery(
            "SELECT ${DatabaseHelper.COLUMN_TOTAL_BUDGET} FROM ${DatabaseHelper.TABLE_BUDGETS} WHERE ${DatabaseHelper.COLUMN_USER_ID_FK} = ?",
            arrayOf(userId.toString())
        )
        return if (cursor.moveToFirst()) {
            val totalBudget = cursor.getDouble(0)
            cursor.close()
            totalBudget
        } else {
            cursor.close()
            0.0
        }
    }

    // Update total budget for a specific user
    fun updateBudget(userId: Int, newBudget: Double) {
        val db = dbHelper.writableDatabase
        db.execSQL(
            "UPDATE ${DatabaseHelper.TABLE_BUDGETS} SET ${DatabaseHelper.COLUMN_TOTAL_BUDGET} = ? WHERE user_id = ?",
            arrayOf(newBudget, userId)
        )
    }

    // Add income for a specific user
    fun addIncome(userId: Int, newIncome: Double) {
        val db = dbHelper.writableDatabase
        db.execSQL(
            "UPDATE ${DatabaseHelper.TABLE_BUDGETS} SET ${DatabaseHelper.COLUMN_INCOME} = ${DatabaseHelper.COLUMN_INCOME} + ? WHERE user_id = ?",
            arrayOf(newIncome, userId)
        )
    }

    fun resetIncome(userId: Int) {
        val db = dbHelper.writableDatabase
        db.execSQL(
            "UPDATE ${DatabaseHelper.TABLE_BUDGETS} SET ${DatabaseHelper.COLUMN_INCOME} = 0.0 WHERE user_id = ?",
            arrayOf(userId.toString())
        )
    }

    // Add expense to a category for a specific user
    fun addExpense(userId: Int, category: String, expense: Double) {
        val db = dbHelper.writableDatabase
        db.execSQL(
            "UPDATE ${DatabaseHelper.TABLE_CATEGORIES} SET ${DatabaseHelper.COLUMN_SPENT_AMOUNT} = ${DatabaseHelper.COLUMN_SPENT_AMOUNT} + ? WHERE ${DatabaseHelper.COLUMN_CATEGORY_NAME} = ? AND user_id = ?",
            arrayOf(expense, category, userId)
        )
    }

    // Add a new category for a specific user
    fun addCategory(userId: Int, name: String, allocated: Double) {
        val db = dbHelper.writableDatabase
        db.execSQL(
            "INSERT INTO ${DatabaseHelper.TABLE_CATEGORIES} (${DatabaseHelper.COLUMN_CATEGORY_NAME}, ${DatabaseHelper.COLUMN_BUDGET_AMOUNT}, ${DatabaseHelper.COLUMN_SPENT_AMOUNT}, user_id) VALUES (?, ?, 0, ?)",
            arrayOf(name, allocated, userId)
        )
    }

    // Delete a category for a specific user
    fun deleteCategory(userId: Int, name: String) {
        val db = dbHelper.writableDatabase
        db.execSQL(
            "DELETE FROM ${DatabaseHelper.TABLE_CATEGORIES} WHERE ${DatabaseHelper.COLUMN_CATEGORY_NAME} = ? AND user_id = ?",
            arrayOf(name, userId)
        )
    }
    fun getCategories(userId: Int): List<BudgetCategory> {
        val db = dbHelper.readableDatabase
        val categories = mutableListOf<BudgetCategory>()
        val cursor = db.rawQuery(
            """
            SELECT 
                ${DatabaseHelper.COLUMN_CATEGORY_NAME}, 
                ${DatabaseHelper.COLUMN_BUDGET_AMOUNT}, 
                ${DatabaseHelper.COLUMN_SPENT_AMOUNT},
                ${DatabaseHelper.COLUMN_IS_PINNED}
            FROM ${DatabaseHelper.TABLE_CATEGORIES} 
            WHERE user_id = ?
            ORDER BY ${DatabaseHelper.COLUMN_IS_PINNED} DESC, ${DatabaseHelper.COLUMN_CATEGORY_NAME} ASC
            """,
            arrayOf(userId.toString())
        )
        while (cursor.moveToNext()) {
            val name = cursor.getString(0)
            val allocated = cursor.getDouble(1)
            val spent = cursor.getDouble(2)
            val isPinned = cursor.getInt(3) == 1
            categories.add(BudgetCategory(name, allocated, spent, isPinned))
        }
        cursor.close()
        return categories
    }

    // Add new function to toggle pin status
    fun toggleCategoryPin(userId: Int, categoryName: String) {
        val db = dbHelper.writableDatabase
        db.execSQL(
            """
            UPDATE ${DatabaseHelper.TABLE_CATEGORIES} 
            SET ${DatabaseHelper.COLUMN_IS_PINNED} = CASE 
                WHEN ${DatabaseHelper.COLUMN_IS_PINNED} = 1 THEN 0 
                ELSE 1 
            END
            WHERE ${DatabaseHelper.COLUMN_CATEGORY_NAME} = ? 
            AND user_id = ?
            """,
            arrayOf(categoryName, userId)
        )
    }

    fun getIncome(userId: Int): Double {
        val db = dbHelper.readableDatabase
        val cursor = db.rawQuery(
            "SELECT ${DatabaseHelper.COLUMN_INCOME} FROM ${DatabaseHelper.TABLE_BUDGETS} WHERE user_id = ?",
            arrayOf(userId.toString())
        )
        return if (cursor.moveToFirst()) {
            val income = cursor.getDouble(0)
            cursor.close()
            income
        } else {
            cursor.close()
            0.0
        }
    }
}

