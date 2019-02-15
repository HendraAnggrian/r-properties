plugins {
    `java-gradle-plugin`
    `kotlin-dsl`
    dokka()
    bintray
    `bintray-release`
}

group = RELEASE_GROUP
version = RELEASE_VERSION

sourceSets {
    getByName("main") {
        java.srcDir("src")
    }
    getByName("test") {
        java.srcDir("tests/src")
    }
}

gradlePlugin {
    (plugins) {
        register(RELEASE_ARTIFACT) {
            id = "$RELEASE_GROUP.r"
            implementationClass = "$RELEASE_GROUP.r.RPlugin"
        }
    }
}

val ktlint by configurations.registering

dependencies {
    implementation(kotlin("stdlib", VERSION_KOTLIN))
    implementation(hendraanggrian("javapoet-dsl", "0.2-rc1"))
    implementation(phCss())
    implementation(jsonSimple())

    testImplementation(kotlin("test", VERSION_KOTLIN))
    testImplementation(junit())

    ktlint {
        invoke(ktlint())
    }
}

tasks {
    register("deploy") {
        dependsOn("build")
        projectDir.resolve("build/libs")?.listFiles()?.forEach {
            val outputFile = File(rootDir.resolve("integration-tests"), it.name)
            if (outputFile.exists()) outputFile.delete()
            it.renameTo(outputFile)
        }
    }

    val ktlint by registering(JavaExec::class) {
        group = LifecycleBasePlugin.VERIFICATION_GROUP
        inputs.dir("src")
        outputs.dir("src")
        description = "Check Kotlin code style."
        classpath(configurations["ktlint"])
        main = "com.github.shyiko.ktlint.Main"
        args("src/**/*.kt")
    }
    "check" {
        dependsOn(ktlint)
    }
    register("ktlintFormat", JavaExec::class) {
        group = "formatting"
        inputs.dir("src")
        outputs.dir("src")
        description = "Fix Kotlin code style deviations."
        classpath(configurations["ktlint"])
        main = "com.github.shyiko.ktlint.Main"
        args("-F", "src/**/*.kt")
    }

    "dokka"(org.jetbrains.dokka.gradle.DokkaTask::class) {
        outputDirectory = "$buildDir/docs"
        doFirst {
            file(outputDirectory).deleteRecursively()
            buildDir.resolve("gitPublish").deleteRecursively()
        }
    }
}

publish {
    bintrayUser = BINTRAY_USER
    bintrayKey = BINTRAY_KEY
    dryRun = false

    userOrg = RELEASE_USER
    groupId = RELEASE_GROUP
    artifactId = RELEASE_ARTIFACT
    publishVersion = RELEASE_VERSION
    desc = RELEASE_DESC
    website = RELEASE_WEBSITE
}