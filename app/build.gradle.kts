plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "com.translabs.bloom"
    compileSdk = 37

    defaultConfig {
        applicationId = "com.translabs.bloom"
        minSdk = 26
        targetSdk = 37
	versionCode = 3
	versionName = "1.0.2"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        ndk {
            // 🌸 FIX: dropped armeabi-v7a — llama.cpp's FP16 intrinsics don't
            // build on 32-bit ARM, and 99%+ of modern Android devices are 64-bit.
            // arm64-v8a = all real devices, x86_64 = emulator 💗
            abiFilters += listOf("arm64-v8a", "x86_64")        }
    }

    buildTypes {
        release {
            // 🌸 FIX: Replaced the invalid `optimization { enable = false }` block.
            // For a release build, we usually enable minification. HOWEVER, because you use
            // JNI callbacks to `onToken` in LlamaCpp.kt, R8 will strip the callback interface
            // and crash the app unless you add `@Keep` to LlamaCpp!
            // I've set it to false here to keep it safe and match your original intent.
            isMinifyEnabled = false
            isShrinkResources = false
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        compose = true
    }

    externalNativeBuild {
        cmake {
            path = file("src/main/cpp/CMakeLists.txt")
            version = "3.22.1"
        }
    }
}

dependencies {
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    testImplementation(libs.junit)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    debugImplementation(libs.androidx.compose.ui.tooling)
    implementation("androidx.compose.material3:material3:1.4.0")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.9.1")
    implementation("androidx.work:work-runtime-ktx:2.10.0")
}
