package com.translabs.bloom.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.translabs.bloom.story.ModelDownloader
import com.translabs.bloom.story.StoryViewModel

@Composable
fun StoryScreen(vm: StoryViewModel = viewModel()) {
    val state by vm.modelState.collectAsState()
    val passage by vm.passage.collectAsState()
    val ai by vm.aiText.collectAsState()
    val busy by vm.busy.collectAsState()

    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("story time 📖", style = MaterialTheme.typography.headlineMedium)

        Column(
            Modifier.fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(MaterialTheme.colorScheme.surfaceContainerLow)
                .padding(16.dp)
        ) {
            when (val s = state) {
                is ModelDownloader.State.Done ->
                    Text("AI storyteller ready ✨ stories are written on YOUR phone, never in the cloud 💗")
                is ModelDownloader.State.Downloading -> {
                    if (s.percent < 0) {
                        Text("downloading the storyteller… (please wait)")
                        LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                    } else {
                        Text("downloading the storyteller… ${s.percent}%")
                        LinearProgressIndicator(progress = { s.percent / 100f },
                            modifier = Modifier.fillMaxWidth())
                    }
                }
                is ModelDownloader.State.NeedWifi -> Text("the storyteller is 491 MB — hop on Wi‑Fi first 📶")
                is ModelDownloader.State.NoSpace -> Text("not enough space for the storyteller right now 🥺")
                is ModelDownloader.State.Failed -> Text("download hiccuped: ${s.why} — try again?")
                else -> Text("built-in storybook active 📚 (or download the AI storyteller below!)")
            }
            if (state !is ModelDownloader.State.Downloading && state !is ModelDownloader.State.Done) {
                Spacer(Modifier.height(8.dp))
                OutlinedButton(onClick = { vm.download() }) {
                    Text("download AI storyteller (491 MB)")
                }
            }
        }

        Button(onClick = { vm.newStory() }, enabled = !busy) {
            Text(if (busy) "writing… 🧠" else "new story ✨")
        }

        if (ai.isNotEmpty()) {
            Column(
                Modifier.fillMaxWidth()
                    .clip(RoundedCornerShape(28.dp))
                    .background(MaterialTheme.colorScheme.secondaryContainer)
                    .padding(20.dp)
            ) {
                Text("your AI story, written on-device ✨",
                    style = MaterialTheme.typography.bodyMedium)
                Spacer(Modifier.height(4.dp))
                // 🌸 FIX: Model name + disclaimer
                Text("powered by Qwen2.5-0.5B-Instruct 🤖",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.6f))
                Spacer(Modifier.height(2.dp))
                Text("AI-generated stories may contain errors or unexpected content — read gently! 💗",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.6f))
                Spacer(Modifier.height(8.dp))
                Text(ai, style = MaterialTheme.typography.bodyLarge)
                Spacer(Modifier.height(8.dp))
                Text("read it out loud & watch the practice tab 🎤",
                    style = MaterialTheme.typography.bodyMedium)
            }
        } else if (!busy) {
            Column(
                Modifier.fillMaxWidth()
                    .clip(RoundedCornerShape(28.dp))
                    .background(MaterialTheme.colorScheme.primaryContainer)
                    .padding(20.dp)
            ) {
                Text(passage.title, style = MaterialTheme.typography.titleLarge)
                Text("focus: ${passage.focus}", style = MaterialTheme.typography.bodyMedium)
                Spacer(Modifier.height(8.dp))
                Text(passage.text, style = MaterialTheme.typography.bodyLarge)
                Spacer(Modifier.height(8.dp))
                Text("read it out loud & watch the practice tab 🎤",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer)
            }
        }
    }
}