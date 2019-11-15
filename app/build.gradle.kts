plugins {
    id("com.android.application")
    id("kotlin-android")
    id("kotlin-android-extensions")
    id("kotlin-kapt")
    id("com.google.firebase.appdistribution")
}

android {
    compileSdkVersion(Versions.compileSdk)
    buildToolsVersion = Versions.buildTools

    defaultConfig {
        applicationId = "jp.kentan.student_portal_plus"
        minSdkVersion(Versions.minSdk)
        targetSdkVersion(Versions.targetSdk)
        versionCode = Versions.code
        versionName = Versions.name

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables.useSupportLibrary = true
    }

    sourceSets {
        val sharedTestDir = "src/sharedTest/java"
        getByName("test") {
            java.srcDir(sharedTestDir)
        }
        getByName("androidTest") {
            java.srcDir(sharedTestDir)
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    kotlinOptions {
        jvmTarget = "1.8"
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
        getByName("debug") {
            applicationIdSuffix = ".debug"
            isDebuggable = true

            firebaseAppDistribution {
                groups = "testers"
            }
        }
    }

    dataBinding {
        isEnabled = true
    }
}

dependencies {
    implementation(Deps.Kotlin.stdlib)
    implementation(Deps.Kotlin.Coroutines.android)

    implementation(Deps.AndroidX.appCompat)
    implementation(Deps.AndroidX.coreKtx)
    implementation(Deps.AndroidX.fragmentKtx)
    implementation(Deps.AndroidX.constraintLayout)
    implementation(Deps.AndroidX.swipeRefreshLayout)
    implementation(Deps.AndroidX.workKtx)
    implementation(Deps.AndroidX.preferenceKtx)
    implementation(Deps.AndroidX.browser)
    implementation(Deps.AndroidX.Navigation.fragmentKtx)
    implementation(Deps.AndroidX.Navigation.uiKtx)
    implementation(Deps.AndroidX.Lifecycle.extensions)
    implementation(Deps.AndroidX.Lifecycle.viewModelKtx)
    implementation(Deps.AndroidX.Lifecycle.liveDataKtx)
    implementation(Deps.AndroidX.Room.runtime)
    implementation(Deps.AndroidX.Room.ktx)
    kapt(Deps.AndroidX.Room.compiler)

    implementation(Deps.material)

    implementation(Deps.Dagger.core)
    implementation(Deps.Dagger.androidSupport)
    kapt(Deps.Dagger.compiler)
    kapt(Deps.Dagger.androidProcessor)

    implementation(Deps.AssistedInject.core)
    kapt(Deps.AssistedInject.processor)

    implementation(Deps.OkHttp.client)
    implementation(Deps.OkHttp.urlConnection)

    implementation(Deps.jsoup)

    implementation(project(":colorpicker"))

    testImplementation(Deps.Test.junit)
    testImplementation(Deps.Test.mockk)
    testImplementation(Deps.Test.truth)
    testImplementation(Deps.Test.kotlinCoroutines)
    testImplementation(Deps.Test.archCore)

    androidTestImplementation(Deps.Test.androidXTestCore)
    androidTestImplementation(Deps.Test.androidXTestRunner)
    androidTestImplementation(Deps.Test.androidXTestEspresso)
    androidTestImplementation(Deps.Test.kotlinCoroutines)
    androidTestImplementation(Deps.Test.commonLang)
}

apply(plugin = "com.google.gms.google-services")
