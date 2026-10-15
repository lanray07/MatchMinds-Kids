plugins { id("com.android.application"); kotlin("android"); id("org.jetbrains.kotlin.plugin.compose") }
android {
    namespace = "com.matchminds.kids"
    compileSdk = 36
    defaultConfig {
        applicationId = "com.matchminds.kids"
        minSdk = 26
        targetSdk = 36
        versionCode = 1
        versionName = "1.0.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        buildConfigField("String", "VERIFICATION_URL", "\"${providers.gradleProperty("verificationUrl").orElse("").get()}\"")
        buildConfigField("boolean", "VOICE_APPROVED", providers.gradleProperty("voiceApproved").orElse("false").get())
    }
    buildFeatures { compose = true; buildConfig = true }
    testOptions { unitTests.isIncludeAndroidResources = true; unitTests.all {
        it.systemProperty("storeCapture", providers.gradleProperty("storeCapture").orElse("false").get())
        it.systemProperty("roborazzi.test.record", providers.gradleProperty("storeCapture").orElse("false").get())
        it.systemProperty("robolectric.pixelCopyRenderMode", "hardware")
        if (!project.hasProperty("storeCapture")) it.exclude("**/StoreCaptureTest*")
        it.maxHeapSize = "3g"
    } }
    buildTypes {
        debug { applicationIdSuffix = ".debug" }
        release { isMinifyEnabled = true; proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro") }
    }
    compileOptions { sourceCompatibility = JavaVersion.VERSION_17; targetCompatibility = JavaVersion.VERSION_17 }
    kotlinOptions { jvmTarget = "17" }
    packaging { resources.excludes += "/META-INF/{AL2.0,LGPL2.1}" }
}
dependencies {
    implementation(project(":core"))
    implementation(platform("androidx.compose:compose-bom:2025.04.01"))
    implementation("androidx.activity:activity-compose:1.10.1")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.9.0")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.9.0")
    implementation("androidx.datastore:datastore-preferences:1.1.4")
    implementation("com.amazon.device:amazon-appstore-sdk:3.0.9")
    testImplementation("junit:junit:4.13.2")
    testImplementation("org.json:json:20240303")
    testImplementation("org.robolectric:robolectric:4.14.1")
    testImplementation("androidx.compose.ui:ui-test-junit4")
    testImplementation("androidx.test:core:1.6.1")
    testImplementation("io.github.takahirom.roborazzi:roborazzi:1.46.1")
    testImplementation("io.github.takahirom.roborazzi:roborazzi-compose:1.46.1")
    debugImplementation("androidx.compose.ui:ui-tooling")
    androidTestImplementation(platform("androidx.compose:compose-bom:2025.04.01"))
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    androidTestImplementation("androidx.test:runner:1.6.2")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
}
