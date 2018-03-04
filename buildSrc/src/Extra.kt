import org.gradle.api.artifacts.dsl.DependencyHandler
import org.gradle.plugin.use.PluginDependenciesSpec

const val releaseUser = "hendraanggrian"
const val releaseGroup = "com.$releaseUser"
const val releaseArtifact = "r"
const val releaseVersion = "0.7"
const val releaseDesc = "Type-safe resources gradle plugin"
const val releaseWeb = "https://github.com/$releaseUser/$releaseArtifact-plugin"

const val kotlinVersion = "1.2.30"

fun DependencyHandler.dokka() = "org.jetbrains.dokka:dokka-gradle-plugin:0.9.16"
inline val PluginDependenciesSpec.dokka get() = id("org.jetbrains.dokka")

fun DependencyHandler.ktlint() = "com.github.shyiko:ktlint:0.18.0"

fun DependencyHandler.gitPublish() = "org.ajoberstar:gradle-git-publish:0.3.2"
inline val PluginDependenciesSpec.`git-publish` get() = id("org.ajoberstar.git-publish")

fun DependencyHandler.bintrayRelease() = "com.novoda:bintray-release:0.8.0"
inline val PluginDependenciesSpec.`bintray-release` get() = id("com.novoda.bintray-release")

fun DependencyHandler.guava() = "com.google.guava:guava:24.0-jre"
fun DependencyHandler.javapoet() = "com.squareup:javapoet:1.10.0"

fun DependencyHandler.junitPlatform(module: String) = "org.junit.platform:junit-platform-$module:1.0.0"
inline val PluginDependenciesSpec.`junit-platform` get() = id("org.junit.platform.gradle.plugin")

fun DependencyHandler.spek(module: String) = "org.jetbrains.spek:spek-$module:1.1.5"