#include <jni.h>
#include <string>
#include <vector>
#include <cstring>
#include <algorithm>
#include <android/log.h>
#include "llama.h"

#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, "bloom-llama", __VA_ARGS__)

// 🌸 FIX: Increased context & batch sizes to handle longer prompts without crashing
static const int N_CTX   = 2048;  // total tokens the model can remember (prompt + story)
static const int N_BATCH = 512;   // max tokens we feed to llama_decode in one call

static llama_model   * g_model = nullptr;
static llama_context * g_ctx   = nullptr;
static llama_sampler * g_smpl  = nullptr;

extern "C" JNIEXPORT jboolean JNICALL
Java_com_translabs_bloom_story_LlamaCpp_nativeLoad(JNIEnv * env, jobject, jstring jpath, jint nThreads) {
    const char * path = env->GetStringUTFChars(jpath, nullptr);
    llama_backend_init();

    llama_model_params mp = llama_model_default_params();
    mp.n_gpu_layers = 0; // CPU only, privacy first
    g_model = llama_model_load_from_file(path, mp);
    env->ReleaseStringUTFChars(jpath, path);
    if (!g_model) { LOGI("model load failed"); return JNI_FALSE; }

    llama_context_params cp = llama_context_default_params();
    cp.n_ctx     = N_CTX;
    cp.n_batch   = N_BATCH;
    cp.n_threads = nThreads;
    g_ctx = llama_init_from_model(g_model, cp);
    if (!g_ctx) return JNI_FALSE;

    g_smpl = llama_sampler_chain_init(llama_sampler_chain_default_params());

    const llama_vocab * vocab = llama_model_get_vocab(g_model);
    int32_t n_vocab = llama_vocab_n_tokens(vocab);

    llama_sampler_chain_add(g_smpl, llama_sampler_init_penalties(
            n_vocab, 64, 1.25f, 0.0f, 0.0f
    ));
    llama_sampler_chain_add(g_smpl, llama_sampler_init_top_k(40));
    llama_sampler_chain_add(g_smpl, llama_sampler_init_top_p(0.9f, 1));
    llama_sampler_chain_add(g_smpl, llama_sampler_init_temp(0.9f));
    llama_sampler_chain_add(g_smpl, llama_sampler_init_dist(42));
    LOGI("model loaded 🌸");
    return JNI_TRUE;
}

// 🌸 Wraps a message into the model's own chat format
static std::string formatChat(const char * systemMsg, const char * userMsg) {
    llama_chat_message msgs[2];
    msgs[0].role = "system"; msgs[0].content = systemMsg;
    msgs[1].role = "user";   msgs[1].content = userMsg;

    // Get the model's default template (or fallback to chatml)
    const char * tmpl = llama_model_chat_template(g_model, nullptr);
    if (!tmpl) tmpl = "chatml";

    // Measure needed size (6 arguments!)
    int32_t need = llama_chat_apply_template(tmpl, msgs, 2, true, nullptr, 0);
    if (need <= 0) return std::string(userMsg);

    std::vector<char> buf(need + 1, 0);
    llama_chat_apply_template(tmpl, msgs, 2, true, buf.data(), need + 1);
    return std::string(buf.data());
}

extern "C" JNIEXPORT jstring JNICALL
Java_com_translabs_bloom_story_LlamaCpp_nativeGenerate(
        JNIEnv * env, jobject, jstring jprompt, jint maxTokens, jobject callback) {
    if (!g_ctx || !g_model) return env->NewStringUTF("");
    const llama_vocab * vocab = llama_model_get_vocab(g_model);
    const char * prompt = env->GetStringUTFChars(jprompt, nullptr);

    // 🌸 FIX: Stricter system prompt to prevent "here u go" type responses
    std::string formatted = formatChat(
            "You are Bloom, a gentle story writer for a voice-practice app. "
            "You write ONLY the story itself. Nothing else. "
            "You NEVER write: 'here you go', 'here is your story', 'sure!', 'of course!', "
            "'certainly!', 'here's a story', 'let me write', 'I'll write', or any introduction. "
            "You NEVER write titles, headings, or explanations. "
            "You ONLY output the story text, starting directly with the first word of the story.",
            prompt
    );
    env->ReleaseStringUTFChars(jprompt, prompt);

    int prompt_len = (int) formatted.size();

    // First pass: measure how many tokens we need
    int n = -llama_tokenize(vocab, formatted.c_str(), prompt_len, nullptr, 0, true, true);
    if (n <= 0) return env->NewStringUTF("");

    // 🌸 FIX: Safety check — leave room in the context window for the generated story
    if (n >= N_CTX - maxTokens) {
        LOGI("prompt too long (%d tokens) for context window (%d) — skipping", n, N_CTX);
        return env->NewStringUTF("");
    }

    // Second pass: actually tokenize
    std::vector<llama_token> tokens(n);
    llama_tokenize(vocab, formatted.c_str(), prompt_len, tokens.data(), n, true, true);

    llama_memory_clear(llama_get_memory(g_ctx), false);

    // 🌸 FIX: THE CRASH FIX! Decode the prompt in chunks of N_BATCH.
    // Previously we fed ALL tokens at once, which exceeded n_batch and caused ggml_abort 💥
    for (int i = 0; i < n; i += N_BATCH) {
        int n_eval = std::min(N_BATCH, n - i);
        llama_batch batch = llama_batch_get_one(tokens.data() + i, n_eval);
        if (llama_decode(g_ctx, batch) != 0) {
            LOGI("prompt decode failed at chunk %d", i);
            return env->NewStringUTF("");
        }
    }

    jclass cbClass  = env->GetObjectClass(callback);
    jmethodID onTok = env->GetMethodID(cbClass, "onToken", "(Ljava/lang/String;)V");

    std::string result;
    for (int i = 0; i < maxTokens; i++) {
        llama_token id = llama_sampler_sample(g_smpl, g_ctx, -1);
        if (llama_vocab_is_eog(vocab, id)) break;

        char buf[128];
        int len = llama_token_to_piece(vocab, id, buf, sizeof(buf), 0, true);
        if (len <= 0) break;
        std::string piece(buf, len);
        result += piece;
        env->CallVoidMethod(callback, onTok, env->NewStringUTF(piece.c_str()));

        // Decode one token at a time (always within n_batch, so it's safe)
        if (llama_decode(g_ctx, llama_batch_get_one(&id, 1)) != 0) break;
    }
    return env->NewStringUTF(result.c_str());
}

extern "C" JNIEXPORT void JNICALL
Java_com_translabs_bloom_story_LlamaCpp_nativeFree(JNIEnv *, jobject) {
    if (g_smpl)  llama_sampler_free(g_smpl);
    if (g_ctx)   llama_free(g_ctx);
    if (g_model) llama_model_free(g_model);
    g_smpl = nullptr; g_ctx = nullptr; g_model = nullptr;

    // 🌸 FIX: Free global backend resources initialized by llama_backend_init()!
    llama_backend_free();
}