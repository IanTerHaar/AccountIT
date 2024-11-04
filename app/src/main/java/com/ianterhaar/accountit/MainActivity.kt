package com.ianterhaar.accountit

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import com.ianterhaar.accountit.data.BudgetTrackingRepository
import com.ianterhaar.accountit.data.UserRepository
import com.ianterhaar.accountit.ui.theme.AccountItTheme
import com.ianterhaar.accountit.ui.auth.LoginScreen
import com.ianterhaar.accountit.ui.auth.RegisterScreen
import com.ianterhaar.accountit.ui.savings.SavingsTrackingScreen
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import java.util.Calendar

class MainActivity : ComponentActivity() {
    private lateinit var userRepository: UserRepository
    private lateinit var budgetTrackingRepository: BudgetTrackingRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        userRepository = UserRepository(this)
        budgetTrackingRepository = BudgetTrackingRepository(this)
        setContent {
            AccountItTheme {
                MainContent(userRepository, budgetTrackingRepository)
            }
        }
    }
}

data class UserState(
    val isLoggedIn: Boolean = false,
    val userId: Int = -1,
    val username: String = ""
)

fun getGreeting(): String {
    val currentHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
    return when {
        currentHour in 0..11 -> "Good morning"
        currentHour in 12..17 -> "Good afternoon"
        else -> "Good evening"
    }
}

@Composable
fun SavingsTrackingScreen(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "Savings Tracking Feature\nComing Soon!",
            textAlign = TextAlign.Center
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainContent(userRepository: UserRepository, budgetTrackingRepository: BudgetTrackingRepository) {
    var currentScreen by remember { mutableIntStateOf(0) } // 0: login, 1: register, 2: dashboard, 3: settings
    var selectedTab by remember { mutableIntStateOf(0) }
    var userState by remember { mutableStateOf(UserState()) }
    var showProfileMenu by remember { mutableStateOf(false) }

    /*
    * This if statement allows for when you are on the settings tab to press the back button on
    * you phone navigation to return to the dashboard screen again.
    */
    if (currentScreen == 3) {
        BackHandler {
            currentScreen = 2
        }
    }

    Scaffold(
        topBar = {
            if (currentScreen == 2) {
                Column {
                    TopAppBar(
                        title = {
                            Row(
                                modifier = Modifier.fillMaxWidth(0.85f), // Reduced width to make room for profile button
                                horizontalArrangement = Arrangement.Center
                            ) {
                                if (userState.isLoggedIn) {
                                    Text(
                                        text = "${getGreeting()}, ${userState.username}",
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        color = Color.White
                                    )
                                }
                            }
                        },
                        actions = {
                            // Profile Button
                            IconButton(onClick = { showProfileMenu = true }) {
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = "Profile",
                                    tint = Color.White
                                )
                            }
                            // Profile Dropdown Menu
                            DropdownMenu(
                                expanded = showProfileMenu,
                                onDismissRequest = { showProfileMenu = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text("Edit Profile") },
                                    onClick = {
                                        // Handle edit profile
                                        showProfileMenu = false
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Settings") },
                                    onClick = {
                                        currentScreen = 3
                                        showProfileMenu = false
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Logout") },
                                    onClick = {
                                        userState = UserState()
                                        currentScreen = 0
                                        showProfileMenu = false
                                    }
                                )
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = Color(0xFF008080),
                            titleContentColor = Color.White
                        )
                    )
                    TabRow(
                        selectedTabIndex = selectedTab,
                        containerColor = Color(0xFF008080),
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
                    userRepository = userRepository,
                    onLoginClick = { username, password ->
                        val userId = userRepository.authenticateUser(username, password)
                        if (userId != -1) {
                            userState = UserState(
                                isLoggedIn = true,
                                userId = userId,
                                username = username
                            )
                            currentScreen = 2
                        }
                    },
                    onRegisterClick = { currentScreen = 1 }
                )
                1 -> RegisterScreen(
                    onRegisterClick = { username, password, securityQuestion, securityAnswer ->
                        if (userRepository.registerUser(username, password, securityQuestion, securityAnswer)) {
                            currentScreen = 0
                        }
                    },
                    onLoginClick = { currentScreen = 0 }
                )
                2 -> {
                    when (selectedTab) {
                        0 -> BudgetTrackingScreen(
                            budgetTrackingRepository = budgetTrackingRepository,
                            userId = userState.userId
                        )
                        1 -> SavingsTrackingScreen()
                    }
                }
                3 -> SettingsScreen()
            }
        }
    }
}

@Composable
fun BudgetTrackingScreen(
    budgetTrackingRepository: BudgetTrackingRepository,
    userId: Int
) {
    var totalBudget by remember { mutableStateOf(0.0) }
    var income by remember { mutableStateOf(0.0) }
    var categories by remember { mutableStateOf(emptyList<BudgetCategory>()) }

    // Load data from the database when the screen is first composed
    LaunchedEffect(Unit) {
        totalBudget = budgetTrackingRepository.getTotalBudget(userId)
        income = budgetTrackingRepository.getIncome(userId)
        categories = budgetTrackingRepository.getCategories(userId)
    }

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
        onManageCategoriesClick = { showManageCategoriesDialog = true },
        onDeleteCategory = { categoryName ->
            budgetTrackingRepository.deleteCategory(userId, categoryName)
            categories = categories.filter { it.name != categoryName }
        },
        onAddExpense = { category, amount ->
            budgetTrackingRepository.addExpense(userId, category, amount)
            categories = categories.map {
                if (it.name == category) it.copy(spent = it.spent + amount)
                else it
            }
        },  // Added missing comma here
        onTogglePinCategory = { categoryName ->
            budgetTrackingRepository.toggleCategoryPin(userId, categoryName)
            categories = budgetTrackingRepository.getCategories(userId)
        }
    )

    if (showSetBudgetDialog) {
        SetBudgetDialog(
            onDismiss = { showSetBudgetDialog = false },
            onSetBudget = { newBudget ->
                budgetTrackingRepository.updateBudget(userId, newBudget)
                totalBudget = newBudget
            }
        )
    }

    if (showAddIncomeDialog) {
        ManageIncomeDialog(
            currentIncome = income,
            onDismiss = { showAddIncomeDialog = false },
            onAddIncome = { newIncome ->
                budgetTrackingRepository.addIncome(userId, newIncome)
                income += newIncome
            },
            onResetIncome = {
                budgetTrackingRepository.resetIncome(userId)
                income = 0.0
                showAddIncomeDialog = false
            }
        )
    }

    if (showManageCategoriesDialog) {
        ManageCategoriesDialog(
            onDismiss = { showManageCategoriesDialog = false },
            onAddCategory = { name, budget ->
                budgetTrackingRepository.addCategory(userId, name, budget)
                categories = categories + BudgetCategory(name, budget, 0.0)
            }
        )
    }
}