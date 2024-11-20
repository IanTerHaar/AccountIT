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
import com.ianterhaar.accountit.SavingsViewModel
import com.ianterhaar.accountit.SavingsViewModelFactory
import com.ianterhaar.accountit.models.SavingsGoal
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.filled.Remove
import com.ianterhaar.accountit.data.UserRepository

private val tealColor = Color(0xFF008080)
private val orangeColor = Color(0xFFFF5722)

@Composable
fun SavingsTrackingScreen(
    userId : Long,
    userRepository: UserRepository,
    viewModel: SavingsViewModel = viewModel(
        factory = SavingsViewModelFactory(LocalContext.current, userId)
    )

) {

    val totalSavings by viewModel.totalSavings.collectAsState()
    val savingsGoals by viewModel.savingsGoals.collectAsState()
    var showAddGoalDialog by remember { mutableStateOf(false) }
    var showAddSavingsDialog by remember { mutableStateOf(false) }
    var showTransactionHistory by remember { mutableStateOf(false) }
    var currencySymbol by remember { mutableStateOf("R") } // Default symbol
    currencySymbol = userRepository.getCurrency(userId.toInt()) ?: "R"


    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        TotalSavingsCard(
            totalSavings = totalSavings,
            currencySymbol = currencySymbol,
            onAddSavingsClick = { showAddSavingsDialog = true },
            onAddGoalClick = { showAddGoalDialog = true },
            onShowHistoryClick = { showTransactionHistory = true }
        )

        Text(
            text = "Savings Goals",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(vertical = 8.dp)
        )

        LazyColumn {
            items(savingsGoals) { goal ->
                SavingsGoalCard(
                    goal = goal,
                    currencySymbol = currencySymbol,
                    onAddSavings = { amount ->
                        viewModel.addSavings(goal.name, amount)
                    }
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }

    if (showAddGoalDialog) {
        AddSavingsGoalDialog(
            currencySymbol = currencySymbol,
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
            currencySymbol = currencySymbol,
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
            currencySymbol = currencySymbol,
            onDismiss = { showTransactionHistory = false }
        )
    }
}


@Composable
private fun TotalSavingsCard(
    totalSavings: Double,
    onAddSavingsClick: () -> Unit,
    onAddGoalClick: () -> Unit,
    onShowHistoryClick: () -> Unit,
    currencySymbol: String
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp),
        border = BorderStroke(1.dp, Color.LightGray)
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            // History icon in top right
            IconButton(
                onClick = onShowHistoryClick,
                modifier = Modifier.align(Alignment.TopEnd)
            ) {
                Icon(
                    Icons.Filled.History,
                    contentDescription = "History",
                    tint = tealColor
                )
            }

            // Main content
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Total Savings",
                    style = MaterialTheme.typography.headlineMedium, // Changed from titleMedium to headlineMedium
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "$currencySymbol$totalSavings",
                    style = MaterialTheme.typography.headlineLarge,
                    color = tealColor,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(24.dp))

                // Buttons row with more spacing and height
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp), // Reduced horizontal padding to allow more button width
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = onAddSavingsClick,
                        modifier = Modifier
                            .weight(1f)
                            .height(60.dp), // Increased from 48.dp to 56.dp
                        colors = ButtonDefaults.buttonColors(
                            containerColor = tealColor
                        ),
                        elevation = ButtonDefaults.buttonElevation(
                            defaultElevation = 2.dp
                        )
                    ) {
                        Icon(
                            Icons.Filled.ArrowUpward,
                            contentDescription = "Add",
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            "Add Savings",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Button(
                        onClick = onAddGoalClick,
                        modifier = Modifier
                            .weight(1f)
                            .height(56.dp), // Increased from 48.dp to 56.dp
                        colors = ButtonDefaults.buttonColors(
                            containerColor = orangeColor
                        ),
                        elevation = ButtonDefaults.buttonElevation(
                            defaultElevation = 2.dp
                        )
                    ) {
                        Icon(
                            Icons.Filled.Add,
                            contentDescription = "New Goal",
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            "New Goal",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SavingsGoalCard(
    goal: SavingsGoal,
    onAddSavings: (Double) -> Unit,
    currencySymbol: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        border = BorderStroke(1.dp, Color.LightGray)
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
                color = tealColor
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "$currencySymbol${goal.currentAmount} of $currencySymbol${goal.targetAmount}",
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = "${((goal.currentAmount / goal.targetAmount) * 100).toInt()}%",
                    style = MaterialTheme.typography.bodyMedium
                )
            }

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

@Composable
private fun AddSavingsGoalDialog(
    onDismiss: () -> Unit,
    currencySymbol: String,
    onAddGoal: (String, Double, String?) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var targetAmount by remember { mutableStateOf("") }
    var deadline by remember { mutableStateOf("") }
    var showErrors by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }

    // Add these date-related state variables
    val calendar = Calendar.getInstance()
    var selectedYear by remember { mutableStateOf(calendar.get(Calendar.YEAR)) }
    var selectedMonth by remember { mutableStateOf(calendar.get(Calendar.MONTH)) }
    var selectedDay by remember { mutableStateOf(calendar.get(Calendar.DAY_OF_MONTH)) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color.White,
        titleContentColor = tealColor,
        title = { Text("Add New Savings Goal", fontWeight = FontWeight.Bold) },
        text = {
            Column {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Goal Name") },
                    isError = showErrors && name.isBlank(),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = tealColor,
                        focusedLabelColor = tealColor,
                        cursorColor = tealColor,
                        errorBorderColor = orangeColor,
                        errorLabelColor = orangeColor
                    )
                )
                if (showErrors && name.isBlank()) {
                    Text(
                        text = "Name is required",
                        color = orangeColor,
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = targetAmount,
                    onValueChange = {
                        if (it.isEmpty() || it.matches(Regex("^\\d*\\.?\\d{0,2}$"))) {
                            targetAmount = it
                        }
                    },
                    label = { Text("Target Amount") },
                    prefix = { Text("$currencySymbol ") },
                    isError = showErrors && (targetAmount.toDoubleOrNull() ?: 0.0) <= 0,
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = tealColor,
                        focusedLabelColor = tealColor,
                        cursorColor = tealColor,
                        errorBorderColor = orangeColor,
                        errorLabelColor = orangeColor
                    )
                )
                if (showErrors && (targetAmount.toDoubleOrNull() ?: 0.0) <= 0) {
                    Text(
                        text = "Valid amount is required",
                        color = orangeColor,
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = if (deadline.isNotEmpty()) formatDate(deadline) else "",
                    onValueChange = { },
                    label = { Text("Deadline (Optional)") },
                    singleLine = true,
                    readOnly = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showDatePicker = true },
                    trailingIcon = {
                        IconButton(onClick = { showDatePicker = true }) {
                            Icon(
                                Icons.Default.DateRange,
                                contentDescription = "Select date",
                                tint = tealColor
                            )
                        }
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = tealColor,
                        focusedLabelColor = tealColor,
                        cursorColor = tealColor
                    )
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
                },
                colors = ButtonDefaults.buttonColors(containerColor = tealColor),
                enabled = name.isNotBlank() && targetAmount.isNotBlank()
            ) {
                Text("Add Goal")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                colors = ButtonDefaults.textButtonColors(contentColor = orangeColor)
            ) {
                Text("Cancel")
            }
        }
    )

    if (showDatePicker) {
        DatePickerDialog(
            onDismiss = { showDatePicker = false },
            onDateSelected = { date ->
                deadline = date
            }
        )
    }
}

