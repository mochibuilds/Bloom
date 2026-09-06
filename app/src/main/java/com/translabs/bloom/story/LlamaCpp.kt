package com.translabs.bloom.story

class LlamaCpp {
    fun interface TokenCallback { fun onToken(token: String) }
    external fun nativeLoad(path: String, threads: Int): Boolean
    external fun nativeGenerate(prompt: String, maxTokens: Int, cb: TokenCallback): String
    external fun nativeFree()
    companion object { init { System.loadLibrary("bloom-llama") } }
}