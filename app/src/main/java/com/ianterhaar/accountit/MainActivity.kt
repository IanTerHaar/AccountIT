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
import com.ianterhaar.accountit.ui.theme.AccountItTheme
import com.ianterhaar.accountit.ui.auth.LoginScreen
import com.ianterhaar.accountit.ui.auth.RegisterScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AccountItTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    AuthScreens(Modifier.padding(innerPadding))
                }
            }
        }
    }
}

@Composable
fun AuthScreens(modifier: Modifier = Modifier) {
    var showLogin by remember { mutableStateOf(true) }

    if (showLogin) {
        LoginScreen(
            onLoginClick = { username, password ->
                // Handle login logic here
                println("Login attempted with: $username, $password")
            },
            onRegisterClick = {
                showLogin = false
            }
        )
    } else {
        RegisterScreen(
            onRegisterClick = { username, password, securityQuestion, securityAnswer ->
                // Handle registration logic here
                println("Registration attempted with: $username, $password, $securityQuestion, $securityAnswer")
                showLogin = true // Switch back to login screen after registration
            },
            onLoginClick = {
                showLogin = true
            }
        )
    }
}