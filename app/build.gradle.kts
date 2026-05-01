plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.dagger.hilt.android")
    id("com.google.devtools.ksp")
}

android {
    namespace = "com.rbook"
    compileSdk = 34
    defaultConfig {
        applicationId = "com.rbook"
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }

        // 限制打包的架构，移除 x86/x86_64，这样可以大幅度减小底层 C++ 库（如 PDF 引擎）导致的安装包体积
        ndk {
            abiFilters.add("armeabi-v7a")
            abiFilters.add("arm64-v8a")
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true // 开启代码混淆和压缩
            isShrinkResources = true // 打开资源文件压缩（移除无用的图片等）
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
        isCoreLibraryDesugaringEnabled = true
    }
    kotlinOptions {
        jvmTarget = "17"
    }
    buildFeatures {
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.8"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
            // 建议增加以下两项，避免 Readium 或 Hilt 引起的合并冲突
            excludes += "META-INF/depends.kotlin_module"
            excludes += "META-INF/gradle/incremental.annotation.processors"
        }
    }
}

dependencies {
    coreLibraryDesugaring("com.android.tools:desugar_jdk_libs:2.0.4")

    val compose_version = "1.5.4"
    val room_version = "2.6.1"
    val hilt_version = "2.48"

    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.6.2")
    implementation("androidx.activity:activity-compose:1.8.1")
    implementation(platform("androidx.compose:compose-bom:2023.08.00"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")
    implementation("androidx.navigation:navigation-compose:2.7.5")

    // Room
    implementation("androidx.room:room-runtime:$room_version")
    implementation("androidx.room:room-ktx:$room_version")
    ksp("androidx.room:room-compiler:$room_version")

    // Hilt
    implementation("com.google.dagger:hilt-android:$hilt_version")
    ksp("com.google.dagger:hilt-compiler:$hilt_version")
    implementation("androidx.hilt:hilt-navigation-compose:1.1.0")

    // Image Loading
    implementation("io.coil-kt:coil-compose:2.5.0")

    // DataStore
    implementation("androidx.datastore:datastore-preferences:1.0.0")

    // Readium (EPUB)
    val readium_version = "2.3.0"
    implementation("org.readium.kotlin-toolkit:readium-shared:$readium_version")
    implementation("org.readium.kotlin-toolkit:readium-streamer:$readium_version")
    implementation("org.readium.kotlin-toolkit:readium-navigator:$readium_version")

    // PDF (Restored to 2.8.2 for compatibility with Aliyun public repository)
    implementation("com.github.barteksc:android-pdf-viewer:2.8.2")

    // Testing
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    androidTestImplementation(platform("androidx.compose:compose-bom:2023.08.00"))
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
}
