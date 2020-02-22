object Deps {
    object GradlePlugin {
        const val android = "com.android.tools.build:gradle:3.5.2"
        const val kotlin = "org.jetbrains.kotlin:kotlin-gradle-plugin:${Kotlin.version}"
        const val googleServices = "com.google.gms:google-services:4.3.3"
        const val ossLicenses = "com.google.android.gms:oss-licenses-plugin:0.10.1"
        const val firebaseAppDistribution =
            "com.google.firebase:firebase-appdistribution-gradle:1.3.1"
    }

    object Kotlin {
        const val version = "1.3.61"

        const val stdlib = "org.jetbrains.kotlin:kotlin-stdlib-jdk7:$version"


        object Coroutines {
            internal const val version = "1.3.3"

            const val android = "org.jetbrains.kotlinx:kotlinx-coroutines-android:$version"
        }
    }

    object AndroidX {
        const val appCompat = "androidx.appcompat:appcompat:1.1.0"
        const val coreKtx = "androidx.core:core-ktx:1.2.0"
        const val fragmentKtx = "androidx.fragment:fragment-ktx:1.2.2"
        const val constraintLayout = "androidx.constraintlayout:constraintlayout:1.1.3"
        const val swipeRefreshLayout = "androidx.swiperefreshlayout:swiperefreshlayout:1.0.0"
        const val workKtx = "androidx.work:work-runtime-ktx:2.3.2"
        const val preferenceKtx = "androidx.preference:preference-ktx:1.1.0"
        const val browser = "androidx.browser:browser:1.2.0"

        object Navigation {
            private const val version = "2.2.1"

            const val fragmentKtx = "androidx.navigation:navigation-fragment-ktx:$version"
            const val uiKtx = "androidx.navigation:navigation-ui-ktx:$version"
        }

        object Lifecycle {
            private const val version = "2.2.0"

            const val viewModelKtx = "androidx.lifecycle:lifecycle-viewmodel-ktx:$version"
            const val liveDataKtx = "androidx.lifecycle:lifecycle-livedata-ktx:$version"
            const val extensions = "androidx.lifecycle:lifecycle-extensions:$version"
        }

        object Room {
            private const val version = "2.2.4"

            const val runtime = "androidx.room:room-runtime:$version"
            const val ktx = "androidx.room:room-ktx:$version"
            const val compiler = "androidx.room:room-compiler:$version"
        }
    }

    const val material = "com.google.android.material:material:1.1.0"

    const val playServicesOssLicenses = "com.google.android.gms:play-services-oss-licenses:17.0.0"

    object Dagger {
        private const val version = "2.26"

        const val core = "com.google.dagger:dagger:$version"
        const val androidSupport = "com.google.dagger:dagger-android-support:$version"
        const val compiler = "com.google.dagger:dagger-compiler:$version"
        const val androidProcessor = "com.google.dagger:dagger-android-processor:$version"
    }

    object AssistedInject {
        private const val version = "0.5.2"

        const val core = "com.squareup.inject:assisted-inject-annotations-dagger2:$version"
        const val processor = "com.squareup.inject:assisted-inject-processor-dagger2:$version"
    }

    object OkHttp {
        private const val version = "3.14.6"

        const val client = "com.squareup.okhttp3:okhttp:$version"
        const val urlConnection = "com.squareup.okhttp3:okhttp-urlconnection:$version"
    }

    const val jsoup = "org.jsoup:jsoup:1.12.2"

    object Test {
        const val junit = "junit:junit:4.13"
        const val mockk = "io.mockk:mockk:1.9.3"
        const val truth = "com.google.truth:truth:1.0"
        const val kotlinCoroutines =
            "org.jetbrains.kotlinx:kotlinx-coroutines-test:${Kotlin.Coroutines.version}"
        const val archCore = "androidx.arch.core:core-testing:2.1.0"

        const val androidXTestCore = "androidx.test:core:1.2.0"
        const val androidXTestRunner = "androidx.test:runner:1.2.0"
        const val androidXTestEspresso = "androidx.test.espresso:espresso-core:3.2.0"
        const val commonLang = "org.apache.commons:commons-lang3:3.9"
    }
}
