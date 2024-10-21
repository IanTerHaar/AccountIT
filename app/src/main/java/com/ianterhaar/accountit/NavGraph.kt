package com.ianterhaar.accountit.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.ianterhaar.accountit.DashboardScreen
import com.ianterhaar.accountit.ui.auth.LoginScreen

@Composable
fun NavGraph(navController: NavHostController) {
    NavHost(navController, startDestination = "login") {
        composable("login") {
            LoginScreen(
                navController = navController,
                onLoginClick = { username, password ->
                    // Implement your login logic here
                    if (username == "admin" && password == "admin") { // Example login logic
                        navController.navigate("dashboard")
                    } else {
                        // Show error
                    }
                },
                errorMessage = "" // You can manage error messages as needed
            )
        }
        composable("dashboard") {
            // Provide dummy parameters for the time being
            DashboardScreen(
                totalBudget = 0.0,
                income = 0.0,
                categories = listOf(),
                onAddIncomeClick = { /* handle add income */ },
                onAddExpenseClick = { /* handle add expense */ },
                onSetBudgetClick = { /* handle set budget */ },
                onManageCategoriesClick = { /* handle manage categories */ }
            )
        }
    }
}
