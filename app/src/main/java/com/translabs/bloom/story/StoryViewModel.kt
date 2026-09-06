package com.translabs.bloom.story

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class StoryViewModel(app: Application) : AndroidViewModel(app) {
    private val _state = MutableStateFlow(
        if (ModelDownloader.modelFile(app).exists()) ModelDownloader.State.Done
        else ModelDownloader.State.Idle
    )
    val modelState: StateFlow<ModelDownloader.State> = _state

    private val _passage = MutableStateFlow(CuratedPassages.all.random())
    val passage: StateFlow<Passage> = _passage
    private val _aiText = MutableStateFlow("")
    val aiText: StateFlow<String> = _aiText
    private val _busy = MutableStateFlow(false)
    val busy: StateFlow<Boolean> = _busy

    private val llama = LlamaCpp()
    private var loaded = false

    private val themes = listOf(
        "a cozy rainy day", "a brave little robot", "friendship & warm tea",
        "a garden that sings", "stargazing with a cat", "a compliment that saves the day",
        "a tiny bird learning to fly", "a magical library", "a gentle dragon who loves flowers"
    )

    fun newStory() {
        if (_busy.value) return
        val file = ModelDownloader.modelFile(getApplication())
        if (!file.exists()) { // built-in storybook fallback 📚
            _passage.value = CuratedPassages.all.filter { it != _passage.value }.random()
            _aiText.value = ""
            return
        }
        _busy.value = true
        _aiText.value = ""
        viewModelScope.launch(Dispatchers.Default) {
            try {
                if (!loaded) loaded = llama.nativeLoad(
                    file.absolutePath,
                    minOf(4, Runtime.getRuntime().availableProcessors())
                )
                if (!loaded) { _passage.value = CuratedPassages.all.random(); return@launch }

                // 🌸 FIX: high vowels woven in naturally, strict story-only output
                val prompt = buildString {
                    append("Write a short, wholesome story (about 100 words) for voice practice, ")
                    append("about: ${themes.random()}. ")
                    append("Favor short, bright words with 'ee' and 'ay' vowel sounds (tiny, breezy, sweet, sunny), ")
                    append("but weave them naturally into real sentences — never list them or repeat a phrase twice. ")
                    append("Include 2-3 questions with rising intonation and soft consonants (m, n, l, w, y). ")
                    append("Make it playful with exclamations and gentle moments. ")
                    append("Output ONLY the story itself. No title, no 'here you go', no filler. Just the story.")
                }

                llama.nativeGenerate(prompt, 300, LlamaCpp.TokenCallback { t ->
                    _aiText.value += t // streams live, token by token ✨
                })

                // 🌸 FIX: strip any preamble the model might have snuck in
                _aiText.value = stripPreamble(_aiText.value)
            } finally { _busy.value = false }
        }
    }

    // 🌸 FIX: aggressively strip conversational filler the model sometimes adds
    private fun stripPreamble(text: String): String {
        val preamblePatterns = listOf(
            Regex("(?i)^\\s*(here\\s*(you|u)\\s*go|here\\s*is\\s*your\\s*story|here\\'s\\s*a\\s*story|here\\s*is\\s*a\\s*story)[.:!]?\\s*"),
            Regex("(?i)^\\s*(sure!|of\\s*course!|certainly!|absolutely!|no\\s*problem!|happy\\s*to\\s*help!)[.:!]?\\s*"),
            Regex("(?i)^\\s*(let\\s*me\\s*write|i\\'ll\\s*write|let\\'s\\s*see|okay,?\\s*here)[.:!]?\\s*"),
            Regex("(?i)^\\s*(story\\s*:|title\\s*:)[.:!]?\\s*")
        )
        var result = text.trim()
        for (pattern in preamblePatterns) {
            result = pattern.replace(result, "")
        }
        // Capitalize first letter if the strip removed it
        return result.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }.trim()
    }

    fun download() {
        val ctx = getApplication<Application>()
        viewModelScope.launch {
            if (!ModelDownloader.onWifi(ctx)) { _state.value = ModelDownloader.State.NeedWifi; return@launch }
            if (ctx.filesDir.freeSpace < ModelDownloader.NEED_BYTES) { _state.value = ModelDownloader.State.NoSpace; return@launch }
            try {
                _state.value = ModelDownloader.State.Downloading(0)
                ModelDownloader.download(ctx) { p -> _state.value = ModelDownloader.State.Downloading(p) }
                _state.value = ModelDownloader.State.Done
            } catch (e: Exception) {
                _state.value = ModelDownloader.State.Failed(e.message ?: "download failed")
            }
        }
    }

    override fun onCleared() { llama.nativeFree() }
}