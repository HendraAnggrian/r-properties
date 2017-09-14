package com.hendraanggrian.rproperties

import org.gradle.api.Plugin
import org.gradle.api.Project
import java.io.File
import java.util.*

/**
 * @author Hendra Anggrian (hendraanggrian@gmail.com)
 */
class RPropertiesPlugin : Plugin<Project> {

    private val loader = javaClass.classLoader

    override fun apply(project: Project) {
        val properties = Properties()
        loader.getResource("test.properties")?.let {
            val stream = it.openStream()
            properties.load(stream)
            stream.close()
        }

        val outputDir = project.buildDir.resolve("generated/source/r")
        val task = project.tasks.create("generateR")
        task.outputs.dir(outputDir)
        val file = File("${project.buildDir}/test.java")
        task.apply {
            doLast {
                RClassGenerator.brewJava(properties.keys, outputDir)
            }
        }
        /*project.tasks.create("test123", Copy::class.java, { copy ->
            copy.from(project.fileTree(loader.getResource("test.properties")))
            copy.into("${project.buildDir}${File.separator}test123.properties")
        })*/
    }
}