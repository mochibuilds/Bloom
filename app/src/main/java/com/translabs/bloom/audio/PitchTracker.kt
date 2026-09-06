package com.translabs.bloom.audio

import android.annotation.SuppressLint
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.isActive

class PitchTracker(private val sampleRate: Int = 16000) {

    @SuppressLint("MissingPermission")
    fun pitches(): Flow<PitchSample> = flow {
        val detector = YinPitchDetector(sampleRate)
        val frame = FloatArray(detector.frameSize)
        val hop = 512
        val chunk = ShortArray(hop)

        val minBuf = AudioRecord.getMinBufferSize(
            sampleRate, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT
        )
        val rec = AudioRecord(
            MediaRecorder.AudioSource.VOICE_RECOGNITION,
            sampleRate, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT,
            maxOf(minBuf, hop * 4)
        )

        // FIXED: Check if AudioRecord initialized successfully
        if (rec.state != AudioRecord.STATE_INITIALIZED) {
            rec.release()
            return@flow // Exit gracefully instead of crashing
        }

        rec.startRecording()
        try {
            while (currentCoroutineContext().isActive) {
                val read = rec.read(chunk, 0, hop)
                if (read <= 0) continue
                System.arraycopy(frame, read, frame, 0, frame.size - read)
                var i = 0; var p = frame.size - read
                while (i < read) frame[p++] = chunk[i++] / 32768f
                detector.process(frame)?.let { emit(it) }
            }
        } finally {
            rec.stop()
            rec.release()
        }
    }.flowOn(Dispatchers.IO)
}