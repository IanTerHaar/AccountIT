package com.ianterhaar.accountit

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.ianterhaar.accountit.data.UserRepository

@Composable
fun SettingsScreen(
    userId: Int,
    userRepository: UserRepository
) {
    var selectedCurrency by remember { mutableStateOf("ZAR") }
    var showCurrencyDialog by remember { mutableStateOf(false) }
    var showAboutDialog by remember { mutableStateOf(false) }
    var showInfoDialog by remember { mutableStateOf(false) }

    LaunchedEffect(userId) {
        val currencyFromDb = userRepository.getCurrency(userId) ?: "ZAR"
        selectedCurrency = currencyFromDb
    }

    val currencySymbols = mapOf(
        "USD" to "$",        // United States
        "EUR" to "€",        // European Union
        "GBP" to "£",        // United Kingdom
        "JPY" to "¥",        // Japan
        "CAD" to "$",        // Canada
        "AUD" to "$",        // Australia
        "CHF" to "Fr",       // Switzerland
        "CNY" to "¥",        // China
        "KRW" to "₩",        // South Korea
        "ZAR" to "R"         // South Africa
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.Start
    ) {
        Text(
            text = "Settings",
            style = MaterialTheme.typography.headlineMedium,
            color = Color(0xFF008080),
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // Currency
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { showCurrencyDialog = true }
                .padding(vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Currency")
            Text(selectedCurrency, color = Color.Gray)
        }

        // About Section
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            Divider(
                color = Color.Gray.copy(alpha = 0.2f),
                thickness = 1.dp,
                modifier = Modifier.padding(vertical = 16.dp)
            )
            // About
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showAboutDialog = true }
                    .padding(vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("About")
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Go to About",
                    modifier = Modifier.rotate(270f)
                )
            }

            // Additional Information
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showInfoDialog = true }
                    .padding(vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Additional Information")
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Go to Info",
                    modifier = Modifier.rotate(270f)
                )
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        // Currency Dialog
        if (showCurrencyDialog) {
            AlertDialog(
                onDismissRequest = { showCurrencyDialog = false },
                title = { Text("Select Currency") },
                text = {
                    Column {
                        currencySymbols.keys.forEach { currencyCode ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        selectedCurrency = currencyCode
                                        userRepository.updateCurrency(userId, currencyCode)
                                        showCurrencyDialog = false
                                    }
                                    .padding(vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(currencyCode, modifier = Modifier.weight(1f))
                                Text(currencySymbols[currencyCode] ?: "")
                            }
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = { showCurrencyDialog = false }
                    ) {
                        Text("Close")
                    }
                }
            )
        }

        // About Dialog
        if (showAboutDialog) {
            AlertDialog(
                onDismissRequest = { showAboutDialog = false },
                title = { Text("About") },
                text = {
                    Text(
                        text = "A third year Software Engineering project by:\n" +
                                "• Ian Ter Haar\n" +
                                "• Hendrik Coetzee\n" +
                                "• Ryan Mostert\n" +
                                "• Barend Kock\n" +
                                "• Simeon Momberg",
                        style = MaterialTheme.typography.bodyLarge
                    )
                },
                confirmButton = {
                    Button(
                        onClick = { showAboutDialog = false },
                        shape = RoundedCornerShape(50)
                    ) {
                        Text("OK")
                    }
                }
            )
        }

        // Additional Information Dialog
        if (showInfoDialog) {
            AlertDialog(
                onDismissRequest = { showInfoDialog = false },
                title = { Text("Additional Information") },
                text = {
                    Column {
                        Text("Database Version: 7", style = MaterialTheme.typography.bodyLarge)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Package Name: com.ianterhaar.accountit", style = MaterialTheme.typography.bodyLarge)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("GitHub repository: https://github.com/IanTerHaar/AccountIT", style = MaterialTheme.typography.bodyLarge)
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                },
                confirmButton = {
                    Button(
                        onClick = { showInfoDialog = false },
                        shape = RoundedCornerShape(50)
                    ) {
                        Text("OK")
                    }
                }
            )
        }
    }
}