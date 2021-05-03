group = RELEASE_GROUP
version = RELEASE_VERSION

plugins {
    `java-gradle-plugin`
    `kotlin-dsl`
    dokka
    `gradle-publish`
}

sourceSets {
    main {
        java.srcDir("src")
    }
    test {
        java.srcDir("tests/src")
    }
    register("functionalTest") {
        java.srcDir("functional-tests/src")
        compileClasspath += sourceSets.main.get().output + configurations.testRuntimeClasspath
        runtimeClasspath += output + compileClasspath
    }
}

gradlePlugin {
    plugins {
        val resPathPlugin by plugins.registering {
            id = "$RELEASE_GROUP.respath"
            implementationClass = "$RELEASE_GROUP.respath.ResPathPlugin"
            displayName = "ResPath Gradle Plugin"
            description = RELEASE_DESCRIPTION
        }
    }
    testSourceSets(sourceSets["functionalTest"])
}

ktlint()

dependencies {
    implementation(kotlin("stdlib", VERSION_KOTLIN))
    implementation(hendraanggrian("javapoet-ktx", VERSION_JAVAPOET_KTX))
    implementation(phCss())
    implementation(jsonSimple())
    testImplementation(kotlin("test-junit", VERSION_KOTLIN))
    "functionalTestImplementation"(gradleTestKit())
    "functionalTestImplementation"(kotlin("test-junit", VERSION_KOTLIN))
}

tasks {
    val functionalTest by registering(Test::class) {
        description = "Runs the functional tests."
        group = LifecycleBasePlugin.VERIFICATION_GROUP
        testClassesDirs = sourceSets["functionalTest"].output.classesDirs
        classpath = sourceSets["functionalTest"].runtimeClasspath
        mustRunAfter(test)
    }
    check { dependsOn(functionalTest) }
}

pluginBundle {
    website = RELEASE_GITHUB
    vcsUrl = RELEASE_GITHUB
    description = RELEASE_DESCRIPTION
    tags = listOf("resources")
}