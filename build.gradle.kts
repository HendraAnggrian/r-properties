buildscript {
    repositories {
        jcenter()
    }
    dependencies {
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:$kotlinVersion")
        classpath("com.novoda:bintray-release:0.7.0")
    }
}

allprojects {
    repositories {
        mavenCentral()
    }
}

/** QUICK LINT CHECK BEFORE UPLOAD
./gradlew bintrayUpload -PdryRun=false -PbintrayUser=hendraanggrian -PbintrayKey=
 */
