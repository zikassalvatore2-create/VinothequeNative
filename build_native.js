const fs = require('fs');
const path = require('path');

const rootDir = "C:\\Users\\zakar\\Downloads\\VinothequeNative";

function mkdirP(dir) {
    if (!fs.existsSync(dir)) {
        fs.mkdirSync(dir, { recursive: true });
    }
}

mkdirP(rootDir);

// 1. settings.gradle.kts
fs.writeFileSync(path.join(rootDir, 'settings.gradle.kts'), `
pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}
rootProject.name = "VinothequeNative"
include(":app")
`);

// 2. build.gradle.kts (Root)
fs.writeFileSync(path.join(rootDir, 'build.gradle.kts'), `
plugins {
    id("com.android.application") version "8.2.0" apply false
    id("org.jetbrains.kotlin.android") version "1.9.20" apply false
}
`);

// 3. gradle.properties
fs.writeFileSync(path.join(rootDir, 'gradle.properties'), `
org.gradle.jvmargs=-Xmx2048m -Dfile.encoding=UTF-8
android.useAndroidX=true
kotlin.code.style=official
`);

// 4. app/build.gradle.kts
mkdirP(path.join(rootDir, 'app'));
fs.writeFileSync(path.join(rootDir, 'app', 'build.gradle.kts'), `
plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.vinotheque.nativeapp"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.vinotheque.nativeapp"
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
        
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
    buildFeatures {
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.4"
    }
}

dependencies {
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.7.0")
    implementation("androidx.activity:activity-compose:1.8.2")
    implementation(platform("androidx.compose:compose-bom:2024.02.00"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
}
`);

// 5. AndroidManifest.xml
mkdirP(path.join(rootDir, 'app', 'src', 'main'));
fs.writeFileSync(path.join(rootDir, 'app', 'src', 'main', 'AndroidManifest.xml'), `<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android"
    package="com.vinotheque.nativeapp">

    <uses-permission android:name="android.permission.INTERNET" />
    <uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE" />
    <uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" />

    <application
        android:allowBackup="true"
        android:icon="@mipmap/ic_launcher"
        android:label="@string/app_name"
        android:roundIcon="@mipmap/ic_launcher_round"
        android:supportsRtl="true"
        android:theme="@style/Theme.VinothequeNative">
        <activity
            android:name=".MainActivity"
            android:exported="true"
            android:theme="@style/Theme.VinothequeNative">
            <intent-filter>
                <action android:name="android.intent.action.MAIN" />
                <category android:name="android.intent.category.LAUNCHER" />
            </intent-filter>
        </activity>
    </application>
</manifest>
`);

// 6. MainActivity.kt
mkdirP(path.join(rootDir, 'app', 'src', 'main', 'java', 'com', 'vinotheque', 'nativeapp'));
fs.writeFileSync(path.join(rootDir, 'app', 'src', 'main', 'java', 'com', 'vinotheque', 'nativeapp', 'MainActivity.kt'), `package com.vinotheque.nativeapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color(0xFF0d0505) // Dark background
                ) {
                    MainScreen()
                }
            }
        }
    }
}

@Composable
fun MainScreen() {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "🍷",
            fontSize = 80.sp
        )
        Spacer(modifier = Modifier.height(20.dp))
        Text(
            text = "Vinothèque Pro",
            color = Color(0xFFd4a54e), // Gold color
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(10.dp))
        Text(
            text = "Native Android Edition",
            color = Color(0xFFc4b4a4),
            fontSize = 18.sp
        )
        Spacer(modifier = Modifier.height(40.dp))
        Button(
            onClick = { /* TODO: Navigate to cellar */ },
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFd4a54e))
        ) {
            Text(text = "Enter Cellar", color = Color.Black)
        }
    }
}
`);

// 7. Values (strings, themes)
mkdirP(path.join(rootDir, 'app', 'src', 'main', 'res', 'values'));
fs.writeFileSync(path.join(rootDir, 'app', 'src', 'main', 'res', 'values', 'strings.xml'), `
<resources>
    <string name="app_name">Vinothèque</string>
</resources>
`);

fs.writeFileSync(path.join(rootDir, 'app', 'src', 'main', 'res', 'values', 'themes.xml'), `
<resources>
    <style name="Theme.VinothequeNative" parent="android:Theme.Material.Light.NoActionBar" />
</resources>
`);

// 8. Default icon placeholder (we will rely on GitHub actions to build it, or user can put one)
mkdirP(path.join(rootDir, 'app', 'src', 'main', 'res', 'mipmap-anydpi-v26'));
fs.writeFileSync(path.join(rootDir, 'app', 'src', 'main', 'res', 'mipmap-anydpi-v26', 'ic_launcher.xml'), `
<adaptive-icon xmlns:android="http://schemas.android.com/apk/res/android">
    <background android:drawable="@android:color/black"/>
    <foreground android:drawable="@android:color/white"/>
</adaptive-icon>
`);
fs.writeFileSync(path.join(rootDir, 'app', 'src', 'main', 'res', 'mipmap-anydpi-v26', 'ic_launcher_round.xml'), `
<adaptive-icon xmlns:android="http://schemas.android.com/apk/res/android">
    <background android:drawable="@android:color/black"/>
    <foreground android:drawable="@android:color/white"/>
</adaptive-icon>
`);

// 9. GitHub Actions workflow
mkdirP(path.join(rootDir, '.github', 'workflows'));
fs.writeFileSync(path.join(rootDir, '.github', 'workflows', 'build-native.yml'), `name: Build Native Android

on:
  push:
    branches: [ "main" ]
  workflow_dispatch:

jobs:
  build:
    runs-on: ubuntu-latest
    steps:
    - uses: actions/checkout@v3
    
    - name: set up JDK 17
      uses: actions/setup-java@v3
      with:
        java-version: '17'
        distribution: 'temurin'
        cache: gradle
        
    - name: Grant execute permission for gradlew
      run: chmod +x gradlew || true
      
    - name: Build with Gradle
      uses: gradle/gradle-build-action@v2
      with:
        arguments: assembleDebug
        
    - name: Upload APK
      uses: actions/upload-artifact@v3
      with:
        name: app-debug
        path: app/build/outputs/apk/debug/app-debug.apk
`);

console.log("Native project generated successfully in " + rootDir);
