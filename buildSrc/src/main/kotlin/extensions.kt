import org.gradle.api.artifacts.dsl.DependencyHandler

const val bintrayUser = "hendraanggrian"
const val bintrayGroup = "com.hendraanggrian"
const val bintrayArtifact = "rsync"
const val bintrayPublish = "0.7"
const val bintrayDesc = "Type safe resources for Java"
const val bintrayWeb = "https://github.com/hendraanggrian/rsync"

const val kotlinVersion = "1.1.61"
const val guavaVersion = "23.5-jre"
const val javapoetVersion = "1.9.0"

const val junitVersion = "4.12"

fun DependencyHandler.guava(version: String) = "com.google.guava:guava:$version"
fun DependencyHandler.javapoet(version: String) = "com.squareup:javapoet:$version"
fun DependencyHandler.junit(version: String) = "junit:junit:$version"
