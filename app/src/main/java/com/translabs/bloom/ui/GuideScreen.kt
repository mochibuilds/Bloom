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

private data class GuideStep(val emoji: String, val title: String, val body: String)

private val steps = listOf(
    GuideStep("🎈", "how your voice works",
        "Inside your larynx (voice box) you have two tiny vocal folds. When air flows past them, " +
                "they vibrate — that's your voice! When the folds are stretched thinner & lighter, they vibrate " +
                "faster = higher pitch. Pitch is a muscle skill… and muscles can be trained 💪"),
    GuideStep("✨", "the real secret: resonance",
        "\"Thin\" vs \"deep\" is mostly resonance — the size of the space your voice echoes in. " +
                "A smaller, brighter space sounds lighter & thinner. The trick: raise your larynx a little " +
                "(like a curious tiny puppy 🐶), smile softly, and aim the sound forward, between your eyes."),
    GuideStep("🫳", "step 1 — meet your larynx",
        "Rest two fingertips VERY gently on your throat and swallow. Feel that little lift? " +
                "That's your larynx! We're not forcing it up — just becoming friends with it. Never press hard."),
    GuideStep("🎢", "step 2 — sirens",
        "Slide a soft \"ng\" (like the end of \"sing\") from low to high and back down, like a gentle " +
                "siren. No pushing, no strain — just exploring. Open the practice tab 🎤 and watch your line " +
                "climb into the bloom zone!"),
    GuideStep("🐶", "step 3 — the tiny puppy",
        "Make a soft, bright whine, or a curious \"uh-huh?⤴\" with a rising melody. Feel the buzz in your " +
                "lips, nose & face — NOT in your chest. That forward buzz is exactly what a thinner, brighter " +
                "voice feels like."),
    GuideStep("😊", "step 4 — tiny sentences",
        "With a soft smile, say short phrases like \"hi! how are you?\" or read a story from the stories " +
                "tab 📖 out loud. Keep it light, keep it forward, keep it easy. Questions with rising intonation " +
                "are perfect practice."),
    GuideStep("🫖", "golden rules",
        "• never push through pain or scratchiness — stop & rest\n" +
                "• sip water, keep it cozy\n" +
                "• 5 gentle minutes daily beats 1 hour once a week\n" +
                "• progress is slow & that's okay — you're blooming 🌸\n" +
                "• a voice therapist (SLP) is the gold standard if you can access one 💗\n" +
                "• high vowels ('ee' in tiny, see, free, breeze) naturally brighten your voice ✨")
)

@Composable
fun GuideScreen() {
    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text("voice guide 🎓", style = MaterialTheme.typography.headlineMedium)
        Text("a gentle tutorial for a lighter, thinner, brighter voice — at your own pace 💗",
            style = MaterialTheme.typography.bodyMedium)
        steps.forEach { s ->
            Column(
                Modifier.fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(MaterialTheme.colorScheme.surfaceContainerLow)
                    .padding(16.dp)
            ) {
                Text("${s.emoji} ${s.title}", style = MaterialTheme.typography.titleLarge)
                Text(s.body, style = MaterialTheme.typography.bodyLarge)
            }
        }
    }
}