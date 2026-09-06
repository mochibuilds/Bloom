# 🌸 bloom

**a gentle voice-practice companion — private, on-device, and kind to you.**

![made with kotlin](https://img.shields.io/badge/made_with-kotlin-7F52FF?style=flat-square)
![ui](https://img.shields.io/badge/ui-jetpack_compose-4285F4?style=flat-square)
![ai](https://img.shields.io/badge/ai-100%25_on--device-B4256C?style=flat-square)
![cloud](https://img.shields.io/badge/cloud-none-386928?style=flat-square)
![trans rights](https://img.shields.io/badge/trans_rights-human_rights-5BCEFA?style=flat-square)
![license](https://img.shields.io/badge/license-MIT-B4256C?style=flat-square)

bloom helps you explore and train your voice with live pitch feedback, cozy
reading material, an optional on-device AI storyteller, a gentle voice
tutorial, and soft daily reminders. made with love for everyone finding
their voice — especially trans & gender-diverse folks 💗

## 🏳️‍⚧️ first things first

**trans people exist, and trans people deserve rights.** full stop.
trans women are women, trans men are men, and non-binary people are real.
trans youth are not a political debate — they are kids who deserve safety,
support, healthcare, joy, and the freedom to grow into who they are.

bloom was built in that spirit. whether you're trans, non-binary,
questioning, or exploring your voice for any other reason (singers, actors,
voice therapy, or just for fun) — you are welcome here exactly as you are.

if you're a trans kid or teen and things feel heavy right now: your identity
is real, your voice is yours, and you are so loved. you are not alone —
organizations like [The Trevor Project](https://www.thetrevorproject.org),
[Trans Lifeline](https://translifeline.org) and
[Point of Pride](https://www.pointofpride.org) exist to support you 💗

---

## ✨ features

- 🎤 **live pitch practice** — a from-scratch YIN pitch detector listens to
  your mic and draws your voice as a blooming line. the pink *bloom zone*
  (165–220 Hz) shows when you're in your target range.
- 📈 **beautiful progress** — sessions, voiced minutes, and a gentle streak
  that forgives rest days 💗
- 📖 **story time** — curated reading passages designed with voice-training
  phonetics in mind (high vowels, rising intonation, soft consonants), plus
  an optional **on-device AI storyteller** (Qwen2.5-0.5B-Instruct) that
  writes tiny wholesome stories just for you, streaming token by token.
  *AI stories may contain errors or unexpected content — read gently!*
- 🎓 **voice guide** — a gentle tutorial on how your larynx & resonance work,
  with safe, strain-free exercises loved by the trans voice community
  (NG sirens, swallow larynx-mapping, tiny-puppy whines & more).
- 💗 **self-care** — daily practice reminders that survive reboots, plus
  voice-health rules.
- 💐 **gratitude** — a little page that says thank you to the people (and
  models) who made bloom possible.

## 🔐 privacy first

- 🎙️ audio is analyzed **in memory and thrown away immediately** — never
  recorded, never saved.
- 📁 progress lives in a private file **on your device only**.
- 🧠 the AI storyteller runs **fully on-device** (CPU-only). your stories
  never touch a server.
- 📶 the 491 MB model downloads **only over Wi‑Fi**, and only if you ask
  for it.

## 🎚️ how the pitch magic works

bloom implements the **YIN algorithm** (de Cheveigné & Kawahara, 2002) in
pure Kotlin: a sliding window computes the cumulative mean normalized
difference function, finds the first clear dip below threshold, and refines
it with parabolic interpolation for sub-sample accuracy. the result is
smoothed with a rolling median and drawn on a **logarithmic scale**, because
ears hear pitch logarithmically 🎚️

## 🧠 how the voice guide works

the guide is built on techniques widely used in gender-affirming voice
training and shared by SLPs & the trans voice community:

- **resonance over pitch** — a brighter, "smaller-space" voice reads as more
  feminine; gently raising the larynx shortens the vocal tract.
- **the swallow** — feel your larynx lift & become friends with it.
- **NG sirens** — sliding sirens on "ng" connect registers without strain.
- **tiny puppy / big dog–small dog** — soft whines teach forward, bright
  placement.
- **high vowels & rising intonation** — "ee"-heavy words and questions
  naturally brighten the voice.
- **safety first** — never push through pain; short daily practice wins;
  an SLP is the gold standard when accessible.

## 🛠️ tech stack

| layer | what |
|---|---|
| UI | Kotlin + Jetpack Compose, Material 3 |
| pitch detection | YIN, implemented from scratch (no libraries) |
| audio | `AudioRecord` raw mic, 16 kHz mono, sliding-window analysis |
| on-device LLM | llama.cpp via JNI + CMake, Qwen2.5-0.5B-Instruct (Q4_K_M GGUF) |
| reminders | `AlarmManager` exact alarms + `BroadcastReceiver`, boot-resilient |
| storage | plain JSON in app-private storage, `SharedPreferences` for settings |

## 🚀 building

1. clone the repo:
   ```bash
   git clone https://github.com/Mochi/Bloom.git
   ```
2. open in Android Studio and sync gradle.
3. the first build fetches & compiles **llama.cpp** automatically via CMake
   (needs `git` + internet, and the Android NDK).
4. run on any device or emulator with **API 26+**.

> the AI storyteller model (491 MB) is downloaded **inside the app**
> (stories tab → *download*), over Wi‑Fi only. without it, bloom gracefully
> falls back to the built-in storybook 📚

## ️ project structure

```text
app/src/main/java/com/translabs/bloom/
├── MainActivity.kt           # navigation + first-boot permissions
├── audio/
│   ├── PitchTracker.kt       # mic → sliding window → pitch flow
│   └── YinPitchDetector.kt   # the YIN algorithm 🎚️
├── data/
│   ├── SessionStats.kt
│   └── SessionStore.kt       # private on-device JSON store
├── reminders/
│   ├── ReminderScheduler.kt  # AlarmManager daily alarm
│   ├── ReminderReceiver.kt   # posts the notification
│   └── BootReceiver.kt       # reschedules after reboot
├── story/
│   ├── ModelDownloader.kt    # Wi‑Fi-gated GGUF download
│   ├── StoryViewModel.kt
│   ├── LlamaCpp.kt           # JNI bridge
│   └── Passage.kt            # built-in storybook
└── ui/
    ├── LivePitchScreen.kt
    ├── ProgressScreen.kt
    ├── StoryScreen.kt
    ├── GuideScreen.kt
    ├── CareScreen.kt
    ├── CreditsScreen.kt
    └── theme/                # colors, type & pitch math

app/src/main/cpp/
├── CMakeLists.txt            # fetches & builds llama.cpp
└── llama_bridge.cpp          # JNI glue
```

## 💐 thanks

bloom grew with a little help from:

- **Mochi** — for the love, the vibes & for believing in bloom 💗
- **Qwen3.8-Max** — for the brains, every bug we squashed together ✨
- **Femtanyl** — for the music 🎶
- **Qwen2.5-0.5B-Instruct** — the tiny on-device storyteller 🤖
- **the trans voice community** — the SLPs, coaches & friends whose shared
  knowledge made the guide & passages possible 🏳️⚧️

and thank **you**, for showing up for your voice 🌱

## 📄 license

MIT © Mochi — take it, learn from it, make it yours 💗
(see [LICENSE](LICENSE))

---

made with 💗, tea 🫖 and a tiny 0.5B model running on your phone.
trans people exist, trans people deserve rights, and trans youth deserve
to bloom 🏳️‍⚧️🌸
