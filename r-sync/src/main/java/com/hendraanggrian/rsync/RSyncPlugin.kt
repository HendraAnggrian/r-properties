package com.hendraanggrian.rsync

import com.google.common.collect.LinkedHashMultimap
import com.squareup.javapoet.JavaFile
import com.squareup.javapoet.TypeSpec
import org.gradle.api.Plugin
import org.gradle.api.Project
import java.io.File
import java.io.FileInputStream
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
            require(extension.className.isNotBlank(), { "Class name must not be blank!" })
            require(extension.properties.isNotEmpty(), { "Properties must be supplied!" })

            // read properties
            val multimap = LinkedHashMultimap.create<String, String>()
            extension.properties.forEach {
                val file = File(it)
                when {
                    file.exists() -> error("'$it' does not exists!")
                    !file.isFile -> error("'$it' is not a file!")
                    file.extension != "properties" -> error("'$it' is not a properties file!")
                    else -> {
                        val stream = FileInputStream(file)
                        val properties = Properties().apply { load(stream) }
                        stream.close()
                        multimap.putAll(it, properties.keys.map { it as? String ?: it.toString() })
                    }
                }
            }
            var temp: Set<String>? = null
            for (key in multimap.keys()) {
                val set = multimap.get(key)
                if (temp != null) {
                    require(temp.containsAll(set), { "Keys mismatch in multiple properties." })
                }
                temp = set
            }

            val outputDir = project.buildDir.resolve("generated/source/r")
            val task = project.tasks.create("rsync")
            task.outputs.dir(outputDir)
            val file = File("${project.buildDir}/test.java")
            task.apply {
                doLast {
                    generateClass(temp!!, outputDir, extension.className)
                }
            }
            /*project.tasks.create("test123", Copy::class.java, { copy ->
                copy.from(project.fileTree(loader.getResource("test.properties")))
                copy.into("${project.buildDir}${File.separator}test123.properties")
            })*/
        }
    }

    companion object {
        private fun generateClass(keys: Set<String>, outputDir: File, className: String) {
            val builder = TypeSpec.classBuilder(className).addModifiers(Modifier.PUBLIC, Modifier.FINAL)
            keys.forEach { builder.addField(String::class.java, it, Modifier.PUBLIC, Modifier.FINAL) }
            JavaFile.builder("com.hendraanggrian.rsync", builder.build())
                    .addFileComment("Damn you all to hell.")
                    .build()
                    .writeTo(outputDir)
        }
    }
}