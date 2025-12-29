import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
}

kotlin {
    androidTarget {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)
        }
    }
    
    listOf(
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "ComposeApp"
            isStatic = true
        }
        iosTarget.compilations.getByName("main") {
            val retrofun by cinterops.creating {
                defFile("src/iosMain/cinterop/retrofun.def")
                packageName("org.retrofun.project.cinterop")
                includeDirs("src/androidMain/cpp")
            }
        }
    }
    
    sourceSets {
        androidMain.dependencies {
            implementation(compose.preview)
            implementation(libs.androidx.activity.compose)
            implementation(libs.koin.android)
        }
        commonMain.dependencies {
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.ui)
            implementation(compose.components.resources)
            implementation(compose.components.uiToolingPreview)
            implementation(libs.androidx.lifecycle.viewmodel)
            implementation(libs.androidx.lifecycle.runtimeCompose)
            implementation(compose.materialIconsExtended)
            
            
            // Koin
            implementation(libs.koin.core)
            implementation(libs.koin.compose)
            
            // Voyager
            implementation(libs.voyager.navigator)
            implementation(libs.voyager.screenModel)
            implementation(libs.voyager.transitions)
            implementation(libs.voyager.koin)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
    }
}

android {
    namespace = "org.retrofun.project"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    defaultConfig {
        applicationId = "org.retrofun.project"
        minSdk = libs.versions.android.minSdk.get().toInt()
        targetSdk = libs.versions.android.targetSdk.get().toInt()
        versionCode = 1
        versionName = "1.0"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    
    externalNativeBuild {
        cmake {
            path = file("src/androidMain/cpp/CMakeLists.txt")
            version = "3.22.1"
        }
    }
}

dependencies {
    debugImplementation(compose.uiTooling)
}

