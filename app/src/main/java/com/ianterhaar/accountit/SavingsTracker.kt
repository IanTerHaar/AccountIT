package com.ianterhaar.accountit.ui.savings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*
import androidx.compose.runtime.collectAsState
import android.content.Context
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch

data class SavingsGoal(
    val name: String,
    val targetAmount: Double,
    val currentAmount: Double,
    val deadline: String? = null
)

@Composable
fun SavingsTrackingScreen(
    viewModel: SavingsViewModel = viewModel(
        factory = SavingsViewModelFactory(LocalContext.current, /* userId = */ 1L) // Replace with actual user ID
    )
) {
    val totalSavings by viewModel.totalSavings.collectAsState()
    val savingsGoals by viewModel.savingsGoals.collectAsState()
    var showAddGoalDialog by remember { mutableStateOf(false) }
    var showAddSavingsDialog by remember { mutableStateOf(false) }
    var showTransactionHistory by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Total Savings Card
        TotalSavingsCard(
            totalSavings = totalSavings,
            onAddSavingsClick = { showAddSavingsDialog = true },
            onAddGoalClick = { showAddGoalDialog = true },
            onShowHistoryClick = { showTransactionHistory = true }
        )

        // Savings Goals List
        Text(
            text = "Savings Goals",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(vertical = 8.dp)
        )

        LazyColumn {
            items(savingsGoals) { goal ->
                SavingsGoalCard(
                    goal = goal,
                    onAddSavings = { amount ->
                        viewModel.addSavings(goal.name, amount)
                    }
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }

    // Dialogs
    if (showAddGoalDialog) {
        AddSavingsGoalDialog(
            onDismiss = { showAddGoalDialog = false },
            onAddGoal = { name, target, deadline ->
                viewModel.addSavingsGoal(name, target, deadline)
                showAddGoalDialog = false
            }
        )
    }

    if (showAddSavingsDialog) {
        AddSavingsDialog(
            goals = savingsGoals,
            onDismiss = { showAddSavingsDialog = false },
            onAddSavings = { goalName, amount ->
                viewModel.addSavings(goalName, amount)
                showAddSavingsDialog = false
            }
        )
    }

    if (showTransactionHistory) {
        TransactionHistoryDialog(
            viewModel = viewModel,
            onDismiss = { showTransactionHistory = false }
        )
    }
}

@Composable
fun TotalSavingsCard(
    totalSavings: Double,
    onAddSavingsClick: () -> Unit,
    onAddGoalClick: () -> Unit,
    onShowHistoryClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Total Savings",
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = formatCurrency(totalSavings),
                style = MaterialTheme.typography.headlineMedium,
                color = Color(0xFF008080)
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Button(
                    onClick = onAddSavingsClick,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF008080)
                    )
                ) {
                    Icon(Icons.Default.ArrowUpward, contentDescription = "Add")
                    Spacer(Modifier.width(4.dp))
                    Text("Add Savings")
                }
                Button(
                    onClick = onAddGoalClick,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF008080)
                    )
                ) {
                    Icon(Icons.Default.Add, contentDescription = "New Goal")
                    Spacer(Modifier.width(4.dp))
                    Text("New Goal")
                }
                IconButton(onClick = onShowHistoryClick) {
                    Icon(Icons.Default.History, contentDescription = "History")
                }
            }
        }
    }
}

