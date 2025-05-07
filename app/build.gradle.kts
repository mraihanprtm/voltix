plugins {
    id("com.google.gms.google-services")
    id("com.google.devtools.ksp")
    id("com.google.dagger.hilt.android")

    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "com.example.voltix"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.voltix"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    signingConfigs {
        create("release") {
            storeFile = file("D:/DEV/Archive/VoltixKeystore/voltixkey.jks")
            storePassword = "admin123"
            keyAlias = "voltixkey"
            keyPassword = "admin123"
        }
    }

    buildTypes {
        getByName("release") {
            signingConfig = signingConfigs.getByName("release")
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

    implementation(platform("com.google.firebase:firebase-bom:33.13.0"))
    implementation("com.google.firebase:firebase-analytics")
    implementation("com.google.android.gms:play-services-auth:21.3.0")
    implementation(libs.firebase.auth)
    implementation(libs.androidx.room.common)
    implementation(libs.androidx.runtime.livedata)
    implementation ("androidx.lifecycle:lifecycle-viewmodel-savedstate:2.6.1")
    implementation(libs.androidx.room.common.jvm)
    val credentialManagerVersion = "1.5.0-alpha05"
    implementation("androidx.credentials:credentials:$credentialManagerVersion")
    implementation("androidx.credentials:credentials-play-services-auth:$credentialManagerVersion")
    implementation("com.google.android.libraries.identity.googleid:googleid:1.1.1")

    implementation ("com.squareup.retrofit2:retrofit:2.11.0")
    implementation ("com.squareup.retrofit2:converter-gson:2.11.0")
    implementation ("com.squareup.okhttp3:logging-interceptor:4.12.0")
    implementation ("com.google.android.material:material:1.5.0")

    implementation ("androidx.lifecycle:lifecycle-viewmodel-compose:2.6.2")
    implementation("com.itextpdf:itext7-core:7.2.3")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
    implementation ("androidx.navigation:navigation-compose:2.7.5")
    implementation("androidx.compose.material3:material3:1.2.0")
    implementation("androidx.compose.material3:material3-window-size-class:1.2.0")
    implementation (platform("com.google.firebase:firebase-bom:32.7.1"))
    implementation ("com.google.mlkit:image-labeling:17.0.9")
    implementation ("com.android.volley:volley:1.2.1")
    implementation ("com.github.bumptech.glide:glide:4.16.0")
    implementation ("com.squareup.okhttp3:logging-interceptor:4.9.3")
    implementation ("com.cloudinary:cloudinary-android:2.0.0")

    implementation("com.google.dagger:hilt-android:2.51")
    implementation(libs.firebase.auth.ktx)
    ksp("com.google.dagger:hilt-android-compiler:2.51")
    implementation("androidx.hilt:hilt-navigation-compose:1.1.0")

    implementation("io.coil-kt.coil3:coil-compose:3.1.0")
    implementation("io.coil-kt.coil3:coil-network-okhttp:3.1.0")
    implementation ("androidx.lifecycle:lifecycle-runtime-compose:2.8.0")
    // Room
    implementation("androidx.room:room-runtime:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")
    ksp("androidx.room:room-compiler:2.6.1")
    implementation("androidx.datastore:datastore-preferences:1.0.0")

    implementation ("com.airbnb.android:lottie-compose:6.5.1")

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}