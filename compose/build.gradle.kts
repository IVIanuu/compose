plugins {
    id("com.android.library")
    kotlin("android")
    id("com.ivianuu.compose")
}

apply(from = "https://raw.githubusercontent.com/IVIanuu/gradle-scripts/master/android-build-lib.gradle")
apply(from = "https://raw.githubusercontent.com/IVIanuu/gradle-scripts/master/java-8-android.gradle")
apply(from = "https://raw.githubusercontent.com/IVIanuu/gradle-scripts/master/kt-android-ext.gradle")
apply(from = "https://raw.githubusercontent.com/IVIanuu/gradle-scripts/master/kt-compiler-args.gradle")
apply(from = "https://raw.githubusercontent.com/IVIanuu/gradle-scripts/master/kt-source-sets-android.gradle")
apply(from = "https://raw.githubusercontent.com/IVIanuu/gradle-scripts/master/mvn-publish.gradle")

dependencies {
    api(Deps.androidxAppCompat)
    api(Deps.androidxUiCore)
    api(Deps.coroutinesCore)
    api(Deps.coroutinesAndroid)
    api(Deps.kotlinReflect)
    api(Deps.kotlinStdLib)

    testImplementation(Deps.androidxTestCore)
    testImplementation(Deps.androidxTestRunner)
    testImplementation(Deps.androidxTestExtJunit)
    testImplementation(Deps.roboelectric)
}