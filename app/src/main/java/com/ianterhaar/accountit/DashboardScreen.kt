
package com.ianterhaar.accountit

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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

val TealColor = Color(0xFF008080)
val OrangeColor = Color(0xFFFF8C00)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    totalBudget: Double,
    income: Double,
    categories: List<BudgetCategory>,
    onAddIncomeClick: () -> Unit,
    onAddExpenseClick: () -> Unit,
    onSetBudgetClick: () -> Unit,
    onManageCategoriesClick: () -> Unit
) {
    val remainingBudget = totalBudget - categories.sumOf { it.spent }

    LazyColumn(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        item {
            // Budget Overview
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = TealColor)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Total Budget: R$totalBudget", fontWeight = FontWeight.Bold, color = Color.White)
                    Text("Total Income: R$income", color = Color.White)
                    Text("Remaining: R$remainingBudget",
                        color = if (remainingBudget < 0) OrangeColor else Color.White)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Action Buttons
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Button(onClick = onSetBudgetClick, colors = ButtonDefaults.buttonColors(containerColor = TealColor)) {
                    Text("Set Budget")
                }
                Button(onClick = onAddIncomeClick, colors = ButtonDefaults.buttonColors(containerColor = OrangeColor)) {
                    Text("Add Income")
                }
                Button(onClick = onAddExpenseClick, colors = ButtonDefaults.buttonColors(containerColor = OrangeColor)) {
                    Text("Add Expense")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Pie Chart placeholder
            Card(
                modifier = Modifier.fillMaxWidth().height(200.dp),
                colors = CardDefaults.cardColors(containerColor = TealColor)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text("Pie Chart Placeholder", color = Color.White)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Category Management
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Budget Categories", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TealColor)
                Button(onClick = onManageCategoriesClick, colors = ButtonDefaults.buttonColors(containerColor = TealColor)) {
                    Text("Manage Categories")
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
        }

        items(categories) { category ->
            CategoryItem(category)
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
fun CategoryItem(category: BudgetCategory) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(category.name, fontWeight = FontWeight.Bold, color = TealColor)
            Spacer(modifier = Modifier.height(4.dp))
            Text("Allocated: R${category.allocated}", color = TealColor)
            Text("Spent: R${category.spent}", color = OrangeColor)
            val remaining = category.allocated - category.spent
            Text("Remaining: R$remaining",
                color = if (remaining < 0) Color.Red else TealColor)
        }
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
fun AddIncomeDialog(
    onDismiss: () -> Unit,
    onAddIncome: (Double) -> Unit
) {
    var incomeInput by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color.White,
        titleContentColor = TealColor,
        title = { Text("Add Income", fontWeight = FontWeight.Bold) },
        text = {
            OutlinedTextField(
                value = incomeInput,
                onValueChange = { if (it.isEmpty() || it.matches(Regex("^\\d*\\.?\\d{0,2}$"))) incomeInput = it },
                label = { Text("Income Amount") },
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
fun AddExpenseDialog(
    categories: List<BudgetCategory>,
    onDismiss: () -> Unit,
    onAddExpense: (String, Double) -> Unit
) {
    var selectedCategory by remember { mutableStateOf(categories.firstOrNull()?.name ?: "") }
    var expenseAmount by remember { mutableStateOf("") }
    var expanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color.White,
        titleContentColor = TealColor,
        title = { Text("Add Expense", fontWeight = FontWeight.Bold) },
        text = {
            Column {
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded }
                ) {
                    OutlinedTextField(
                        value = selectedCategory,
                        onValueChange = { },
                        readOnly = true,
                        label = { Text("Category") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        modifier = Modifier.menuAnchor().fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = TealColor,
                            focusedLabelColor = TealColor
                        )
                    )
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        categories.forEach { category ->
                            DropdownMenuItem(
                                text = { Text(category.name, color = TealColor) },
                                onClick = {
                                    selectedCategory = category.name
                                    expanded = false
                                }
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = expenseAmount,
                    onValueChange = { if (it.isEmpty() || it.matches(Regex("^\\d*\\.?\\d{0,2}$"))) expenseAmount = it },
                    label = { Text("Expense Amount") },
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
                    expenseAmount.toDoubleOrNull()?.let { onAddExpense(selectedCategory, it) }
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(containerColor = OrangeColor)
            ) {
                Text("Add Expense")
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
    categories: List<BudgetCategory>,
    onDismiss: () -> Unit,
    onAddCategory: (String, Double) -> Unit,
    onDeleteCategory: (String) -> Unit
) {
    var newCategoryName by remember { mutableStateOf("") }
    var newCategoryBudget by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color.White,
        titleContentColor = TealColor,
        title = { Text("Manage Categories", fontWeight = FontWeight.Bold) },
        text = {
            Column {
                // List existing categories
                categories.forEach { category ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(category.name, color = TealColor, fontWeight = FontWeight.Medium)
                            Text("R ${String.format("%.2f", category.allocated)}", color = TealColor)
                            IconButton(
                                onClick = { onDeleteCategory(category.name) }
                            ) {
                                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = OrangeColor)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                Text("Add New Category", color = TealColor, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))

                // Add new category
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
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = {
                        newCategoryBudget.toDoubleOrNull()?.let {
                            onAddCategory(newCategoryName, it)
                            newCategoryName = ""
                            newCategoryBudget = ""
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = TealColor),
                    modifier = Modifier.fillMaxWidth(),
                    enabled = newCategoryName.isNotBlank() && newCategoryBudget.isNotBlank()
                ) {
                    Text("Add Category")
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = TealColor)
            ) {
                Text("Done")
            }
        }
    )
}

data class BudgetCategory(
    val name: String,
    val allocated: Double,
    val spent: Double
)