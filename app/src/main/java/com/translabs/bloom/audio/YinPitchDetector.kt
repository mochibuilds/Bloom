package com.translabs.bloom.audio

import kotlin.math.sqrt

data class PitchSample(val hz: Float, val clarity: Float)

/** YIN pitch detector (de Cheveigné & Kawahara, 2002). */
class YinPitchDetector(
    private val sampleRate: Int = 16000,
    minHz: Float = 70f,
    maxHz: Float = 500f,
    private val threshold: Float = 0.15f,
) {
    private val tauMin = (sampleRate / maxHz).toInt() // fastest flap we search for
    private val tauMax = (sampleRate / minHz).toInt() // slowest flap we search for
    val frameSize = tauMax + 1024

    private val diff = FloatArray(tauMax + 1)
    private val cmnd = FloatArray(tauMax + 1)

    /** frame = audio samples in [-1, 1]. returns null when silent/not-voicey. */
    fun process(frame: FloatArray): PitchSample? {
        if (frame.size < frameSize) return null

        // silence gate: don't hunt for pitch in quiet room noise
        var power = 0f
        for (s in frame) power += s * s
        if (sqrt(power / frame.size) < 0.01f) return null

        val window = frame.size - tauMax

        // 1) how different is the wave from a copy of itself shifted by τ samples?
        for (tau in 1..tauMax) {
            var sum = 0f
            for (j in 0 until window) {
                val d = frame[j] - frame[j + tau]
                sum += d * d
            }
            diff[tau] = sum
        }

        // 2) cumulative mean normalized difference (makes the dip obvious)
        cmnd[0] = 1f
        var running = 0f
        for (tau in 1..tauMax) {
            running += diff[tau]
            cmnd[tau] = if (running > 0f) diff[tau] * tau / running else 1f
        }

        // 3) first dip clear enough = the wave's repeating period
        var tauEst = -1
        var tau = tauMin
        while (tau <= tauMax) {
            if (cmnd[tau] < threshold) {
                while (tau + 1 <= tauMax && cmnd[tau + 1] < cmnd[tau]) tau++
                tauEst = tau
                break
            }
            tau++
        }
        if (tauEst == -1) return null // no clear period → not voiced sound

        // 4) parabolic interpolation for sub-sample accuracy
        val x0 = cmnd[(tauEst - 1).coerceAtLeast(0)]
        val x1 = cmnd[tauEst]
        val x2 = cmnd[(tauEst + 1).coerceAtMost(tauMax)]
        val denom = x0 - 2 * x1 + x2
        val shift = if (denom != 0f) ((x0 - x2) / (2 * denom)).coerceIn(-1f, 1f) else 0f

        return PitchSample(hz = sampleRate / (tauEst + shift), clarity = 1f - x1)
    }
}