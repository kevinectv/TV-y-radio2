import java.util.Base64

plugins {
  alias(libs.plugins.android.application)
  alias(libs.plugins.kotlin.compose)
  alias(libs.plugins.google.devtools.ksp)
  alias(libs.plugins.roborazzi)
  alias(libs.plugins.secrets)
}

// Decode debug.keystore from base64 if it is missing
val keystoreFile = file("${rootDir}/debug.keystore")
val base64File = file("${rootDir}/debug.keystore.base64")
println("--- KEYSTORE DIAGNOSTICS: keystoreFile exists = ${keystoreFile.exists()}, base64File exists = ${base64File.exists()}, absolute path = ${keystoreFile.absolutePath} ---")
if (base64File.exists()) {
    try {
        val base64Content = base64File.readText().replace("\\s".toRegex(), "")
        val decodedBytes = Base64.getDecoder().decode(base64Content)
        keystoreFile.writeBytes(decodedBytes)
        println("--- DECODED DEBUG KEYSTORE TO ROOT SUCCESSFULLY ---")
    } catch (e: Exception) {
        println("--- FAILED TO DECODE KEYSTORE: ${e.message} ---")
    }
}

android {
  namespace = "com.example"
  compileSdk = 35

  defaultConfig {
    applicationId = "com.aistudio.luminaplay.iptv.vpxwqr"
    minSdk = 21
    targetSdk = 34
    versionCode = 4
    versionName = "2.0.2"

    testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
  }

  signingConfigs {
    val uploadKeystorePath = System.getenv("KEYSTORE_PATH") ?: "${rootDir}/my-upload-key.jks"
    val uploadKeystoreFile = file(uploadKeystorePath)
    val hasUploadKeystore = uploadKeystoreFile.exists()

    create("release") {
      if (hasUploadKeystore) {
        storeFile = uploadKeystoreFile
        storePassword = System.getenv("STORE_PASSWORD") ?: "android"
        keyAlias = System.getenv("KEY_ALIAS") ?: "upload"
        keyPassword = System.getenv("KEY_PASSWORD") ?: "android"
      } else {
        storeFile = file("${rootDir}/debug.keystore")
        storePassword = "android"
        keyAlias = "androiddebugkey"
        keyPassword = "android"
      }
      enableV1Signing = true
      enableV2Signing = true
    }
    create("debugConfig") {
      storeFile = file("${rootDir}/debug.keystore")
      storePassword = "android"
      keyAlias = "androiddebugkey"
      keyPassword = "android"
      enableV1Signing = true
      enableV2Signing = true
    }
  }

  buildTypes {
    release {
      isCrunchPngs = false
      isMinifyEnabled = false
      proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
      signingConfig = signingConfigs.getByName("release")
    }
    debug {
      signingConfig = signingConfigs.getByName("debugConfig")
    }
  }
  compileOptions {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
  }
  buildFeatures {
    compose = true
    buildConfig = true
  }
  testOptions { unitTests { isIncludeAndroidResources = true } }
}

// Configure the Secrets Gradle Plugin to use .env and .env.example files
// to match the convention used in Web projects.
secrets {
  propertiesFileName = ".env"
  defaultPropertiesFileName = ".env.example"
}

