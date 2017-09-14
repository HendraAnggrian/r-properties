package com.hendraanggrian.rsync

import com.squareup.javapoet.FieldSpec
import com.squareup.javapoet.JavaFile
import com.squareup.javapoet.MethodSpec
import com.squareup.javapoet.TypeSpec
import org.gradle.api.Plugin
import org.gradle.api.Project
import java.io.File
import java.io.FileInputStream
import java.nio.file.Files
import java.nio.file.Paths
import java.time.LocalDateTime
import java.util.*
import javax.lang.model.element.Modifier.*

/**
 * @author Hendra Anggrian (hendraanggrian@gmail.com)
 */
class RSyncPlugin : Plugin<Project> {

    override fun apply(project: Project) {
        val extension = project.extensions.create(EXTENSION_RSYNC, RSyncExtension::class.java)
        project.afterEvaluate {
            // requirement checks
            require(extension.packageName.isNotBlank(), { "Package name must not be blank!" })
            require(extension.className.isNotBlank(), { "Class name must not be blank!" })

            // read properties
            val names = mutableSetOf<String>()
            val fields = mutableSetOf<String>()
            Files.walk(Paths.get(project.projectDir.resolve(extension.pathToResources).absolutePath))
                    .filter { Files.isRegularFile(it) }
                    .map { it.toFile() }
                    .filter { it.extension == "properties" && !extension.ignore.contains(it.name) }
                    .forEach {
                        when {
                            !it.exists() -> error("'$it' does not exists!")
                            !it.isFile -> error("'$it' is not a file!")
                            it.extension != "properties" -> error("'$it' is not a properties file!")
                            else -> {
                                val stream = FileInputStream(it)
                                val properties = Properties().apply { load(stream) }
                                stream.close()
                                names.add(it.name)
                                fields.addAll(properties.keys.map { it as? String ?: it.toString() })
                            }
                        }
                    }

            // write class
            val outputDir = project.projectDir.resolve(extension.pathToJava)
            project.tasks.create(TASK_RSYNC).apply {
                outputs.dir(outputDir)
                doLast {
                    generateClass(names, fields, outputDir, extension.packageName, extension.className)
                }
            }
        }
    }

    companion object {
        private const val EXTENSION_RSYNC = "rsync"
        private const val TASK_RSYNC = "rsync"

        private fun generateClass(names: Set<String>, keys: Set<String>, outputDir: File, packageName: String, className: String) {
            val commentBuilder = StringBuilder("rsync generated this class at ${LocalDateTime.now()} from:")
                    .appendln()
            names.forEachIndexed { i, s ->
                commentBuilder.append(s)
                if (i != names.size - 1) {
                    commentBuilder.appendln()
                }
            }

            val classBuilder = TypeSpec.classBuilder(className)
                    .addModifiers(PUBLIC, FINAL)
                    .addMethod(MethodSpec.constructorBuilder().addModifiers(PRIVATE).build())
            keys.forEach { classBuilder.addField(FieldSpec.builder(String::class.java, it, PUBLIC, STATIC, FINAL).initializer("\$S", it).build()) }

            JavaFile.builder(packageName, classBuilder.build())
                    .addFileComment(commentBuilder.toString())
                    .build()
                    .writeTo(outputDir)
        }
    }
}