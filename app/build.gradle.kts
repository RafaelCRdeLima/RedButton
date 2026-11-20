plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "br.botaovermelho"
    compileSdk = 35

    defaultConfig {
        applicationId = "br.botaovermelho"
        minSdk = 23          // Android 6.0 ou mais novo
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
}
// Sem dependências: o app usa só a API do próprio Android.
