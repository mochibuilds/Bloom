package com.translabs.bloom.story

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.net.HttpURLConnection
import java.net.URL

object ModelDownloader {
    const val URL = "https://huggingface.co/Qwen/Qwen2.5-0.5B-Instruct-GGUF/resolve/main/qwen2.5-0.5b-instruct-q4_k_m.gguf"
    const val NEED_BYTES = 600_000_000L

    sealed class State {
        object Idle : State()
        object NeedWifi : State()
        object NoSpace : State()
        data class Downloading(val percent: Int) : State()
        object Done : State()
        data class Failed(val why: String) : State()
    }

    fun modelFile(ctx: Context) = File(ctx.filesDir, "qwen2.5-0.5b-q4km.gguf")

    fun onWifi(ctx: Context): Boolean {
        val cm = ctx.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val caps = cm.getNetworkCapabilities(cm.activeNetwork) ?: return false
        return caps.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
    }

    suspend fun download(ctx: Context, onProgress: (Int) -> Unit) = withContext(Dispatchers.IO) {
        val conn = URL(URL).openConnection() as HttpURLConnection
        conn.connect()
        val total = conn.contentLengthLong // Use Long for large files

        // 🌸 FIX: Download to a temp file to prevent corrupt "Done" states!
        val tmpFile = File(ctx.filesDir, "qwen2.5-0.5b-q4km.tmp")
        if (tmpFile.exists()) tmpFile.delete()

        conn.inputStream.use { input ->
            tmpFile.outputStream().use { out ->
                val buf = ByteArray(64 * 1024)
                var copied = 0L
                var n = input.read(buf)
                while (n > 0) {
                    out.write(buf, 0, n)
                    copied += n
                    if (total > 0) {
                        onProgress(((copied * 100) / total).toInt())
                    } else {
                        onProgress(-1) // Indeterminate progress
                    }
                    n = input.read(buf)
                }
            }
        }
        conn.disconnect()

        // Atomically rename to final file only if download succeeded
        val finalFile = modelFile(ctx)
        if (finalFile.exists()) finalFile.delete()
        if (!tmpFile.renameTo(finalFile)) {
            throw Exception("Failed to rename temp file")
        }
    }
}