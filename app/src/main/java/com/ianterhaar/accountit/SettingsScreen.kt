package com.ianterhaar.accountit

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.unit.dp

@Composable
fun SettingsScreen() {
    var isDarkModeEnabled by remember { mutableStateOf(false) }
    var selectedCurrency by remember { mutableStateOf("USD") }
    var showAboutDialog by remember { mutableStateOf(false) }

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

        // Dark Mode
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { isDarkModeEnabled = !isDarkModeEnabled }
                .padding(vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Dark Mode")
            Switch(
                checked = isDarkModeEnabled,
                onCheckedChange = { isDarkModeEnabled = it },
                modifier = Modifier.padding(start = 16.dp)
            )
        }

        // Currency
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { /* TODO: Open currency selection dialog */ }
                .padding(vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Currency")
            Text(selectedCurrency, color = Color.Gray)
        }

        // About, Help, and License
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            Divider(
                color = Color.Gray.copy(alpha = 0.2f),
                thickness = 1.dp,
                modifier = Modifier.padding(vertical = 16.dp)
            )
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
            Text(
                text = "Help",
                style = MaterialTheme.typography.titleMedium,
                color = Color(0xFF008080),
                modifier = Modifier.padding(bottom = 8.dp, top = 16.dp)
            )
            Text(
                text = "License",
                style = MaterialTheme.typography.titleMedium,
                color = Color(0xFF008080),
                modifier = Modifier.padding(bottom = 8.dp, top = 16.dp)
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        if (showAboutDialog) {
            AlertDialog(
                onDismissRequest = { showAboutDialog = false },
                title = { Text("About") },
                text = {
                    ClickableText(
                        text = buildAnnotatedString {
                            append("A third year Software Engineering project by:\n")
                            pushStringAnnotation(
                                tag = "Ian",
                                annotation = "https://github.com/IanTerHaar"
                            )
                            append("• Ian Ter Haar ")
                            pop()
                            pushStringAnnotation(
                                tag = "Hendrik",
                                annotation = "https://github.com/HenreCoetzee"
                            )
                            append("(https://github.com/IanTerHaar)\n")
                            pop()
                            pushStringAnnotation(
                                tag = "Ryan",
                                annotation = "https://github.com/RyanMostert"
                            )
                            append("• Hendrik Coetzee ")
                            pop()
                            pushStringAnnotation(
                                tag = "Barend",
                                annotation = "https://github.com/Oats10"
                            )
                            append("(https://github.com/HenreCoetzee)\n")
                            pop()
                            pushStringAnnotation(
                                tag = "Simeon",
                                annotation = "https://github.com/SimeonMomberg"
                            )
                            append("• Ryan Mostert ")
                            pop()
                            append("(https://github.com/RyanMostert)\n")
                            append("• Barend Kock ")
                            append("(https://github.com/Oats10)\n")
                            append("• Simeon Momberg ")
                            append("(https://github.com/SimeonMomberg)")
                        },
                        style = MaterialTheme.typography.bodyLarge,
                        onClick = { offset ->
                            clickableText(offset, listOf(
                                "Ian" to "https://github.com/IanTerHaar",
                                "Hendrik" to "https://github.com/HenreCoetzee",
                                "Ryan" to "https://github.com/RyanMostert",
                                "Barend" to "https://github.com/Oats10",
                                "Simeon" to "https://github.com/SimeonMomberg"
                            ))
                        }
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
    }
}

private fun clickableText(
    offset: Int,
    links: List<Pair<String, String>>
) {
    links.forEach { (tag, url) ->
        if (tag in listOf("Ian", "Hendrik", "Ryan", "Barend", "Simeon")) {
            // Open URL on click
            // TODO: Implement opening URL in browser
            println("Clicked on $tag's GitHub link: $url")
        }
    }
}