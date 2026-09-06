package com.translabs.bloom.data

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

/** Everything stays in this private file. nothing leaves the device. 💗 */
class SessionStore(context: Context) {
    private val file = File(context.filesDir, "sessions.json")
    private val _sessions = MutableStateFlow<List<SessionStats>>(emptyList())
    val sessions: StateFlow<List<SessionStats>> = _sessions

    suspend fun load() = withContext(Dispatchers.IO) {
        if (!file.exists()) return@withContext
        val arr = JSONArray(file.readText())
        _sessions.value = (0 until arr.length()).map { i ->
            val o = arr.getJSONObject(i)
            SessionStats(
                o.getLong("endedAt"), o.getInt("seconds"),
                o.getDouble("medianHz").toFloat(), o.getDouble("p10Hz").toFloat(),
                o.getDouble("p90Hz").toFloat(), o.getInt("inTargetPercent")
            )
        }
    }

    suspend fun add(s: SessionStats) = withContext(Dispatchers.IO) {
        _sessions.value = _sessions.value + s
        file.writeText(JSONArray().apply {
            _sessions.value.forEach {
                put(JSONObject().apply {
                    put("endedAt", it.endedAt); put("seconds", it.seconds)
                    put("medianHz", it.medianHz.toDouble()); put("p10Hz", it.p10Hz.toDouble())
                    put("p90Hz", it.p90Hz.toDouble()); put("inTargetPercent", it.inTargetPercent)
                })
            }
        }.toString())
    }
}