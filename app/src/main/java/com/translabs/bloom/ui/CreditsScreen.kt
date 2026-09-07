package com.translabs.bloom.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp

private data class Credit(val emoji: String, val name: String, val thanks: String)

private val credits = listOf(
    Credit("🌸", "Mochi", "for the love, the vibes & for believing in bloom 💗"),
    Credit("🤖", "Qwen3.8-Max, Qwen3.8-27B", "for every bug we squashed together ✨"),
    Credit("🎵", "Femtanyl", "for the music 🎶"),
    Credit("🧠", "Qwen2.5-0.5B-Instruct", "the tiny on-device storyteller 🤖"),
    Credit(
        "🏳️‍⚧️",
        "the trans voice community",
        "the SLPs, coaches & friends whose shared knowledge made the guide & passages possible 💗",
    ),
)

@Composable
fun CreditsScreen() {
    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text("thank you 💐", style = MaterialTheme.typography.headlineMedium)
        Text("bloom grew with a little help from these wonderful beings:",
            style = MaterialTheme.typography.bodyMedium)
        credits.forEach { c ->
            Column(
                Modifier.fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(MaterialTheme.colorScheme.secondaryContainer)
                    .padding(16.dp)
            ) {
                Text("${c.emoji} ${c.name}", style = MaterialTheme.typography.titleLarge)
                Text(c.thanks, style = MaterialTheme.typography.bodyLarge)
            }
        }
        Text("and thank YOU, for showing up for your voice 🌱",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.secondary)

        // 🏳️‍⚧️ bloom stands with trans people — always 💗
        Column(
            Modifier.fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(MaterialTheme.colorScheme.primaryContainer)
                .padding(16.dp)
        ) {
            Text("🏳️‍⚧️ bloom stands with trans people — trans youth included.",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer)
            Text("you exist, you matter, and you deserve every right, every joy, " +
                    "and every chance to bloom 💗",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer)
        }
    }
}