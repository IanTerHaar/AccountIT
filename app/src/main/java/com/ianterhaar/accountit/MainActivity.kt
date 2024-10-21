package com.ianterhaar.accountit

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import com.ianterhaar.accountit.ui.theme.AccountItTheme
import com.ianterhaar.accountit.ui.auth.LoginScreen
import com.ianterhaar.accountit.ui.auth.RegisterScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AccountItTheme {
                MainContent()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainContent() {
    var currentScreen by remember { mutableIntStateOf(0) } // 0: login, 1: register, 2: dashboard
    var selectedTab by remember { mutableIntStateOf(0) }

    Scaffold(
        topBar = {
            if (currentScreen == 2) {
                Column {
                    TopAppBar(
                        title = { Text("AccountIT", maxLines = 1, overflow = TextOverflow.Ellipsis) },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = Color(0xFF008080), // Teal
                            titleContentColor = Color.White
                        )
                    )
                    TabRow(
                        selectedTabIndex = selectedTab,
                        containerColor = Color(0xFF008080), // Teal
                        contentColor = Color.White
                    ) {
                        Tab(
                            selected = selectedTab == 0,
                            onClick = { selectedTab = 0 },
                            text = { Text("Budget Tracking") }
                        )
                        Tab(
                            selected = selectedTab == 1,
                            onClick = { selectedTab = 1 },
                            text = { Text("Savings Tracking") }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            when (currentScreen) {
                0 -> LoginScreen(
                    onLoginClick = { username, password ->
                        // Handle login logic here
                        println("Login attempted with: $username, $password")
                        currentScreen = 2 // Navigate to dashboard
                    },
                    onRegisterClick = { currentScreen = 1 } // Navigate to register screen
                )
                1 -> RegisterScreen(
                    onRegisterClick = { username, password, securityQuestion, securityAnswer ->
                        // Handle registration logic here
                        println("Registration attempted with: $username, $password, $securityQuestion, $securityAnswer")
                        currentScreen = 0 // Navigate back to login screen
                    },
                    onLoginClick = { currentScreen = 0 } // Navigate back to login screen
                )
                2 -> {
                    when (selectedTab) {
                        0 -> BudgetTrackingScreen()
                        1 -> SavingsTrackingScreen()
                    }
                }
            }
        }
    }
}

@Composable
fun BudgetTrackingScreen() {
    var totalBudget by remember { mutableDoubleStateOf(3000.0) }
    var income by remember { mutableDoubleStateOf(3500.0) }
    var categories by remember { mutableStateOf(
        listOf(
            BudgetCategory("Food", 900.0, 400.0),
            BudgetCategory("Rent", 1200.0, 1200.0),
            BudgetCategory("Utilities", 450.0, 200.0),
            BudgetCategory("Entertainment", 450.0, 100.0)
        )
    ) }

    var showSetBudgetDialog by remember { mutableStateOf(false) }
    var showAddIncomeDialog by remember { mutableStateOf(false) }
    var showAddExpenseDialog by remember { mutableStateOf(false) }
    var showManageCategoriesDialog by remember { mutableStateOf(false) }

    DashboardScreen(
        totalBudget = totalBudget,
        income = income,
        categories = categories,
        onAddIncomeClick = { showAddIncomeDialog = true },
        onAddExpenseClick = { showAddExpenseDialog = true },
        onSetBudgetClick = { showSetBudgetDialog = true },
        onManageCategoriesClick = { showManageCategoriesDialog = true }
    )

    if (showSetBudgetDialog) {
        SetBudgetDialog(
            onDismiss = { showSetBudgetDialog = false },
            onSetBudget = { newBudget ->
                totalBudget = newBudget
                showSetBudgetDialog = false
            }
        )
    }

    if (showAddIncomeDialog) {
        AddIncomeDialog(
            onDismiss = { showAddIncomeDialog = false },
            onAddIncome = { newIncome ->
                income += newIncome
                showAddIncomeDialog = false
            }
        )
    }

    if (showAddExpenseDialog) {
        AddExpenseDialog(
            categories = categories,
            onDismiss = { showAddExpenseDialog = false },
            onAddExpense = { category, amount ->
                categories = categories.map {
                    if (it.name == category) it.copy(spent = it.spent + amount)
                    else it
                }
                showAddExpenseDialog = false
            }
        )
    }

    if (showManageCategoriesDialog) {
        ManageCategoriesDialog(
            categories = categories,
            onDismiss = { showManageCategoriesDialog = false },
            onAddCategory = { name, budget ->
                categories = categories + BudgetCategory(name, budget, 0.0)
            },
            onDeleteCategory = { name ->
                categories = categories.filter { it.name != name }
            }
        )
    }
}

@Composable
fun SavingsTrackingScreen() {
    // TODO: Implement Savings Tracking Screen
    Text("Savings Tracking - Coming Soon")
}