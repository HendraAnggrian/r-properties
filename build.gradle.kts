buildscript {
    repositories {
        jcenter()
    }
    dependencies {
        classpath(kotlin("gradle-plugin", VERSION_KOTLIN))
        classpath(dokka())
        classpath(gitPublish())
        classpath(bintray())
        classpath(bintrayRelease())
        classpath(junitPlatform("gradle-plugin"))
    }
}

allprojects {
    repositories {
        jcenter()
    }
    tasks.withType<Delete> {
        delete(projectDir.resolve("out"))
    }
}

tasks {
    val clean by registering(Delete::class) {
        delete(rootProject.buildDir)
    }
    val wrapper by registering(Wrapper::class) {
        gradleVersion = VERSION_GRADLE
    }
}