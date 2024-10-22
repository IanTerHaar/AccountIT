package com.ianterhaar.accountit.ui.auth

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.ianterhaar.accountit.data.UserRepository
import com.ianterhaar.accountit.data.UserCredentials
import com.ianterhaar.accountit.ui.theme.TealColor
import com.ianterhaar.accountit.ui.theme.OrangeColor

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    userRepository: UserRepository,
    onLoginClick: (String, String) -> Unit,
    onRegisterClick: () -> Unit
) {
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var showError by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    var showForgotPasswordDialog by remember { mutableStateOf(false) }
    var forgotPasswordStep by remember { mutableStateOf(0) }
    var recoveryUsername by remember { mutableStateOf("") }
    var securityAnswer by remember { mutableStateOf("") }
    var securityQuestion by remember { mutableStateOf("") }
    var recoveredCredentials by remember { mutableStateOf<UserCredentials?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "AccountIT",
            style = MaterialTheme.typography.headlineLarge,
            color = TealColor
        )

        Spacer(modifier = Modifier.height(32.dp))

        OutlinedTextField(
            value = username,
            onValueChange = {
                username = it
                showError = false
            },
            label = { Text("Username") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = password,
            onValueChange = {
                password = it
                showError = false
            },
            label = { Text("Password") },
            singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            modifier = Modifier.fillMaxWidth()
        )

        if (showError) {
            Text(
                text = errorMessage,
                color = Color.Red,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(vertical = 8.dp)
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = {
                if (userRepository.loginUser(username, password)) {
                    showError = false
                    onLoginClick(username, password)
                } else {
                    showError = true
                    errorMessage = "Incorrect username or password"
                }
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = TealColor)
        ) {
            Text("Login")
        }

        Spacer(modifier = Modifier.height(16.dp))

        TextButton(
            onClick = {
                showForgotPasswordDialog = true
                forgotPasswordStep = 0
                recoveryUsername = ""
                securityAnswer = ""
                securityQuestion = ""
                recoveredCredentials = null
            },
            colors = ButtonDefaults.textButtonColors(contentColor = OrangeColor)
        ) {
            Text("Forgot Password?")
        }

        TextButton(
            onClick = onRegisterClick,
            colors = ButtonDefaults.textButtonColors(contentColor = OrangeColor)
        ) {
            Text("Don't have an account? Register")
        }
    }

    if (showForgotPasswordDialog) {
        AlertDialog(
            onDismissRequest = {
                showForgotPasswordDialog = false
                forgotPasswordStep = 0
            },
            title = { Text("Password Recovery", color = TealColor) },
            text = {
                Column {
                    when (forgotPasswordStep) {
                        0 -> {
                            OutlinedTextField(
                                value = recoveryUsername,
                                onValueChange = { recoveryUsername = it },
                                label = { Text("Enter Username") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                        1 -> {
                            Text("Security Question:", color = TealColor)
                            Text(
                                text = securityQuestion,
                                style = MaterialTheme.typography.bodyLarge,
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                            OutlinedTextField(
                                value = securityAnswer,
                                onValueChange = { securityAnswer = it },
                                label = { Text("Your Answer") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                        2 -> {
                            recoveredCredentials?.let { credentials ->
                                Text("Your credentials:", color = TealColor)
                                Text(
                                    "Username: ${credentials.username}",
                                    modifier = Modifier.padding(vertical = 4.dp)
                                )
                                Text(
                                    "Password: ${credentials.password}",
                                    modifier = Modifier.padding(vertical = 4.dp)
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        when (forgotPasswordStep) {
                            0 -> {
                                val question = userRepository.getSecurityQuestion(recoveryUsername)
                                if (question != null) {
                                    securityQuestion = question
                                    forgotPasswordStep = 1
                                } else {
                                    showError = true
                                    errorMessage = "Username not found"
                                    showForgotPasswordDialog = false
                                }
                            }
                            1 -> {
                                val credentials = userRepository.verifySecurityAnswer(recoveryUsername, securityAnswer)
                                if (credentials != null) {
                                    recoveredCredentials = credentials
                                    forgotPasswordStep = 2
                                } else {
                                    showError = true
                                    errorMessage = "Incorrect security answer"
                                    showForgotPasswordDialog = false
                                }
                            }
                            2 -> {
                                showForgotPasswordDialog = false
                                forgotPasswordStep = 0
                            }
                        }
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = TealColor)
                ) {
                    Text(when (forgotPasswordStep) {
                        0 -> "Next"
                        1 -> "Verify"
                        else -> "Close"
                    })
                }
            },
            dismissButton = {
                if (forgotPasswordStep != 2) {
                    TextButton(
                        onClick = {
                            showForgotPasswordDialog = false
                            forgotPasswordStep = 0
                        },
                        colors = ButtonDefaults.textButtonColors(contentColor = OrangeColor)
                    ) {
                        Text("Cancel")
                    }
                }
            }
        )
    }
}