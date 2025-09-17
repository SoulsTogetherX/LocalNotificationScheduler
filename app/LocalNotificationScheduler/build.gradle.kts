plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
}

val pluginName = "LocalNotificationScheduler"
val pluginPackageName = "org.godotengine.plugin.android.localnotificationscheduler"


android {
    namespace = pluginPackageName
    compileSdk = 36

    buildFeatures {
        buildConfig = true
    }

    defaultConfig {
        minSdk = 24

        manifestPlaceholders["godotPluginName"] = pluginName
        manifestPlaceholders["godotPluginPackageName"] = pluginPackageName
        //setProperty("archivesBaseName", pluginName)
    }

    buildTypes {
        all {
            buildConfigField("String", "GODOT_PLUGIN_NAME", "\"${pluginName}\"")
        }
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
}

dependencies {
    //noinspection UseTomlInstead,Aligned16KB
    implementation("org.godotengine:godot:4.3.0.stable")
    implementation(libs.androidx.core)
    implementation(libs.androidx.appcompat)
}

