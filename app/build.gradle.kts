plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "com.example.socialme_interfazgrafica"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.socialme_interfazgrafica"
        minSdk = 24
        targetSdk = 34
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
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.navigation.runtime.android)
    implementation(libs.androidx.runtime.livedata)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.play.services.cast.framework)
    implementation(libs.androidx.compiler)
    implementation(libs.play.services.maps)
    implementation(libs.play.services.location)
    implementation(libs.firebase.firestore.ktx)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation ("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation ("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.5.2")
    implementation("com.squareup.okhttp3:logging-interceptor:4.11.0")
    // Coil Compose
    implementation ("io.coil-kt:coil-compose:2.4.0")

    // Optional: If you want to support loading images from URLs
    implementation ("io.coil-kt:coil-compose-base:2.4.0")

    implementation ("com.google.accompanist:accompanist-flowlayout:0.28.0")

    // Librería principal OSMDroid
    implementation ("org.osmdroid:osmdroid-android:6.1.17")

    // Eliminadas las siguientes dependencias que no están disponibles o han cambiado:
    // implementation ("org.osmdroid:osmdroid-mapsforge:6.1.17")
    // implementation ("org.osmdroid:osmdroid-geopackage:6.1.17")
    // implementation ("org.osmdroid:osmdroid-wms:6.1.17")
    // implementation ("org.osmdroid:osmdroid-third-party:6.1.17")

    // Para geocodificación y funcionalidades adicionales
    implementation ("com.github.MKergall:osmbonuspack:6.9.0")
    // PayPal SDK
    implementation ("com.paypal.sdk:paypal-android-sdk:2.16.0")

    // También necesitarás estas para compatibilidad
    implementation ("androidx.legacy:legacy-support-v4:1.0.0")
    implementation ("androidx.cardview:cardview:1.0.0")

    // WebSockets
    implementation ("org.java-websocket:Java-WebSocket:1.5.3")

    // Retrofit y Gson (si no lo tienes ya)
    implementation ("com.squareup.retrofit2:retrofit:2.9.0")
    implementation ("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation ("com.google.code.gson:gson:2.9.0")
}