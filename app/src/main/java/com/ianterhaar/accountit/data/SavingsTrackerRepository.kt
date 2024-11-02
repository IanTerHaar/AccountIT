package com.ianterhaar.accountit.data

import android.content.ContentValues
import com.ianterhaar.accountit.models.SavingsGoal
import com.ianterhaar.accountit.models.SavingsTransaction
import java.util.Date

class SavingsTrackerRepository(private val dbHelper: DatabaseHelper) {

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
        val db = dbHelper.readableDatabase
        return db.query(
            DatabaseHelper.TABLE_SAVINGS,
            arrayOf("SUM(${DatabaseHelper.COLUMN_AMOUNT}) as total"),
            "${DatabaseHelper.COLUMN_SAVING_USER_ID} = ? AND ${DatabaseHelper.COLUMN_TYPE} = ?",
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

    fun getTransactions(userId: Long): List<SavingsTransaction> {
        val db = dbHelper.readableDatabase
        val cursor = db.query(
            DatabaseHelper.TABLE_SAVINGS,
            null,
            "${DatabaseHelper.COLUMN_SAVING_USER_ID} = ? AND ${DatabaseHelper.COLUMN_TYPE} = ?",
            arrayOf(userId.toString(), "deposit"),
            null,
            null,
            "${DatabaseHelper.COLUMN_DATE} DESC"
        )

        val transactions = mutableListOf<SavingsTransaction>()

        cursor.use {
            while (it.moveToNext()) {
                transactions.add(
                    SavingsTransaction(
                        goalName = it.getString(it.getColumnIndexOrThrow(DatabaseHelper.COLUMN_DESCRIPTION)),
                        amount = it.getDouble(it.getColumnIndexOrThrow(DatabaseHelper.COLUMN_AMOUNT)),
                        date = it.getString(it.getColumnIndexOrThrow(DatabaseHelper.COLUMN_DATE))
                    )
                )
            }
        }

        return transactions
    }
}