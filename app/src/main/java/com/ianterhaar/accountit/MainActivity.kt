package com.ianterhaar.accountit

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.ianterhaar.accountit.data.UserRepository
import com.ianterhaar.accountit.ui.theme.AccountItTheme
import com.ianterhaar.accountit.navigation.NavGraph

class MainActivity : ComponentActivity() {
    private lateinit var userRepository: UserRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Initialize the repository
        userRepository = UserRepository(this)

        setContent {
            AccountItTheme {
                val navController = rememberNavController() // Create NavHostController
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    NavGraph(navController = navController, modifier = Modifier.padding(innerPadding)) // Pass innerPadding here
                }
            }
        }
    }
}




@Composable
fun AuthScreens(modifier: Modifier = Modifier, userRepository: UserRepository) {
    var showLogin by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf("") }

    if (showLogin) {
        LoginScreen(
            onLoginClick = { username, password ->
                if (userRepository.loginUser(username, password)) {
                    // Navigate to dashboard if login is successful
                    println("Login successful for user: $username")
                    errorMessage = ""
                    // Navigate to your dashboard or home screen here
                    // TODO: Implement navigation to Dashboard
                } else {
                    errorMessage = "Invalid username or password"
                    println(errorMessage)
                }
            },
            onRegisterClick = {
                showLogin = false
            },
            errorMessage = errorMessage // Pass the error message to the UI to display
        )
    } else {
        RegisterScreen(
            onRegisterClick = { username, password, securityQuestion, securityAnswer ->
                if (userRepository.registerUser(username, password, securityQuestion, securityAnswer)) {
                    println("Registration successful for user: $username")
                    errorMessage = ""
                    showLogin = true // Switch back to login screen after registration
                } else {
                    errorMessage = "Registration failed. Try again."
                    println(errorMessage)
                }
            },
            onLoginClick = {
                showLogin = true
            },
            errorMessage = errorMessage
        )
    }
}

