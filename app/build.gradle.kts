plugins {
    alias(libs.plugins.android.application)
    id("org.jetbrains.kotlin.android")
    // Добавьте ksp для генерации кода, если планируете переход на Room,
    // но для чистого SQLite дополнительные плагины не требуются.
}

android {
    namespace = "com.example.project_skebob"
    compileSdk = 35 // Рекомендуется использовать актуальный SDK

    defaultConfig {
        applicationId = "com.example.project_skebob"
        minSdk = 28
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildFeatures {
        viewBinding = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    // Добавьте этот блок обязательно!
    kotlinOptions {
        jvmTarget = "11"
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)

    // Многопоточность (Coroutines)
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.6.2")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.6.2")

    // Сеть (Retrofit)
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")

    // UI компоненты
    implementation("androidx.recyclerview:recyclerview:1.3.2")
}