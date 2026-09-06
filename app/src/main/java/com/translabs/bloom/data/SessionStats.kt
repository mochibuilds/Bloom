package com.translabs.bloom.data

data class SessionStats(
    val endedAt: Long,        // epoch millis
    val seconds: Int,         // seconds your voice was actually heard
    val medianHz: Float,
    val p10Hz: Float,
    val p90Hz: Float,
    val inTargetPercent: Int, // % of time inside the bloom zone
)