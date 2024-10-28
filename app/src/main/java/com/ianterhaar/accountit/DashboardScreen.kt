package com.ianterhaar.accountit

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border


val TealColor = Color(0xFF008080)
val OrangeColor = Color(0xFFFF8C00)
val BlueColor = Color(0xFF1E90FF)    // Dodger Blue
val CardBorder = BorderStroke(1.dp, Color.Black)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    totalBudget: Double,
    income: Double,
    categories: List<BudgetCategory>,
    onAddIncomeClick: () -> Unit,
    onAddExpenseClick: () -> Unit,
    onSetBudgetClick: () -> Unit,
    onManageCategoriesClick: () -> Unit,
    onDeleteCategory: (String) -> Unit,
    onAddExpense: (String, Double) -> Unit
) {
    val remainingBudget = totalBudget - categories.sumOf { it.spent }
    var selectedCategory by remember { mutableStateOf<String?>(null) }
    var showExpenseDialog by remember { mutableStateOf(false) }
    var categoryToDelete by remember { mutableStateOf<String?>(null) }

    LazyColumn(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        item {
            // Budget Overview with improved spacing and text sizes
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = TealColor),
                border = CardBorder

            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        "Total Remaining: R$remainingBudget",
                        fontSize = 23.sp,
                        color = if (remainingBudget < 0) OrangeColor else Color.White,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        "Total Budget: R$totalBudget",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        "Total Income: R$income",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )

                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onSetBudgetClick,
                    colors = ButtonDefaults.buttonColors(containerColor = OrangeColor),
                    modifier = Modifier.weight(1f),
                ) {
                    Text("Set Budget")
                }
                Button(
                    onClick = onAddIncomeClick,
                    colors = ButtonDefaults.buttonColors(containerColor = BlueColor),
                    modifier = Modifier.weight(1f),
                ) {
                    Text("Manage Income")
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Pie Chart placeholder
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                BudgetPieChartWithLegend(
                    categories = categories,
                    modifier = Modifier.padding(16.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
            // Category Management Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),  // Added vertical padding
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Budget Categories",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = TealColor,
                    modifier = Modifier.padding(end = 16.dp)  // Added end padding to text
                )
                Button(
                    onClick = onManageCategoriesClick,
                    colors = ButtonDefaults.buttonColors(containerColor = TealColor),
                    modifier = Modifier.padding(start = 8.dp)  // Added start padding to button
                ) {
                    Text("Add Category")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))  // Increased space after the header
        }

        items(categories) { category ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .clickable {
                        selectedCategory = category.name
                        showExpenseDialog = true
                    },
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = CardBorder
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            category.name,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = TealColor
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            "Allocated: R${category.allocated}",
                            color = TealColor,
                            fontSize = 16.sp
                        )
                        Text(
                            "Spent: R${category.spent}",
                            color = OrangeColor,
                            fontSize = 16.sp
                        )
                        val remaining = category.allocated - category.spent
                        Text(
                            "Remaining: R$remaining",
                            color = if (remaining < 0) Color.Red else TealColor,
                            fontSize = 16.sp
                        )
                    }
                    IconButton(
                        onClick = { categoryToDelete = category.name }  // Instead of direct delete
                    ) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "Delete category",
                            tint = OrangeColor
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
        }
    }

    // Quick Add Expense Dialog when category is clicked
    if (showExpenseDialog && selectedCategory != null) {
        var expenseAmount by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = {
                showExpenseDialog = false
                selectedCategory = null
            },
            title = {
                Text(
                    "Add Expense to ${selectedCategory}",
                    fontWeight = FontWeight.Bold,
                    color = TealColor
                )
            },
            text = {
                Column {
                    OutlinedTextField(
                        value = expenseAmount,
                        onValueChange = { if (it.isEmpty() || it.matches(Regex("^\\d*\\.?\\d{0,2}$"))) expenseAmount = it },
                        label = { Text("Amount") },
                        prefix = { Text("R ") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = TealColor,
                            focusedLabelColor = TealColor,
                            cursorColor = TealColor
                        )
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        expenseAmount.toDoubleOrNull()?.let { amount ->
                            selectedCategory?.let { category ->
                                onAddExpense(category, amount)
                            }
                        }
                        showExpenseDialog = false
                        selectedCategory = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = TealColor)
                ) {
                    Text("Add Expense")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showExpenseDialog = false
                        selectedCategory = null
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = OrangeColor)
                ) {
                    Text("Cancel")
                }
            }
        )
    }
    // DELETE CONFIRMATION CHANGES - Add confirmation dialog
    if (categoryToDelete != null) {
        AlertDialog(
            onDismissRequest = { categoryToDelete = null },
            containerColor = Color.White,
            titleContentColor = TealColor,
            title = {
                Text(
                    "Delete Category",
                    fontWeight = FontWeight.Bold,
                    color = TealColor
                )
            },
            text = {
                Text(
                    text = "Are you sure you want to delete the category '$categoryToDelete'?",
                    color = Color.Black,
                    fontSize = 16.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        categoryToDelete?.let { onDeleteCategory(it) }
                        categoryToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { categoryToDelete = null },
                    colors = ButtonDefaults.textButtonColors(contentColor = TealColor)
                ) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun SetBudgetDialog(
    onDismiss: () -> Unit,
    onSetBudget: (Double) -> Unit
) {
    var budgetInput by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color.White,
        titleContentColor = TealColor,
        title = { Text("Set Monthly Budget", fontWeight = FontWeight.Bold) },
        text = {
            OutlinedTextField(
                value = budgetInput,
                onValueChange = { if (it.isEmpty() || it.matches(Regex("^\\d*\\.?\\d{0,2}$"))) budgetInput = it },
                label = { Text("Budget Amount") },
                prefix = { Text("R ") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = TealColor,
                    focusedLabelColor = TealColor,
                    cursorColor = TealColor
                )
            )
        },
        confirmButton = {
            Button(
                onClick = {
                    budgetInput.toDoubleOrNull()?.let { onSetBudget(it) }
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(containerColor = TealColor)
            ) {
                Text("Set Budget")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                colors = ButtonDefaults.textButtonColors(contentColor = OrangeColor)
            ) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun ManageIncomeDialog(
    currentIncome: Double,
    onDismiss: () -> Unit,
    onAddIncome: (Double) -> Unit,
    onResetIncome: () -> Unit
) {
    var incomeInput by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color.White,
        titleContentColor = TealColor,
        title = { Text("Manage Income", fontWeight = FontWeight.Bold) },
        text = {
            Column {
                Text(
                    "Current Total Income: R$currentIncome",
                    fontSize = 18.sp,
                    color = TealColor,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                OutlinedTextField(
                    value = incomeInput,
                    onValueChange = { if (it.isEmpty() || it.matches(Regex("^\\d*\\.?\\d{0,2}$"))) incomeInput = it },
                    label = { Text("Add Income Amount") },
                    prefix = { Text("R ") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = TealColor,
                        focusedLabelColor = TealColor,
                        cursorColor = TealColor
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = onResetIncome,
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Reset Income to 0")
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    incomeInput.toDoubleOrNull()?.let { onAddIncome(it) }
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(containerColor = OrangeColor)
            ) {
                Text("Add Income")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                colors = ButtonDefaults.textButtonColors(contentColor = TealColor)
            ) {
                Text("Cancel")
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManageCategoriesDialog(
    onDismiss: () -> Unit,
    onAddCategory: (String, Double) -> Unit
) {
    var newCategoryName by remember { mutableStateOf("") }
    var newCategoryBudget by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color.White,
        titleContentColor = TealColor,
        title = { Text("Add New Category", fontWeight = FontWeight.Bold) },
        text = {
            Column {
                OutlinedTextField(
                    value = newCategoryName,
                    onValueChange = { newCategoryName = it },
                    label = { Text("Category Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = TealColor,
                        focusedLabelColor = TealColor,
                        cursorColor = TealColor
                    )
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = newCategoryBudget,
                    onValueChange = { if (it.isEmpty() || it.matches(Regex("^\\d*\\.?\\d{0,2}$"))) newCategoryBudget = it },
                    label = { Text("Budget Amount") },
                    prefix = { Text("R ") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = TealColor,
                        focusedLabelColor = TealColor,
                        cursorColor = TealColor
                    )
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    newCategoryBudget.toDoubleOrNull()?.let {
                        onAddCategory(newCategoryName, it)
                        onDismiss()
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = TealColor),
                enabled = newCategoryName.isNotBlank() && newCategoryBudget.isNotBlank()
            ) {
                Text("Add Category")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                colors = ButtonDefaults.textButtonColors(contentColor = OrangeColor)
            ) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun BudgetPieChart(
    categories: List<BudgetCategory>,
    modifier: Modifier = Modifier
) {
    val categoriesWithSpending = categories.filter { it.spent > 0 }
    val totalSpent = categoriesWithSpending.sumOf { it.spent }

    Box(modifier = modifier) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
        ) {
            val canvasWidth = size.width
            val canvasHeight = size.height
            val radius = minOf(canvasWidth, canvasHeight) / 2f
            val center = Offset(canvasWidth / 2f, canvasHeight / 2f)

            if (categoriesWithSpending.isEmpty()) {
                // Draw empty state circle
                drawCircle(
                    color = Color.LightGray.copy(alpha = 0.3f),
                    radius = radius,
                    center = center
                )
            } else {
                var startAngle = -90f // Start from top
                categoriesWithSpending.forEach { category ->
                    val sweepAngle = (category.spent / totalSpent * 360f)
                    val color = when (category.name.lowercase()) {
                        "food" -> TealColor
                        "petrol" -> OrangeColor
                        else -> Color(0xFF2C7873)
                    }

                    drawArc(
                        color = color,
                        startAngle = startAngle,
                        sweepAngle = sweepAngle.toFloat(), // Cast to Float here
                        useCenter = true,
                        size = Size(radius * 2, radius * 2),
                        topLeft = Offset(center.x - radius, center.y - radius)
                    )

                    startAngle += sweepAngle.toFloat()
                }
            }
        }
    }
}

@Composable
fun BudgetPieChartWithLegend(
    categories: List<BudgetCategory>,
    modifier: Modifier = Modifier
) {
    val categoriesWithSpending = categories.filter { it.spent > 0 }

    Column(modifier = modifier) {
        BudgetPieChart(
            categories = categoriesWithSpending,
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
        )

        if (categoriesWithSpending.isEmpty()) {
            Text(
                "No expenses recorded",
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(16.dp),
                color = Color.Gray
            )
        } else {
            Spacer(modifier = Modifier.height(16.dp))

            // Legend
            categoriesWithSpending.forEach { category ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(12.dp)
                                .padding(2.dp)
                        ) {
                            Canvas(modifier = Modifier.fillMaxSize()) {
                                drawCircle(
                                    color = when (category.name.lowercase()) {              // Moet n better maneer kry om collers te asine
                                        "food" -> TealColor
                                        "petrol" -> OrangeColor
                                        else -> Color(0xFF2C7873)
                                    }
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = category.name,
                            color = TealColor,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    Text(
                        text = "R${String.format("%.2f", category.spent)}",
                        color = TealColor
                    )
                }
            }
        }
    }
}

data class BudgetCategory(
    val name: String,
    val allocated: Double,
    val spent: Double
)