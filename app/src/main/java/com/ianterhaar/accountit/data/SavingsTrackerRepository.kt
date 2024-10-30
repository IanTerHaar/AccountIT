package com.ianterhaar.accountit.data

import android.content.ContentValues
import java.util.Date

data class SavingsTransaction(
    val id: Long? = null,
    val userId: Long,
    val amount: Double,
    val description: String,
    val category: String,
    val date: String,
    val type: String // "income" or "expense"
)

class SavingsTrackerRepository(private val dbHelper: DatabaseHelper) {

    fun addTransaction(transaction: SavingsTransaction) {
        val db = dbHelper.writableDatabase
        val values = ContentValues().apply {
            put(DatabaseHelper.COLUMN_SAVING_USER_ID, transaction.userId)
            put(DatabaseHelper.COLUMN_AMOUNT, transaction.amount)
            put(DatabaseHelper.COLUMN_DESCRIPTION, transaction.description)
            put(DatabaseHelper.COLUMN_CATEGORY, transaction.category)
            put(DatabaseHelper.COLUMN_DATE, transaction.date)
            put(DatabaseHelper.COLUMN_TYPE, transaction.type)
        }
        db.insert(DatabaseHelper.TABLE_SAVINGS, null, values)
    }

    fun getTransactions(userId: Long, startDate: String? = null, endDate: String? = null): List<SavingsTransaction> {
        val db = dbHelper.readableDatabase
        val transactions = mutableListOf<SavingsTransaction>()

        var selection = "${DatabaseHelper.COLUMN_SAVING_USER_ID} = ? AND ${DatabaseHelper.COLUMN_TYPE} IN (?, ?)"
        var selectionArgs = arrayOf(userId.toString(), "income", "expense")

        if (startDate != null && endDate != null) {
            selection += " AND ${DatabaseHelper.COLUMN_DATE} BETWEEN ? AND ?"
            selectionArgs = selectionArgs.plus(arrayOf(startDate, endDate))
        }

        val cursor = db.query(
            DatabaseHelper.TABLE_SAVINGS,
            null,
            selection,
            selectionArgs,
            null,
            null,
            "${DatabaseHelper.COLUMN_DATE} DESC"
        )

        cursor.use {
            while (it.moveToNext()) {
                transactions.add(
                    SavingsTransaction(
                        id = it.getLong(it.getColumnIndexOrThrow(DatabaseHelper.COLUMN_SAVING_ID)),
                        userId = it.getLong(it.getColumnIndexOrThrow(DatabaseHelper.COLUMN_SAVING_USER_ID)),
                        amount = it.getDouble(it.getColumnIndexOrThrow(DatabaseHelper.COLUMN_AMOUNT)),
                        description = it.getString(it.getColumnIndexOrThrow(DatabaseHelper.COLUMN_DESCRIPTION)),
                        category = it.getString(it.getColumnIndexOrThrow(DatabaseHelper.COLUMN_CATEGORY)),
                        date = it.getString(it.getColumnIndexOrThrow(DatabaseHelper.COLUMN_DATE)),
                        type = it.getString(it.getColumnIndexOrThrow(DatabaseHelper.COLUMN_TYPE))
                    )
                )
            }
        }
        return transactions
    }

    fun getTransactionsByCategory(userId: Long, startDate: String? = null, endDate: String? = null): Map<String, Double> {
        val db = dbHelper.readableDatabase
        val categoryTotals = mutableMapOf<String, Double>()

        var selection = "${DatabaseHelper.COLUMN_SAVING_USER_ID} = ? AND ${DatabaseHelper.COLUMN_TYPE} IN (?, ?)"
        var selectionArgs = arrayOf(userId.toString(), "income", "expense")

        if (startDate != null && endDate != null) {
            selection += " AND ${DatabaseHelper.COLUMN_DATE} BETWEEN ? AND ?"
            selectionArgs = selectionArgs.plus(arrayOf(startDate, endDate))
        }

        val cursor = db.query(
            DatabaseHelper.TABLE_SAVINGS,
            arrayOf(
                DatabaseHelper.COLUMN_CATEGORY,
                "SUM(${DatabaseHelper.COLUMN_AMOUNT}) as total"
            ),
            selection,
            selectionArgs,
            DatabaseHelper.COLUMN_CATEGORY,
            null,
            "total DESC"
        )

        cursor.use {
            while (it.moveToNext()) {
                val category = it.getString(0)
                val total = it.getDouble(1)
                categoryTotals[category] = total
            }
        }
        return categoryTotals
    }

