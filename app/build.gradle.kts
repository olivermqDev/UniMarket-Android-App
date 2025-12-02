plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.google.gms.google.services)
    alias(libs.plugins.ksp)
}

android {
    namespace = "com.atom.unimarket"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.atom.unimarket"
        minSdk = 24
        targetSdk = 36
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
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)
    implementation(libs.androidx.compose.runtime.livedata)
    implementation(libs.androidx.compose.runtime)
    implementation(libs.androidx.compose.foundation.layout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    implementation("androidx.navigation:navigation-compose:2.8.0-beta05")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.3")
    // Importa el Bill of Materials (BOM) de Firebase. NO USES UNA VERSIÓN ESPECÍFICA AQUÍ.
    implementation(platform("com.google.firebase:firebase-bom:33.1.2"))
    // Libreria para el dashboard
    //implementation("com.patrykandpatrick.vico:compose:2.0.0-beta.1")
    implementation("com.patrykandpatrick.vico:compose:1.16.1")
    implementation("com.patrykandpatrick.vico:core:1.16.1")

    // Añade la dependencia para Firebase Authentication
    implementation("com.google.firebase:firebase-auth-ktx")
    // Para Firebase Storage (almacenamiento de archivos como imágenes)
    implementation("com.google.firebase:firebase-storage-ktx")

    // Librería para cargar imágenes desde una URL de forma sencilla en Jetpack Compose
    implementation("io.coil-kt:coil-compose:2.6.0")
    // Para la base de datos en tiempo real Cloud Firestore
    implementation("com.google.firebase:firebase-firestore-ktx")
    // Para Firebase Cloud Messaging (notificaciones push)
    implementation("com.google.firebase:firebase-messaging-ktx")
    implementation("androidx.compose.material:material-icons-extended")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-play-services:1.8.1")
    implementation("androidx.annotation:annotation:1.8.0") // O una versión más reciente
    // Google AI (Gemini)
    implementation("com.google.ai.client.generativeai:generativeai:0.9.0")

    implementation(libs.koin.android)
    implementation(libs.koin.androidx.compose)
    testImplementation(libs.koin.android.test)
}