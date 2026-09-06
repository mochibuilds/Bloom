package com.translabs.bloom.ui.theme

import kotlin.math.ln

/** log scale because ears hear pitch logarithmically 🎚️ */
fun pitchY(hz: Float, height: Float): Float {
    val lo = ln(60f); val hi = ln(400f)
    val t = ((ln(hz.coerceIn(60f, 400f)) - lo) / (hi - lo)).coerceIn(0f, 1f)
    return height * (1f - t)
}