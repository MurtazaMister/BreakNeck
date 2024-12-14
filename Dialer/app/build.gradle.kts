import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.jetbrains.kotlin.konan.properties.Properties
import java.io.FileInputStream

plugins {
    alias(libs.plugins.android)
    alias(libs.plugins.kotlinAndroid)
    alias(libs.plugins.kotlinSerialization)
    alias(libs.plugins.ksp)
    base
    id("com.chaquo.python") version "16.0.0"
}

base {
    archivesName.set("dialer")
}

val keystorePropertiesFile: File = rootProject.file("keystore.properties")
val keystoreProperties = Properties()
if (keystorePropertiesFile.exists()) {
    keystoreProperties.load(FileInputStream(keystorePropertiesFile))
}
val properties = Properties().apply {
    load(rootProject.file("local.properties").reader())
}

android {
    compileSdk = project.libs.versions.app.build.compileSDKVersion.get().toInt()

    defaultConfig {
        applicationId = libs.versions.app.version.appId.get()
        minSdk = project.libs.versions.app.build.minimumSDK.get().toInt()
        targetSdk = project.libs.versions.app.build.targetSDK.get().toInt()
        versionName = project.libs.versions.app.version.versionName.get()
        versionCode = project.libs.versions.app.version.versionCode.get().toInt()
        setProperty("archivesBaseName", "dialer-$versionCode")
        buildConfigField("String", "RIGHT_APP_KEY", "\"${properties["RIGHT_APP_KEY"]}\"")
        buildConfigField("String", "PRODUCT_ID_X1", "\"${properties["PRODUCT_ID_X1"]}\"")
        buildConfigField("String", "PRODUCT_ID_X2", "\"${properties["PRODUCT_ID_X2"]}\"")
        buildConfigField("String", "PRODUCT_ID_X3", "\"${properties["PRODUCT_ID_X3"]}\"")
        buildConfigField("String", "SUBSCRIPTION_ID_X1", "\"${properties["SUBSCRIPTION_ID_X1"]}\"")
        buildConfigField("String", "SUBSCRIPTION_ID_X2", "\"${properties["SUBSCRIPTION_ID_X2"]}\"")
        buildConfigField("String", "SUBSCRIPTION_ID_X3", "\"${properties["SUBSCRIPTION_ID_X3"]}\"")
        buildConfigField("String", "SUBSCRIPTION_YEAR_ID_X1", "\"${properties["SUBSCRIPTION_YEAR_ID_X1"]}\"")
        buildConfigField("String", "SUBSCRIPTION_YEAR_ID_X2", "\"${properties["SUBSCRIPTION_YEAR_ID_X2"]}\"")
        buildConfigField("String", "SUBSCRIPTION_YEAR_ID_X3", "\"${properties["SUBSCRIPTION_YEAR_ID_X3"]}\"")
        ksp {
            arg("room.schemaLocation", "$projectDir/schemas")
        }
        ndk {
            abiFilters += listOf("arm64-v8a", "x86_64")
        }
    }

    signingConfigs {
        if (keystorePropertiesFile.exists()) {
            register("release") {
                keyAlias = keystoreProperties.getProperty("keyAlias")
                keyPassword = keystoreProperties.getProperty("keyPassword")
                storeFile = file(keystoreProperties.getProperty("storeFile"))
                storePassword = keystoreProperties.getProperty("storePassword")
            }
        }
    }

    buildFeatures {
        viewBinding = true
        buildConfig = true
    }

    buildTypes {
        debug {
            applicationIdSuffix = ".debug"
        }
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            if (keystorePropertiesFile.exists()) {
                signingConfig = signingConfigs.getByName("release")
            }
        }
    }

    flavorDimensions += listOf("variants", "pyVersion")

    productFlavors {
        // Existing flavors
        register("core") {
            dimension = "variants"
        }
        register("foss") {
            dimension = "variants"
        }
        register("prepaid") {
            dimension = "variants"
        }

        // Python version flavor
        register("py38") {
            dimension = "pyVersion"
        }
    }

    sourceSets {
        getByName("main") {
            assets {
                srcDirs("src/main/assets")
            }
        }
        getByName("main").java.srcDirs("src/main/kotlin")
    }

    compileOptions {
        val currentJavaVersionFromLibs = JavaVersion.valueOf(libs.versions.app.build.javaVersion.get())
        sourceCompatibility = currentJavaVersionFromLibs
        targetCompatibility = currentJavaVersionFromLibs
    }

    dependenciesInfo {
        includeInApk = false
    }

    tasks.withType<KotlinCompile> {
        kotlinOptions.jvmTarget = project.libs.versions.app.build.kotlinJVMTarget.get()
    }

    namespace = libs.versions.app.version.appId.get()

    lint {
        checkReleaseBuilds = false
        abortOnError = false
    }
}

// Add basic Chaquopy configuration
chaquopy {
    defaultConfig {
        version = "3.8"
        pip {
            install("fastdtw")
            install("scipy")
            install("pandas")
            install("numpy")
            install("fsspec")
        }
    }
    sourceSets {
        getByName("main") {
            srcDirs("src/main/python")
        }
    }
}

dependencies {
    implementation(libs.indicator.fast.scroll)
    implementation(libs.autofit.text.view)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.eventbus)

    //Goodwy
    implementation(libs.goodwy.commons)
    implementation(libs.shortcut.badger)
    implementation(libs.googlecode.libphonenumber)
    implementation(libs.googlecode.geocoder)
    implementation(libs.rustore.client)
    implementation(libs.behavio.rule)
    implementation(libs.rx.animation)
    implementation(libs.rx.java)
    implementation(libs.swipe.action)
    //timer
    implementation(libs.bundles.lifecycle)
    ksp(libs.androidx.room.compiler)
}
