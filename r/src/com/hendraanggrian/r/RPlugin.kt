package com.hendraanggrian.r

import com.google.common.collect.LinkedHashMultimap
import com.google.common.collect.Multimap
import com.squareup.javapoet.FieldSpec
import com.squareup.javapoet.JavaFile
import com.squareup.javapoet.MethodSpec.constructorBuilder
import com.squareup.javapoet.TypeSpec.classBuilder
import org.gradle.api.Plugin
import org.gradle.api.Project
import java.io.File
import java.io.File.separator
import java.nio.file.Files.*
import java.nio.file.Paths.get
import java.time.LocalDateTime.now
import java.time.format.DateTimeFormatter.ofPattern
import java.util.*
import javax.lang.model.SourceVersion.isName
import javax.lang.model.element.Modifier.*

/** Generate Android-like R class with this plugin. */
class RPlugin : Plugin<Project> {

    override fun apply(project: Project) {
        val ext = project.extensions.create(TASK_NAME, RExtension::class.java)
        project.afterEvaluate {
            project.task(TASK_NAME).apply {
                val outputDir = project.projectDir.resolve(ext.srcDir)
                doFirst {
                    require(ext.packageName.isNotBlank()) { "Package name must not be blank!" }
                    deleteIfExists(get(outputDir.absolutePath, *ext.packageName.split('.').toTypedArray(), "$CLASS_NAME.java"))
                }
                doLast {
                    val multimap = LinkedHashMultimap.create<String, Pair<String, String>>()
                    get(project.projectDir.resolve(ext.resDir).absolutePath)
                            .list()
                            .forEach { path ->
                                val file = path.toFile()
                                when {
                                    isRegularFile(path) && file.isValid && file.isResourceBundle -> file.inputStream().use { stream ->
                                        Properties().apply { load(stream) }.keys.forEach { key ->
                                            val s = key as? String ?: key.toString()
                                            check(isName(file.resourceBundleName)) { "Field name is not a valid java variable name!" }
                                            multimap.put(file.resourceBundleName, s to s)
                                        }
                                    }
                                    isDirectory(path) -> path
                                            .list()
                                            .map { it.toFile() }
                                            .filter { it.isValid }
                                            .forEach { innerFile ->
                                                val s = "${file.name}$separator${innerFile.name}"
                                                check(isName(file.resourceBundleName)) { "Field name is not a valid java variable name!" }
                                                multimap.put(file.name, innerFile.nameWithoutExtension to when {
                                                    ext.leadingSlash -> "/$s"
                                                    else -> s
                                                })
                                            }
                                }
                            }
                    generateClass(ext.packageName, multimap, outputDir)
                }
            }
        }
    }

    companion object {
        internal const val TASK_NAME = "r"
        private const val CLASS_NAME = "R"

        private fun generateClass(packageName: String, multimap: Multimap<String, Pair<String, String>>, outputDir: File) = JavaFile
                .builder(packageName, classBuilder(CLASS_NAME)
                        .addModifiers(PUBLIC, FINAL)
                        .addMethod(constructorBuilder()
                                .addModifiers(PRIVATE)
                                .build())
                        .apply {
                            multimap.keySet().forEach { innerClass ->
                                addType(classBuilder(innerClass)
                                        .addModifiers(PUBLIC, STATIC, FINAL)
                                        .addMethod(constructorBuilder()
                                                .addModifiers(PRIVATE)
                                                .build())
                                        .apply {
                                            multimap.get(innerClass).forEach { (field, value) ->
                                                addField(FieldSpec.builder(String::class.java, field, PUBLIC, STATIC, FINAL)
                                                        .initializer("\$S", value)
                                                        .build())
                                            }
                                        }
                                        .build())
                            }
                        }
                        .build())
                .addFileComment("$TASK_NAME generated this class at ${now().format(ofPattern("MM-dd-yyyy 'at' h.mm.ss a"))}")
                .build()
                .writeTo(outputDir)
    }
}