@Composable
private fun DatePickerDialog(
    onDismiss: () -> Unit,
    onDateSelected: (String) -> Unit
) {
    val calendar = Calendar.getInstance()
    var selectedYear by remember { mutableStateOf(calendar.get(Calendar.YEAR)) }
    var selectedMonth by remember { mutableStateOf(calendar.get(Calendar.MONTH) + 1) }
    var selectedDay by remember { mutableStateOf(calendar.get(Calendar.DAY_OF_MONTH)) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color.White,
        title = {
            Text(
                "Select Deadline",
                fontWeight = FontWeight.Bold,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
        },
        text = {
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(24.dp),
                    modifier = Modifier.padding(vertical = 16.dp)
                ) {
                    // Year Picker
                    NumberPickerRow(
                        label = "Year",
                        value = selectedYear,
                        onValueChange = { selectedYear = it },
                        range = (calendar.get(Calendar.YEAR))..(calendar.get(Calendar.YEAR) + 10)
                    )

                    // Month Picker
                    NumberPickerRow(
                        label = "Month",
                        value = selectedMonth,
                        onValueChange = { selectedMonth = it },
                        range = 1..12,
                        displayTransform = { getMonthName(it) }
                    )

                    // Day Picker
                    NumberPickerRow(
                        label = "Day",
                        value = selectedDay,
                        onValueChange = { selectedDay = it },
                        range = 1..getDaysInMonth(selectedYear, selectedMonth)
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val date = String.format("%04d-%02d-%02d", selectedYear, selectedMonth, selectedDay)
                    onDateSelected(date)
                    onDismiss()
                }
            ) {
                Text("OK", color = tealColor)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = orangeColor)
            }
        }
    )
}

@Composable
private fun NumberPickerRow(
    label: String,
    value: Int,
    onValueChange: (Int) -> Unit,
    range: IntRange,
    displayTransform: (Int) -> String = { it.toString() }
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.width(200.dp)  // Fixed width for consistent centering
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceEvenly,
            modifier = Modifier.fillMaxWidth()
        ) {
            IconButton(
                onClick = {
                    if (value > range.first) onValueChange(value - 1)
                }
            ) {
                Icon(Icons.Filled.Remove, "Decrease", tint = tealColor)
            }

            Text(
                text = displayTransform(value),
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.widthIn(min = 100.dp),
                textAlign = TextAlign.Center
            )

            IconButton(
                onClick = {
                    if (value < range.last) onValueChange(value + 1)
                }
            ) {
                Icon(Icons.Filled.Add, "Increase", tint = tealColor)
            }
        }
    }
}

