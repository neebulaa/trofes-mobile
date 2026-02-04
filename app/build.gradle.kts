import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.compose.compiler)
}

android {
    namespace = "pepes.co.trofes"
    compileSdk {
        version = release(36)
    }

    defaultConfig {
        applicationId = "pepes.co.trofes"
        minSdk = 26
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // ------- BASE_URL configurable -------
        // Kamu bisa set di `local.properties`:
        // TROFES_BASE_URL=http://192.168.1.10:8000/
        val localProps = Properties()
        val localPropsFile = rootProject.file("local.properties")
        if (localPropsFile.exists()) {
            localPropsFile.inputStream().use { localProps.load(it) }
        }
        val localBaseUrl = (localProps.getProperty("TROFES_BASE_URL") ?: "").trim()

        // Default (Emulator)
        val defaultBaseUrl = "http://10.0.2.2:8000/"
        val finalBaseUrl = if (localBaseUrl.isNotBlank()) localBaseUrl else defaultBaseUrl

        buildConfigField("String", "BASE_URL", "\"$finalBaseUrl\"")
    }

    buildTypes {
        debug {
            // Tidak perlu hardcode lagi di sini karena sudah ambil dari local.properties
            // Kalau mau override khusus debug, bisa isi ulang buildConfigField di sini.
        }
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            // Release: pakai juga BASE_URL dari defaultConfig (local.properties) agar konsisten.
            // Jika kamu butuh URL beda khusus release, override di sini.
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }
    kotlinOptions {
        jvmTarget = "21"
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = libs.versions.composeCompiler.get()
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.recyclerview)
    implementation("androidx.viewpager2:viewpager2:1.1.0")
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    implementation(platform(libs.compose.bom))

    // ComposeView untuk XML layout
    implementation(libs.androidx.compose.ui.viewbinding)

    // gunakan koordinat langsung agar IDE tidak error bila accessor version catalog belum refresh
    implementation("androidx.activity:activity-compose:" + libs.versions.activityCompose.get())

    implementation(libs.compose.ui)
    implementation(libs.compose.ui.tooling.preview)
    implementation(libs.compose.material3)
    debugImplementation(libs.compose.ui.tooling)

    // CameraX
    val cameraxVersion = "1.3.4"
    implementation("androidx.camera:camera-core:$cameraxVersion")
    implementation("androidx.camera:camera-camera2:$cameraxVersion")
    implementation("androidx.camera:camera-lifecycle:$cameraxVersion")
    implementation("androidx.camera:camera-view:$cameraxVersion")

    // Networking (Retrofit)
    implementation("com.squareup.retrofit2:retrofit:2.11.0")
    implementation("com.squareup.retrofit2:converter-gson:2.11.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")

    // ViewModel + Coroutines scope
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.8.7")

    // Image loading (URL -> ImageView)
    implementation("io.coil-kt:coil:2.6.0")

    // Google Sign-In
    implementation("com.google.android.gms:play-services-auth:21.2.0")
}