// build.gradle.kts (Module: app)
// File ini mengatur konfigurasi dan dependencies modul aplikasi

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    // Plugin Compose Compiler — wajib untuk Kotlin 2.0+ dengan Jetpack Compose
    id("org.jetbrains.kotlin.plugin.compose")
}

android {
    namespace = "com.example.inventarisbarang"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.inventarisbarang"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        compose = true
    }
}

dependencies {
    // === COMPOSE BOM ===
    // BOM (Bill of Materials) mengatur versi semua library Compose secara konsisten
    // Cukup deklarasikan BOM sekali, semua library Compose otomatis versi yang sama
    val composeBom = platform("androidx.compose:compose-bom:2024.12.01")
    implementation(composeBom)

    // === COMPOSE UI ===
    implementation("androidx.compose.ui:ui")                    // Komponen UI dasar
    implementation("androidx.compose.ui:ui-graphics")           // Grafis dan rendering
    implementation("androidx.compose.ui:ui-tooling-preview")    // Preview di Android Studio
    implementation("androidx.compose.material3:material3")      // Material Design 3

    // === MATERIAL COMPONENTS (XML) ===
    // Diperlukan agar themes.xml bisa menggunakan parent Theme.Material3
    implementation("com.google.android.material:material:1.12.0")
    implementation("androidx.compose.material:material-icons-extended")

    // === ANDROID CORE ===
    implementation("androidx.core:core-ktx:1.15.0")             // Extension functions Android
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.7")  // Lifecycle aware
    implementation("androidx.activity:activity-compose:1.9.3")  // Activity + Compose bridge

    // === NAVIGATION ===
    // Navigation Compose — mengatur perpindahan antar screen
    implementation("androidx.navigation:navigation-compose:2.8.5")

    // === IMAGE LOADING ===
    // Coil — library modern untuk load gambar dari URI/URL secara asinkron
    implementation("io.coil-kt:coil-compose:2.7.0")

    // === JSON SERIALIZATION ===
    // Gson — konversi objek Kotlin ke JSON dan sebaliknya
    // Digunakan untuk menyimpan List<Barang> ke SharedPreferences
    implementation("com.google.code.gson:gson:2.11.0")

    // === TESTING ===
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")
    androidTestImplementation(composeBom)
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")

    // === DEBUG ===
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
}
