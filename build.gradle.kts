// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    id("androidx.navigation.safeargs.kotlin") version "2.8.9" apply false
}

// Project-level build.gradle.kts
buildscript {
    repositories {
        google()  // Google's Maven repository
        mavenCentral()  // Maven Central repository
        gradlePluginPortal()  // Gradle Plugin Portal
    }
    dependencies {
        classpath("com.android.tools.build:gradle:7.0.4")
        // other classpath dependencies
    }
}


