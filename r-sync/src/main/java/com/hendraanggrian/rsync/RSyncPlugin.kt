package com.hendraanggrian.rsync

import com.squareup.javapoet.JavaFile
import com.squareup.javapoet.TypeSpec
import org.gradle.api.Plugin
import org.gradle.api.Project
import java.io.File
import java.util.*
import javax.lang.model.element.Modifier

/**
 * @author Hendra Anggrian (hendraanggrian@gmail.com)
 */
class RSyncPlugin : Plugin<Project> {

    override fun apply(project: Project) {
        val extension = project.extensions.create("rsync", RSyncExtension::class.java)
        project.afterEvaluate {
            // requirement checks
            require(extension.className.isNotBlank(), { "Class name must be set and not blank!" })
            require(extension.properties.isNotEmpty(), { "Properties must be supplied!" })

            // read properties
            val stream = project.buildscript.classLoader.getResourceAsStream(extension.properties[0])
            checkNotNull(stream, { "${extension.properties[0]} not found!" })
            val properties = Properties().apply { load(stream) }
            stream.close()

            val outputDir = project.buildDir.resolve("generated/source/r")
            val task = project.tasks.create("rsync")
            task.outputs.dir(outputDir)
            val file = File("${project.buildDir}/test.java")
            task.apply {
                doLast {
                    generateClass(properties.keys, outputDir, extension.className)
                }
            }
            /*project.tasks.create("test123", Copy::class.java, { copy ->
                copy.from(project.fileTree(loader.getResource("test.properties")))
                copy.into("${project.buildDir}${File.separator}test123.properties")
            })*/
        }
    }

    companion object {
        private fun generateClass(keys: Set<Any>, outputDir: File, className: String) {
            val builder = TypeSpec.classBuilder(className).addModifiers(Modifier.PUBLIC, Modifier.FINAL)
            keys.forEach { builder.addField(String::class.java, it.toString(), Modifier.PUBLIC, Modifier.FINAL) }
            JavaFile.builder("com.hendraanggrian.rsync", builder.build())
                    .addFileComment("Damn you all to hell.")
                    .build()
                    .writeTo(outputDir)
        }
    }
}