private fun getMonthName(month: Int): String {
    return when (month) {
        1 -> "January"
        2 -> "February"
        3 -> "March"
        4 -> "April"
        5 -> "May"
        6 -> "June"
        7 -> "July"
        8 -> "August"
        9 -> "September"
        10 -> "October"
        11 -> "November"
        12 -> "December"
        else -> ""
    }
}

private fun getDaysInMonth(year: Int, month: Int): Int {
    val calendar = Calendar.getInstance()
    calendar.set(Calendar.YEAR, year)
    calendar.set(Calendar.MONTH, month - 1)
    return calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddSavingsDialog(
    goals: List<SavingsGoal>,
    currencySymbol: String,
    onDismiss: () -> Unit,
    onAddSavings: (String, Double) -> Unit
) {
    var selectedGoal by remember { mutableStateOf(goals.firstOrNull()?.name ?: "") }
    var amount by remember { mutableStateOf("") }
    var expanded by remember { mutableStateOf(false) }
    var showErrors by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color.White,
        titleContentColor = tealColor,
        title = { Text("Add Savings", fontWeight = FontWeight.Bold) },
        text = {
            Column {
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded }
                ) {
                    OutlinedTextField(
                        value = selectedGoal,
                        onValueChange = { },
                        readOnly = true,
                        label = { Text("Select Goal") },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth(),
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = tealColor,
                            focusedLabelColor = tealColor,
                            cursorColor = tealColor
                        )
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
                OutlinedTextField(
                    value = amount,
                    onValueChange = {
                        if (it.isEmpty() || it.matches(Regex("^\\d*\\.?\\d{0,2}$"))) {
                            amount = it
                        }
                    },
                    label = { Text("Amount") },
                    prefix = { Text("$currencySymbol ") },
                    isError = showErrors && (amount.toDoubleOrNull() ?: 0.0) <= 0,
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = tealColor,
                        focusedLabelColor = tealColor,
                        cursorColor = tealColor,
                        errorBorderColor = orangeColor,
                        errorLabelColor = orangeColor
                    )
                )
                if (showErrors && (amount.toDoubleOrNull() ?: 0.0) <= 0) {
                    Text(
                        text = "Valid amount is required",
                        color = orangeColor,
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
                },
                colors = ButtonDefaults.buttonColors(containerColor = tealColor),
                enabled = selectedGoal.isNotBlank() && amount.isNotBlank()
            ) {
                Text("Add")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                colors = ButtonDefaults.textButtonColors(contentColor = orangeColor)
            ) {
                Text("Cancel")
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TransactionHistoryDialog(
    viewModel: SavingsViewModel,
    onDismiss: () -> Unit,
    currencySymbol: String
) {
    val transactions by viewModel.transactionHistory.collectAsState()

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color.White,
        titleContentColor = tealColor,
        title = { Text("Transaction History", fontWeight = FontWeight.Bold) },
        text = {
            LazyColumn {
                items(transactions) { transaction ->
                    Column {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(
                                    text = transaction.goalName,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = formatDate(transaction.date),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.Gray
                                )
                            }
                            Text(
                                text = currencySymbol + (transaction.amount),
                                style = MaterialTheme.typography.bodyMedium,
                                color = tealColor,
                                fontWeight = FontWeight.Medium
                            )
                        }
                        Divider(color = Color.LightGray)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = onDismiss,
                colors = ButtonDefaults.textButtonColors(contentColor = tealColor)
            ) {
                Text("Close")
            }
        }
    )
}

private fun calculateDaysRemaining(deadline: String): Long {
    val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    return try {
        val deadlineDate = dateFormat.parse(deadline)
        val today = Date()
        val diff = deadlineDate?.time?.minus(today.time) ?: 0
        diff / (1000 * 60 * 60 * 24) // Convert milliseconds to days
    } catch (e: Exception) {
        0L
    }
}

//private fun formatCurrency(amount: Double): String {
//    val format = NumberFormat.getCurrencyInstance(Locale("en", "ZA"))
//    return format.format(amount)
//}

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

// Extension function to help with numeric input validation
private fun String.isValidAmount(): Boolean {
    return try {
        this.toDoubleOrNull()?.let { it > 0 } ?: false
    } catch (e: NumberFormatException) {
        false
    }
}

// Extension function to help with date validation
private fun String.isValidDate(): Boolean {
    return try {
        if (this.isBlank()) return true // Optional dates are valid
        val format = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        format.isLenient = false
        format.parse(this)
        true
    } catch (e: Exception) {
        false
    }
}