    fun getIncomeVsExpenses(userId: Long, startDate: String? = null, endDate: String? = null): Pair<Double, Double> {
        val db = dbHelper.readableDatabase

        var selection = "${DatabaseHelper.COLUMN_SAVING_USER_ID} = ? AND ${DatabaseHelper.COLUMN_TYPE} = ?"
        var baseSelectionArgs = arrayOf(userId.toString())

        if (startDate != null && endDate != null) {
            selection += " AND ${DatabaseHelper.COLUMN_DATE} BETWEEN ? AND ?"
            baseSelectionArgs = baseSelectionArgs.plus(arrayOf(startDate, endDate))
        }

        // Get total income
        val incomeCursor = db.query(
            DatabaseHelper.TABLE_SAVINGS,
            arrayOf("SUM(${DatabaseHelper.COLUMN_AMOUNT}) as total"),
            selection,
            baseSelectionArgs.plus("income"),
            null,
            null,
            null
        )

        // Get total expenses
        val expensesCursor = db.query(
            DatabaseHelper.TABLE_SAVINGS,
            arrayOf("SUM(${DatabaseHelper.COLUMN_AMOUNT}) as total"),
            selection,
            baseSelectionArgs.plus("expense"),
            null,
            null,
            null
        )

        var totalIncome = 0.0
        var totalExpenses = 0.0

        incomeCursor.use {
            if (it.moveToFirst()) {
                totalIncome = it.getDouble(0)
            }
        }

        expensesCursor.use {
            if (it.moveToFirst()) {
                totalExpenses = it.getDouble(0)
            }
        }

        return Pair(totalIncome, totalExpenses)
    }

    fun deleteTransaction(transactionId: Long) {
        val db = dbHelper.writableDatabase
        db.delete(
            DatabaseHelper.TABLE_SAVINGS,
            "${DatabaseHelper.COLUMN_SAVING_ID} = ?",
            arrayOf(transactionId.toString())
        )
    }

    fun updateTransaction(transaction: SavingsTransaction) {
        val db = dbHelper.writableDatabase
        val values = ContentValues().apply {
            put(DatabaseHelper.COLUMN_AMOUNT, transaction.amount)
            put(DatabaseHelper.COLUMN_DESCRIPTION, transaction.description)
            put(DatabaseHelper.COLUMN_CATEGORY, transaction.category)
            put(DatabaseHelper.COLUMN_DATE, transaction.date)
            put(DatabaseHelper.COLUMN_TYPE, transaction.type)
        }

        db.update(
            DatabaseHelper.TABLE_SAVINGS,
            values,
            "${DatabaseHelper.COLUMN_SAVING_ID} = ?",
            arrayOf(transaction.id.toString())
        )
    }
}

// ViewModel for SavingsTracker
class SavingsTrackerViewModel(
    private val repository: SavingsTrackerRepository,
    private val userId: Long
) : ViewModel() {

    private val _transactions = MutableStateFlow<List<SavingsTransaction>>(emptyList())
    val transactions: StateFlow<List<SavingsTransaction>> = _transactions

    private val _categoryTotals = MutableStateFlow<Map<String, Double>>(emptyMap())
    val categoryTotals: StateFlow<Map<String, Double>> = _categoryTotals

    private val _incomeVsExpenses = MutableStateFlow<Pair<Double, Double>>(Pair(0.0, 0.0))
    val incomeVsExpenses: StateFlow<Pair<Double, Double>> = _incomeVsExpenses

    init {
        loadData()
    }

    private fun loadData(startDate: String? = null, endDate: String? = null) {
        viewModelScope.launch(Dispatchers.IO) {
            _transactions.value = repository.getTransactions(userId, startDate, endDate)
            _categoryTotals.value = repository.getTransactionsByCategory(userId, startDate, endDate)
            _incomeVsExpenses.value = repository.getIncomeVsExpenses(userId, startDate, endDate)
        }
    }

    fun addTransaction(
        amount: Double,
        description: String,
        category: String,
        type: String
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            val transaction = SavingsTransaction(
                userId = userId,
                amount = amount,
                description = description,
                category = category,
                date = Date().toString(),
                type = type
            )
            repository.addTransaction(transaction)
            loadData()
        }
    }

    fun deleteTransaction(transactionId: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteTransaction(transactionId)
            loadData()
        }
    }

    fun updateTransaction(transaction: SavingsTransaction) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.updateTransaction(transaction)
            loadData()
        }
    }

    fun filterByDateRange(startDate: String, endDate: String) {
        loadData(startDate, endDate)
    }
}

// ViewModel Factory for SavingsTracker
class SavingsTrackerViewModelFactory(
    private val context: Context,
    private val userId: Long
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SavingsTrackerViewModel::class.java)) {
            val repository = SavingsTrackerRepository(DatabaseHelper(context))
            @Suppress("UNCHECKED_CAST")
            return SavingsTrackerViewModel(repository, userId) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}