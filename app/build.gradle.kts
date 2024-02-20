plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("kotlin-kapt")
    id("com.google.android.libraries.mapsplatform.secrets-gradle-plugin")
    id("com.google.gms.google-services")
    id("com.google.firebase.crashlytics")
}
secrets {
    // Optionally specify a different file name containing your secrets.
    // The plugin defaults to "local.properties"
    propertiesFileName = "secrets.properties"

    // A properties file containing default secret values. This file can be
    // checked in version control.
    defaultPropertiesFileName = "local.properties"

    // Configure which keys should be ignored by the plugin by providing regular expressions.
    // "sdk.dir" is ignored by default.
    ignoreList.add("keyToIgnore") // Ignore the key "keyToIgnore"
    ignoreList.add("sdk.*")       // Ignore all keys matching the regexp "sdk.*"
}


android {
    namespace = "com.app.weatherstack"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.app.weatherstack"
        minSdk = 30
        targetSdk = 34
        versionCode = 4
        versionName = "1.3"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }


    buildFeatures{
        dataBinding = true
        viewBinding = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
}

dependencies {


    implementation("androidx.core:core-ktx:1.9.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.11.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")

    //Pentru Coroutines
    implementation("io.reactivex.rxjava3:rxjava:3.0.2")
    implementation("io.reactivex.rxjava3:rxandroid:3.0.2")
    implementation("com.github.akarnokd:rxjava3-retrofit-adapter:3.0.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.4")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.6.4")

    //Pentru Api de weather
    implementation("com.google.code.gson:gson:2.10.1")
    implementation ("com.squareup.retrofit2:retrofit:2.9.0")
    implementation ("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.5.0")

    //Pentru MVVM
    implementation ("androidx.lifecycle:lifecycle-viewmodel-ktx:<latest_version>")
    implementation ("androidx.lifecycle:lifecycle-livedata-ktx:<latest_version>")
    implementation ("androidx.lifecycle:lifecycle-runtime-ktx:<latest_version>")
    implementation ("androidx.lifecycle:lifecycle-viewmodel-savedstate:<latest_version>")
    implementation ("androidx.activity:activity-ktx:1.8.2")

    //Pentru locatie
    implementation("com.google.android.gms:play-services-maps:18.2.0")

    //Pentru url imagine din news api
    implementation("com.github.bumptech.glide:glide:4.12.0")
    implementation("androidx.work:work-runtime-ktx:2.9.0")
    implementation("com.google.firebase:firebase-crashlytics:18.6.1")
    implementation("com.google.firebase:firebase-analytics:21.5.0")
    annotationProcessor("com.github.bumptech.glide:compiler:4.12.0")

    //Pentru Crashalytics si AdMob
    implementation(platform("com.google.firebase:firebase-bom:32.7.1"))
    implementation("com.google.android.gms:play-services-ads:22.6.0")

    implementation ("com.google.android.gms:play-services-location:21.0.1")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
}