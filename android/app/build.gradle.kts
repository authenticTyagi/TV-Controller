plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}
android {
    namespace = "com.home.tvwatch"
    compileSdk = 34
    defaultConfig {
        applicationId = "com.home.tvwatch"
        minSdk = 24          // Android TV 7+; 2020 iFFALCON is typically Android 9
        targetSdk = 28       // keeps background-start rules relaxed on older TV firmware
        versionCode = 1
        versionName = "1.0"
        buildConfigField("String", "RELAY_URL", "\"${project.property("RELAY_URL")}\"")
        buildConfigField("String", "TV_TOKEN", "\"${project.property("TV_TOKEN")}\"")
    }
    buildFeatures { buildConfig = true }
    compileOptions { sourceCompatibility = JavaVersion.VERSION_17; targetCompatibility = JavaVersion.VERSION_17 }
    kotlinOptions { jvmTarget = "17" }
}
dependencies {
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("androidx.core:core-ktx:1.13.1")
}
