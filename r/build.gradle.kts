import org.gradle.api.plugins.ExtensionAware
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.kotlin

import org.junit.platform.gradle.plugin.FiltersExtension
import org.junit.platform.gradle.plugin.EnginesExtension
import org.junit.platform.gradle.plugin.JUnitPlatformExtension

plugins {
    `java-gradle-plugin`
    kotlin("jvm")
    dokka
    `bintray-release`
    `junit-platform`
}

group = bintrayGroup
version = bintrayPublish

java.sourceSets {
    getByName("main") {
        java.srcDir("src")
        resources.srcDirs("res")
    }
    getByName("test").java.srcDir("tests/src")
}

configure<JUnitPlatformExtension> {
    filters {
        engines {
            include("spek")
        }
    }
}

dependencies {
    implementation(kotlin("stdlib", kotlinVersion))
    implementation(guava())
    implementation(javapoet())

    testCompile(kotlin("test", kotlinVersion))
    testCompile(kotlin("reflect", kotlinVersion))
    testCompile(spek("api", spekVersion)) {
        exclude("org.jetbrains.kotlin")
    }
    testRuntime(spek("junit-platform-engine", spekVersion)) {
        exclude("org.jetbrains.kotlin")
        exclude("org.junit.platform")
    }
    testCompile(junitPlatform("runner", junitPlatformVersion))
}

publish {
    userOrg = bintrayUser
    groupId = bintrayGroup
    artifactId = bintrayArtifact
    publishVersion = bintrayPublish
    desc = bintrayDesc
    website = bintrayWeb
}

fun JUnitPlatformExtension.filters(setup: FiltersExtension.() -> Unit) {
    when (this) {
        is ExtensionAware -> extensions.getByType(FiltersExtension::class.java).setup()
        else -> throw Exception("${this::class} must be an instance of ExtensionAware")
    }
}

fun FiltersExtension.engines(setup: EnginesExtension.() -> Unit) {
    when (this) {
        is ExtensionAware -> extensions.getByType(EnginesExtension::class.java).setup()
        else -> throw Exception("${this::class} must be an instance of ExtensionAware")
    }
}
