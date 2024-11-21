package com.ianterhaar.accountit

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.ianterhaar.accountit.models.SavingsGoal
import com.ianterhaar.accountit.models.SavingsTransaction
import com.ianterhaar.accountit.data.DatabaseHelper
import com.ianterhaar.accountit.data.SavingsTrackerRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SavingsViewModel(
    private val repository: SavingsTrackerRepository,
    private val userId: Long
) : ViewModel() {
    private val _totalSavings = MutableStateFlow(0.0)
    val totalSavings: StateFlow<Double> = _totalSavings.asStateFlow()

    private val _savingsGoals = MutableStateFlow<List<SavingsGoal>>(emptyList())
    val savingsGoals: StateFlow<List<SavingsGoal>> = _savingsGoals.asStateFlow()

    private val _transactionHistory = MutableStateFlow<List<SavingsTransaction>>(emptyList())
    val transactionHistory: StateFlow<List<SavingsTransaction>> = _transactionHistory.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            try {
                _totalSavings.value = repository.getTotalSavings(userId)
                _savingsGoals.value = repository.getSavingsGoals(userId)
                loadTransactionHistory()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun loadTransactionHistory() {
        viewModelScope.launch {
            try {
                _transactionHistory.value = repository.getTransactions(userId)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun addSavingsGoal(name: String, targetAmount: Double, deadline: String?) {
        viewModelScope.launch {
            try {
                repository.addSavingsGoal(userId, name, targetAmount, deadline)
                loadData()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun addSavings(goalName: String, amount: Double) {
        viewModelScope.launch {
            try {
                repository.addSavingsAmount(userId, goalName, amount)
                loadData()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }


    fun deleteSavingsGoal(goalName: String) {
        viewModelScope.launch {
            try {
                repository.deleteSavingsGoal(userId, goalName)
                loadData()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}

class SavingsViewModelFactory(
    private val context: Context,
    private val userId: Long
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SavingsViewModel::class.java)) {
            val dbHelper = DatabaseHelper(context)
            val repository = SavingsTrackerRepository(dbHelper)
            return SavingsViewModel(repository, userId) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}