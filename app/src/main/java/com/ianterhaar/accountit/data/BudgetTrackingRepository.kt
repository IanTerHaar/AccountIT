package com.ianterhaar.accountit.data

import android.content.ContentValues
import android.content.Context
import com.ianterhaar.accountit.BudgetCategory
import com.ianterhaar.accountit.data.DatabaseHelper.Companion.TABLE_CATEGORIES

class BudgetTrackingRepository(context: Context) {
    private val dbHelper = DatabaseHelper(context)

    // Retrieve total budget for a specific user
    fun getTotalBudget(userId: Int): Double {
        val db = dbHelper.readableDatabase
        val cursor = db.rawQuery(
            "SELECT ${DatabaseHelper.COLUMN_TOTAL_BUDGET} FROM ${DatabaseHelper.TABLE_BUDGETS} WHERE user_id = ?",
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

    // Retrieve income for a specific user
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

    // Retrieve categories for a specific user
    fun getCategories(userId: Int): List<BudgetCategory> {
        val db = dbHelper.readableDatabase
        val categories = mutableListOf<BudgetCategory>()
        val cursor = db.rawQuery(
            "SELECT ${DatabaseHelper.COLUMN_CATEGORY_NAME}, ${DatabaseHelper.COLUMN_BUDGET_AMOUNT}, ${DatabaseHelper.COLUMN_SPENT_AMOUNT} FROM ${DatabaseHelper.TABLE_CATEGORIES} WHERE user_id = ?",
            arrayOf(userId.toString())
        )
        while (cursor.moveToNext()) {
            val name = cursor.getString(0)
            val allocated = cursor.getDouble(1)
            val spent = cursor.getDouble(2)
            categories.add(BudgetCategory(name, allocated, spent))
        }
        cursor.close()
        return categories
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
}

