plugins {
    id("com.android.library")
    id("kotlin-android")
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

    testImplementation(Deps.roboelectric)
}