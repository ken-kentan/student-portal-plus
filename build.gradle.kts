// Top-level build file where you can add configuration options common to all sub-projects/modules.

buildscript {
    repositories {
        google()
        jcenter()
        gradlePluginPortal()
    }
    dependencies {
        classpath(Deps.GradlePlugin.android)
        classpath(Deps.GradlePlugin.kotlin)
        classpath(Deps.GradlePlugin.ktlint)
        classpath(Deps.GradlePlugin.googleServices)
        classpath(Deps.GradlePlugin.ossLicenses)
        classpath(Deps.GradlePlugin.firebaseAppDistribution)
        // NOTE: Do not place your application dependencies here; they belong
        // in the individual module build.gradle files
    }
}

allprojects {
    repositories {
        google()
        jcenter()
    }

    apply(plugin = "org.jlleitschuh.gradle.ktlint")
}

task<Delete>("clean") {
    delete = setOf(rootProject.buildDir)
}
