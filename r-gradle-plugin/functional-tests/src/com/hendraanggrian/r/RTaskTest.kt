package com.hendraanggrian.r

import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import java.io.File
import java.io.IOException
import java.util.Properties
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RTaskTest {

    @Rule @JvmField val testProjectDir = TemporaryFolder()
    private lateinit var settingsFile: File
    private lateinit var buildFile: File
    private lateinit var runner: GradleRunner

    @BeforeTest
    @Throws(IOException::class)
    fun setup() {
        testProjectDir.newFolder("src", "main", "resources")
            .resolve("my.properties")
            .outputStream()
            .use { stream ->
                Properties().apply { setProperty("key", "value") }.store(stream, "")
            }
        settingsFile = testProjectDir.newFile("settings.gradle.kts")
        settingsFile.writeText(
            """
            rootProject.name = "generate-test"
            """.trimIndent()
        )
        buildFile = testProjectDir.newFile("build.gradle.kts")
        runner = GradleRunner.create()
            .withPluginClasspath()
            .withProjectDir(testProjectDir.root)
            .withTestKitDir(testProjectDir.newFolder())
    }

    @Test
    fun noConfiguration() {
        // TODO: find out why group and version are always null
        buildFile.writeText(
            """
            group = "com.example"
            version = "1.0"
            plugins {
                java
                idea
                id("com.hendraanggrian.r")
            }
            tasks.getByName<com.hendraanggrian.r.RTask>("generateR") {
                configureProperties()
                packageName.set("com.example")
            }
            """.trimIndent()
        )
        runner.withArguments("compileR").build().let {
            assertEquals(TaskOutcome.SUCCESS, it.task(":compileR")!!.outcome)
            val lines = testProjectDir.root.resolve("build")
                .resolve("generated${File.separator}r${File.separator}src${File.separator}main")
                .resolve("com${File.separator}example${File.separator}R.java")
                .readLines()
            assertTrue("package com.example;" in lines, "invalid package")
            assertTrue("public final class R {" in lines, "invalid class")
            assertTrue("  public static final String key = \"key\";" in lines, "invalid properties")
            assertTrue("  public static final String _my = \"/my.properties\";" in lines, "invalid path")
        }
    }

    @Test
    fun configureAll() {
        buildFile.writeText(
            """
            group = "com.example"
            version = "1.0"
            plugins {
                java
                idea
                id("com.hendraanggrian.r")
            }
            tasks.getByName<com.hendraanggrian.r.RTask>("generateR") {
                configureProperties()
                packageName.set("mypackage")
                className.set("R2")
                shouldUppercaseField.set(true)
                shouldLowercaseClass.set(true)
            }
            """.trimIndent()
        )
        runner.withArguments("compileR").build().let {
            assertEquals(TaskOutcome.SUCCESS, it.task(":compileR")!!.outcome)
            val lines = testProjectDir.root.resolve("build")
                .resolve("generated${File.separator}r${File.separator}src${File.separator}main")
                .resolve("mypackage${File.separator}R2.java")
                .readLines()
            assertTrue("package mypackage;" in lines, "invalid package")
            assertTrue("public final class r2 {" in lines, "invalid class")
            assertTrue("  public static final String KEY = \"key\";" in lines, "invalid properties")
            assertTrue("  public static final String _MY = \"/my.properties\";" in lines, "invalid path")
        }
    }
}