@Composable
fun SavingsGoalCard(
    goal: SavingsGoal,
    onAddSavings: (Double) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = goal.name,
                    style = MaterialTheme.typography.titleMedium
                )
                if (goal.deadline != null) {
                    Text(
                        text = "Due: ${formatDate(goal.deadline)}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            LinearProgressIndicator(
                progress = (goal.currentAmount / goal.targetAmount).toFloat().coerceIn(0f, 1f),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp),
                color = Color(0xFF008080)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "${formatCurrency(goal.currentAmount)} of ${formatCurrency(goal.targetAmount)}",
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = "${((goal.currentAmount / goal.targetAmount) * 100).toInt()}%",
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            // Time remaining if deadline exists
            goal.deadline?.let { deadline ->
                val daysRemaining = calculateDaysRemaining(deadline)
                if (daysRemaining > 0) {
                    Text(
                        text = "$daysRemaining days remaining",
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.End
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionHistoryDialog(
    viewModel: SavingsViewModel,
    onDismiss: () -> Unit
) {
    val transactions by viewModel.transactionHistory.collectAsState()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Transaction History") },
        text = {
            LazyColumn {
                items(transactions) { transaction ->
                    TransactionItem(transaction)
                    Divider()
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}

@Composable
fun TransactionItem(transaction: SavingsTransaction) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Text(
                text = transaction.goalName,
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = formatDate(transaction.date),
                style = MaterialTheme.typography.bodySmall
            )
        }
        Text(
            text = formatCurrency(transaction.amount),
            style = MaterialTheme.typography.bodyMedium,
            color = Color(0xFF008080)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddSavingsGoalDialog(
    onDismiss: () -> Unit,
    onAddGoal: (String, Double, String?) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var targetAmount by remember { mutableStateOf("") }
    var deadline by remember { mutableStateOf("") }
    var showErrors by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add New Savings Goal") },
        text = {
            Column {
                TextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Goal Name") },
                    isError = showErrors && name.isBlank(),
                    modifier = Modifier.fillMaxWidth()
                )
                if (showErrors && name.isBlank()) {
                    Text(
                        text = "Name is required",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))
                TextField(
                    value = targetAmount,
                    onValueChange = { targetAmount = it },
                    label = { Text("Target Amount") },
                    isError = showErrors && (targetAmount.toDoubleOrNull() ?: 0.0) <= 0,
                    modifier = Modifier.fillMaxWidth()
                )
                if (showErrors && (targetAmount.toDoubleOrNull() ?: 0.0) <= 0) {
                    Text(
                        text = "Valid amount is required",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))
                TextField(
                    value = deadline,
                    onValueChange = { deadline = it },
                    label = { Text("Deadline (YYYY-MM-DD, optional)") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    showErrors = true
                    val amount = targetAmount.toDoubleOrNull() ?: 0.0
                    if (name.isNotBlank() && amount > 0) {
                        val deadlineValue = deadline.takeIf { it.isNotBlank() }
                        onAddGoal(name, amount, deadlineValue)
                    }
                }
            ) {
                Text("Add Goal")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddSavingsDialog(
    goals: List<SavingsGoal>,
    onDismiss: () -> Unit,
    onAddSavings: (String, Double) -> Unit
) {
    var selectedGoal by remember { mutableStateOf(goals.firstOrNull()?.name ?: "") }
    var amount by remember { mutableStateOf("") }
    var expanded by remember { mutableStateOf(false) }
    var showErrors by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Savings") },
        text = {
            Column {
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded }
                ) {
                    TextField(
                        value = selectedGoal,
                        onValueChange = { },
                        readOnly = true,
                        label = { Text("Select Goal") },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth(),
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) }
                    )
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        goals.forEach { goal ->
                            DropdownMenuItem(
                                text = { Text(goal.name) },
                                onClick = {
                                    selectedGoal = goal.name
                                    expanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                TextField(
                    value = amount,
                    onValueChange = { amount = it },
                    label = { Text("Amount") },
                    isError = showErrors && (amount.toDoubleOrNull() ?: 0.0) <= 0,
                    modifier = Modifier.fillMaxWidth()
                )
                if (showErrors && (amount.toDoubleOrNull() ?: 0.0) <= 0) {
                    Text(
                        text = "Valid amount is required",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    showErrors = true
                    val savingsAmount = amount.toDoubleOrNull() ?: 0.0
                    if (savingsAmount > 0 && selectedGoal.isNotBlank()) {
                        onAddSavings(selectedGoal, savingsAmount)
                    }
                }
            ) {
                Text("Add")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

private fun formatCurrency(amount: Double): String {
    val format = NumberFormat.getCurrencyInstance(Locale("en", "ZA"))
    return format.format(amount)
}

private fun formatDate(dateString: String): String {
    val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    val outputFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
    return try {
        val date = inputFormat.parse(dateString)
        outputFormat.format(date!!)
    } catch (e: Exception) {
        dateString
    }
}

private fun calculateDaysRemaining(deadline: String): Long {
    val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.get