// Some unused dependencies are commented out below instead of being removed.
// This makes it easy to add them back in the future if needed.
dependencies {
  implementation(platform(libs.androidx.compose.bom))
  implementation(platform(libs.firebase.bom))
  // implementation(libs.accompanist.permissions)
  implementation(libs.androidx.activity.compose)
  // implementation(libs.androidx.camera.camera2)
  // implementation(libs.androidx.camera.core)
  // implementation(libs.androidx.camera.lifecycle)
  // implementation(libs.androidx.camera.view)
  implementation(libs.androidx.compose.material.icons.core)
  implementation(libs.androidx.compose.material.icons.extended)
  implementation(libs.androidx.compose.material3)
  implementation(libs.androidx.compose.ui)
  implementation(libs.androidx.compose.ui.graphics)
  implementation(libs.androidx.compose.ui.tooling.preview)
  implementation(libs.androidx.core.ktx)
  implementation(libs.androidx.core.splashscreen)
  implementation(libs.androidx.datastore.preferences)
  implementation(libs.androidx.lifecycle.runtime.compose)
  implementation(libs.androidx.lifecycle.runtime.ktx)
  implementation(libs.androidx.lifecycle.viewmodel.compose)
  // implementation(libs.androidx.navigation.compose)
  implementation(libs.androidx.room.ktx)
  implementation(libs.androidx.room.runtime)
  implementation(libs.coil.compose)
  implementation(libs.coil.svg)
  implementation(libs.converter.moshi)
  // implementation(libs.firebase.ai)
  implementation(libs.kotlinx.coroutines.android)
  implementation(libs.kotlinx.coroutines.core)
  implementation(libs.logging.interceptor)
  implementation(libs.moshi.kotlin)
  implementation(libs.okhttp)
  // implementation(libs.play.services.location)
  implementation(libs.retrofit)
  testImplementation(libs.androidx.compose.ui.test.junit4)
  testImplementation(libs.androidx.core)
  testImplementation(libs.androidx.junit)
  testImplementation(libs.junit)
  testImplementation(libs.kotlinx.coroutines.test)
  testImplementation(libs.robolectric)
  testImplementation(libs.roborazzi)
  testImplementation(libs.roborazzi.compose)
  testImplementation(libs.roborazzi.junit.rule)
  androidTestImplementation(platform(libs.androidx.compose.bom))
  androidTestImplementation(libs.androidx.compose.ui.test.junit4)
  androidTestImplementation(libs.androidx.espresso.core)
  androidTestImplementation(libs.androidx.junit)
  androidTestImplementation(libs.androidx.runner)
  debugImplementation(libs.androidx.compose.ui.test.manifest)
  debugImplementation(libs.androidx.compose.ui.tooling)
  "ksp"(libs.androidx.room.compiler)
  "ksp"(libs.moshi.kotlin.codegen)
}

tasks.register("copyApkToOutputFolders") {
    val buildDir = layout.buildDirectory
    val projectDir = layout.projectDirectory
    
    outputs.dir(projectDir.dir("../build-outputs"))
    outputs.dir(projectDir.dir("../.build-outputs"))
    outputs.upToDateWhen { false }
    
    doLast {
        val destVisibleDir = projectDir.dir("../build-outputs").asFile
        val destHiddenDir = projectDir.dir("../.build-outputs").asFile
        
        if (!destVisibleDir.exists()) destVisibleDir.mkdirs()
        if (!destHiddenDir.exists()) destHiddenDir.mkdirs()

        val apkDir = buildDir.dir("outputs/apk").get().asFile
        val foundApks = if (apkDir.exists()) {
            apkDir.walkTopDown().filter { it.isFile && it.extension == "apk" }.toList()
        } else {
            emptyList()
        }

        if (foundApks.isNotEmpty()) {
            // Clean previous APK files
            destVisibleDir.listFiles()?.forEach { if (it.name.endsWith(".apk")) it.delete() }
            destHiddenDir.listFiles()?.forEach { if (it.name.endsWith(".apk")) it.delete() }

            // Copy each found APK
            foundApks.forEach { apkFile ->
                val visTarget = File(destVisibleDir, apkFile.name)
                val hiddenTarget = File(destHiddenDir, apkFile.name)
                apkFile.copyTo(visTarget, overwrite = true)
                apkFile.copyTo(hiddenTarget, overwrite = true)
                println("--- COPIED APK: ${apkFile.name} (${apkFile.length()} bytes) ---")
            }

            // Ensure app-debug.apk is always available as default download target
            val defaultDebug = File(destVisibleDir, "app-debug.apk")
            val defaultHiddenDebug = File(destHiddenDir, "app-debug.apk")
            if (!defaultDebug.exists()) {
                val fallbackSource = foundApks.first()
                fallbackSource.copyTo(defaultDebug, overwrite = true)
                fallbackSource.copyTo(defaultHiddenDebug, overwrite = true)
                println("--- CREATED DEFAULT app-debug.apk from ${fallbackSource.name} ---")
            }

            println("--- APK COPY TO OUTPUT FOLDERS COMPLETED SUCCESSFULLY ---")
        } else {
            println("--- APK COPY WARNING: No APK files found in ${apkDir.absolutePath} ---")
        }
    }
}

afterEvaluate {
    tasks.findByName("assembleDebug")?.finalizedBy("copyApkToOutputFolders")
    tasks.findByName("assembleRelease")?.finalizedBy("copyApkToOutputFolders")
    tasks.findByName("assemble")?.finalizedBy("copyApkToOutputFolders")
    tasks.findByName("packageDebug")?.finalizedBy("copyApkToOutputFolders")
    tasks.findByName("packageRelease")?.finalizedBy("copyApkToOutputFolders")
    tasks.findByName("build")?.finalizedBy("copyApkToOutputFolders")
}

